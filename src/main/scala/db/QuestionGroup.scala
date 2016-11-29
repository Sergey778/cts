package db

import scalikejdbc._

case class QuestionGroup(id: BigInt, name: String, creator: User, parentGroup: Option[QuestionGroup]) {
  def childs = QuestionGroup.findByParent(id)

  def questions = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            question_id,
            question_creator_id,
            question_modifier_id,
            question_create_time,
            question_modify_time,
            question_text
           FROM question
           WHERE question_group_id = $id
         """
        .map(x => Question.fromResultSet(x, group = Some(this)))
        .list()
        .apply()
    }
  }

  def questionsCount = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT COUNT(question_id) FROM question WHERE question_group_id = $id"
        .map(x => x.int(1))
        .single()
        .apply()
    }
  }
}

object QuestionGroup {

  private def createId = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      sql"SELECT nextval('question_group_seq')"
        .map(rs => rs.bigInt(1))
        .single()
        .apply()
    }
  }

  def create(name: String, creator: User, parentGroup: Option[QuestionGroup]): Option[QuestionGroup] = createId flatMap { id =>
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"""
             INSERT INTO question_group
             (question_group_id, question_group_name, question_group_creator_id, question_group_parent_id)
             VALUES
             (${BigInt(id)}, $name, ${creator.id}, ${parentGroup.map(x => x.id)})
           """
        .update()
        .apply()
      }
    }
    if (result > 0) Some(id) else None
  } map { id =>
    QuestionGroup(id, name, creator, parentGroup)
  }
  // SHOULD BE REWRITTEN
  def fromResultSet(rs: WrappedResultSet): QuestionGroup = QuestionGroup(
    id = rs.bigInt("question_group_id"),
    name = rs.string("question_group_name"),
    creator = User.findById(rs.bigInt("question_group_creator_id")).get,
    parentGroup = findById(rs.bigIntOpt("question_group_parent_id").map(x => BigInt(x)))
  )

  def findByUser(user: User) = {
    using(DB(ConnectionPool.borrow())) { db =>
      db readOnly { implicit session =>
        sql"""
            SELECT question_group_id, question_group_name, question_group_creator_id, question_group_parent_id
            FROM question_group
            WHERE question_group_creator_id = ${user.id} AND question_group_parent_id IS NULL
          """
          .map(rs => fromResultSet(rs))
          .list()
          .apply()
      }
    }
  }

  def findById(id: BigInt) = {
    using(DB(ConnectionPool.borrow())) { db =>
      db readOnly { implicit session =>
        sql"""
            SELECT
            question_group_id, question_group_name, question_group_creator_id, question_group_parent_id
            FROM question_group
            WHERE question_group_id = $id
          """
          .map(rs => fromResultSet(rs))
          .single()
          .apply()
      }
    }
  }

  def findById(id: Option[BigInt]) = {
    using(DB(ConnectionPool.borrow())) { db =>
      db readOnly { implicit session =>
        sql"""
            SELECT
            question_group_id, question_group_name, question_group_creator_id, question_group_parent_id
            FROM question_group
            WHERE question_group_id = $id
          """
          .map(rs => fromResultSet(rs))
          .single()
          .apply()
      }
    }
  }

  def findByParent(id: BigInt) = {
    using(DB(ConnectionPool.borrow())) { db =>
      db readOnly { implicit session =>
        sql"""
            SELECT
            question_group_id, question_group_name, question_group_creator_id, question_group_parent_id
            FROM question_group
            WHERE question_group_parent_id = $id
          """
          .map(rs => fromResultSet(rs))
          .list()
          .apply()
      }
    }
  }
}
