package db

import db.core.{IdHolder, Table, TableObject}
import scalikejdbc._


case class Course(id: BigInt, name: String, description: String, creator: User) extends Table {
  def userGroups: List[UserGroup] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            u.user_group_id "user_group_id",
            u.user_group_leader "user_group_leader",
            u.user_group_name "user_group_name",
            u.user_group_parent_id "user_group_parent_id"
           FROM course_groups c JOIN user_group u ON c.group_id = u.user_group_id
           WHERE c.course_id = $id
         """
        .map(rs => UserGroup.fromResultSet(rs))
        .list()
        .apply()
    }
  }

  def tests: List[Test] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            t.test_id "test_id",
            t.test_name "test_name",
            t.test_creator_id "test_creator_id"
           FROM course_tests c JOIN test t ON c.test_id = t.test_id
           WHERE c.course_id = $id
         """
        .map(rs => Test.fromResultSet(rs))
        .list()
        .apply()
    }
  }

  def addUserGroup(group: UserGroup): Option[Course] = using(DB(ConnectionPool.borrow())) { db =>
    val result = db localTx { implicit session =>
      sql"""
           INSERT INTO course_groups (course_id, group_id)
           VALUES ($id, ${group.id})
         """
        .update()
        .apply()
    }
    if (result > 0) Some(this) else None
  }

  def addTest(test: Test): Option[Course] = using(DB(ConnectionPool.borrow())) { db =>
    val result = db localTx { implicit session =>
      sql"""
           INSERT INTO course_tests (course_id, test_id)
           VALUES ($id, ${test.id})
         """
        .update()
        .apply()
    }
    if (result > 0) Some(this) else None
  }
}

object Course extends TableObject[Course] with IdHolder {

  override def name: String = "course"

  private final val $id = "id"
  private final val $name = "name"
  private final val $description = "description"
  private final val $creator = "creator"

  override def columns: Map[String, String] = Map(
    $id -> "course_id",
    $name -> "course_name",
    $description -> "course_description",
    $creator -> "course_creator_id"
  )

  def create(name: String, description: String, creator: User): Option[Course] = nextId flatMap { id =>
    using(DB(ConnectionPool.borrow())) { db =>
      val result = db localTx { implicit session =>
        sql"""
             INSERT INTO course (course_id, course_name, course_description, course_creator_id)
             VALUES ($id, $name, $description, ${creator.id})
           """
          .update()
          .apply()
      }
      if (result > 0) Some(Course(id, name, description, creator)) else None
    }
  }

  override def fromResultSet(rs: WrappedResultSet) = Course(
    id = rs.bigInt(columns($id)),
    name = rs.string(columns($name)),
    description = rs.string(columns($description)),
    creator = User.withId(rs.bigInt(columns($creator))).get
  )

  def withId(id: BigInt): Option[Course] = whereOption($id -> id)

  def availableForUser(user: User): List[Course] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            c.course_id "course_id",
            c.course_name "course_name",
            c.course_description "course_description",
            c.course_creator_id "course_creator_id"
           FROM
            course c JOIN course_groups cg ON c.course_id = cg.course_id
            JOIN user_groups ug ON cg.group_id = ug.user_group_id
            WHERE ug.user_id = ${user.id}
         """
        .map(rs => fromResultSet(rs))
        .list()
        .apply()
    }
  }

  def withCreator(user: User): List[Course] = whereList($creator -> user.id)
}
