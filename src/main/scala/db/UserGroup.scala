package db

import scalikejdbc._

import scala.annotation.tailrec

case class UserGroup(id: BigInt, name: String, leader: User, parentGroup: Option[UserGroup]) {

  def users = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            user_id, user_name, user_email, user_password, user_signup_time, user_confirmed
           FROM "user"
           WHERE user_group_id = ${id}
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
         WHERE user_group_parent_id = ${id}
         """
        .map(x => UserGroup.fromResultSet(x))
        .list()
        .apply()
    }
  }
}

object UserGroup {

  protected def generateId = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      sql"SELECT nextval('user_group_seq')"
        .map(rs => BigInt(rs.bigInt(1)))
        .single()
        .apply()
    }
  }

  def fromResultSet(rs: WrappedResultSet): UserGroup = UserGroup(
    id = rs.bigInt("user_group_id"),
    name = rs.string("user_group_name"),
    leader = User.findById(rs.bigInt("user_group_leader")).get,
    parentGroup = rs.bigIntOpt("user_group_parent_id").flatMap(x => findById(BigInt(x)))
  )

  def create(name: String, leader: User, parentGroup: Option[UserGroup]) = generateId flatMap { id =>
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"""
             INSERT INTO
              user_group (user_group_id, user_group_leader, user_group_name, user_group_parent_id)
             VALUES (${id}, ${leader.id}, ${name}, ${parentGroup.map(x => x.id)})
          """
          .update()
          .apply()
      }
    }
    if (result > 0) Some(UserGroup(id, name, leader, parentGroup)) else None
  }

  def all = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT user_group_id, user_group_leader, user_group_name, user_group_parent_id FROM user_group"
        .map(rs => fromResultSet(rs))
        .list()
        .apply()
    }
  }

  def findById(id: BigInt) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            user_group_id, user_group_leader, user_group_name, user_group_parent_id
           FROM user_group
           WHERE user_group_id = ${id}
         """
        .map(rs => fromResultSet(rs))
        .single()
        .apply()
    }
  }
}
