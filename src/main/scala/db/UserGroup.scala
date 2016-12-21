package db

import db.core.{IdHolder, Table, TableObject}
import scalikejdbc._

import scala.annotation.tailrec

case class UserGroup(id: BigInt, name: String, leader: User, parentGroup: Option[UserGroup]) extends Table {

  def users = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            user_id, user_name, user_email, user_password, user_signup_time, user_confirmed
           FROM "user"
           WHERE user_group_id = $id
         """
        .map(rs => User.fromResultSet(rs))
        .list()
        .apply()
    }
  }

  def fullName(separator: String = "/") = {
    @tailrec
    def loop(current: UserGroup, result: List[String] = List()): List[String] = current.parentGroup match {
      case Some(group) => loop(group, current.name :: result)
      case None => current.name :: result
    }
    loop(this).mkString(separator)
  }

  def childs = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
         SELECT
          user_group_id, user_group_leader, user_group_name, user_group_parent_id
         FROM user_group
         WHERE user_group_parent_id = $id
         """
        .map(x => UserGroup.fromResultSet(x))
        .list()
        .apply()
    }
  }

  def members: List[User] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT user_id FROM user_groups WHERE user_group_id = $id
         """
        .map(rs => User.withId(rs.bigInt("user_id")))
        .list()
        .apply()
        .flatten
    }
  }

  def fullMembers: List[User] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT user_id FROM user_groups WHERE user_group_id = $id AND full_member = 1
         """
        .map(rs => User.withId(rs.bigInt("user_id")))
        .list()
        .apply()
        .flatten
    }
  }

  def makeFullMember(user: User) = using(DB(ConnectionPool.borrow())) { db =>
    val result = db localTx { implicit session =>
      sql"UPDATE user_groups SET full_member = 1 WHERE user_group_id = $id AND user_id = ${user.id}"
        .update()
        .apply()
    }
    result > 0
  }

  def addMember(user: User) = using(DB(ConnectionPool.borrow())) { db =>
    val result = db localTx { implicit session =>
      sql"""
           INSERT INTO user_groups(user_id, user_group_id, full_member)
           VALUES (${user.id}, $id, 0)
         """
        .update()
        .apply()
    }
    if (result > 0) Some(this) else None
  }
}

object UserGroup extends TableObject[UserGroup] with IdHolder {

  override def name: String = "user_group"

  private final val $id = "id"
  private final val $name = "name"
  private final val $leader = "leader"
  private final val $parentGroup = "parentGroup"

  override def columns: Map[String, String] = Map (
    $id -> "user_group_id",
    $name -> "user_group_name",
    $leader -> "user_group_leader",
    $parentGroup -> "user_group_parent_id"
  )

  def fromResultSet(rs: WrappedResultSet,
                    id: Option[BigInt] = None,
                    name: Option[String] = None,
                    leader: Option[User] = None,
                    parentGroup: Option[Option[UserGroup]] = None) = UserGroup(
    id = id.getOrElse(rs.bigInt(columns($id))),
    name = name.getOrElse(rs.string(columns($name))),
    leader = leader.orElse(User.withId(rs.bigInt(columns($leader)))).get,
    parentGroup = parentGroup
      .orElse(rs.bigIntOpt(columns($parentGroup)).map(x => withId(BigInt(x))))
      .getOrElse(None)
  )

  override def fromResultSet(rs: WrappedResultSet): UserGroup = fromResultSet(rs, None, None, None, None)

  def create(name: String, leader: User, parentGroup: Option[UserGroup]): Option[UserGroup] = nextId flatMap { id =>
    val result = DB localTx { implicit session =>
      insertSql(
        $id -> BigInt(id),
        $name -> name,
        $leader -> leader.id,
        $parentGroup -> parentGroup.map(x => x.id)
      ).update().apply()
    }
    if (result > 0) {
      val group = UserGroup(id, name, leader, parentGroup)
      group
        .addMember(leader)
        .map(g => g.makeFullMember(leader))
        .map(x => group)
    } else {
      None
    }

  }

  def withId(id: BigInt): Option[UserGroup] = whereOption($id -> id)
}
