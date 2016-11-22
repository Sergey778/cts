package db

import scalikejdbc._


case class Course(id: BigInt, name: String, description: String, creator: User) {
  def userGroups = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            u.user_group_id "user_group_id",
            u.user_group_leader "user_group_leader",
            u.user_group_name "user_group_name",
            u.user_group_parent_id "user_group_parent_id"
           FROM course_groups c JOIN user_group u ON c.group_id = u.user_group_id
           WHERE c.course_id = ${id}
         """
        .map(rs => UserGroup.fromResultSet(rs))
        .list()
        .apply()
    }
  }

  def tests = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            t.test_id "test_id",
            t.test_name "test_name",
            t.test_creator_id "test_creator_id"
           FROM course_tests c JOIN test t ON c.test_id = t.test_id
           WHERE c.course_id = ${id}
         """
        .map(rs => Test.fromResultSet(rs))
        .list()
        .apply()
    }
  }

  def addUserGroup(group: UserGroup) = using(DB(ConnectionPool.borrow())) { db =>
    val result = db localTx { implicit session =>
      sql"""
           INSERT INTO course_groups (course_id, group_id)
           VALUES (${id}, ${group.id})
         """
        .update()
        .apply()
    }
    if (result > 0) Some(this) else None
  }

  def addTest(test: Test) = using(DB(ConnectionPool.borrow())) { db =>
    val result = db localTx { implicit session =>
      sql"""
           INSERT INTO course_tests (course_id, test_id)
           VALUES (${id}, ${test.id})
         """
        .update()
        .apply()
    }
    if (result > 0) Some(this) else None
  }
}

object Course {

  protected def generateId = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      sql"""SELECT nextval('course_seq')"""
        .map(rs => BigInt(rs.bigInt(1)))
        .single()
        .apply()
    }
  }

  def create(name: String, description: String, creator: User): Option[Course] = generateId flatMap { id =>
    using(DB(ConnectionPool.borrow())) { db =>
      val result = db localTx { implicit session =>
        sql"""
             INSERT INTO course (course_id, course_name, course_description, course_creator_id)
             VALUES (${id}, ${name}, ${description}, ${creator.id})
           """
          .update()
          .apply()
      }
      if (result > 0) Some(Course(id, name, description, creator)) else None
    }
  }

  def fromResultSet(rs: WrappedResultSet) = User.findById(rs.bigInt("course_creator_id")) map { creator =>
    Course(rs.bigInt("course_id"), rs.string("course_name"), rs.string("course_description"), creator)
  }

  def all = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT course_id, course_name, course_description, course_creator_id FROM course"
        .map(rs => fromResultSet(rs))
        .list()
        .apply()
    }
  }

  def fromId(id: BigInt) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT course_id, course_name, course_description, course_creator_id
           FROM course
           WHERE course_id = ${id}
         """
        .map(rs => fromResultSet(rs))
        .single()
        .apply()
    }
  }

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
        .flatten
    }
  }

  def createdByUser(user: User): List[Course] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT course_id, course_name, course_description, course_creator_id
           FROM course
           WHERE course_creator_id = ${user.id}
         """
        .map(x => fromResultSet(x))
        .list()
        .apply()
        .flatten
    }
  }
}
