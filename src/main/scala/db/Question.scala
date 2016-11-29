package db

import java.sql.Timestamp

import scalikejdbc._

case class Question (
                    id: BigInt,
                    creator: User,
                    modifier: User,
                    createTime: Timestamp,
                    modifyTime: Timestamp,
                    text: String,
                    group: QuestionGroup
                    )

object Question {
  def findById(id: BigInt) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            question_id,
            question_creator_id,
            question_modifier_id,
            question_create_time,
            question_modify_time,
            question_text,
            question_group_id
           FROM
            question
           WHERE question_id = $id
         """
        .map(x => fromResultSet(x))
        .single()
        .apply()
    }
  }
  // REWRITE THIS:
  def fromResultSet(rs: WrappedResultSet,
                    group: Option[QuestionGroup] = None) = Question(
    id = rs.bigInt("question_id"),
    creator = User.findById(rs.bigInt("question_creator_id")).get,
    modifier = User.findById(rs.bigInt("question_modifier_id")).get,
    createTime = rs.timestamp("question_create_time"),
    modifyTime = rs.timestamp("question_modify_time"),
    text = rs.string("question_text"),
    group = group.getOrElse(
        QuestionGroup.findById(rs.bigInt("question_group_id")).get
    )
  )

  def findByCreator(user: User) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            question_id,
            question_creator_id,
            question_modifier_id,
            question_create_time,
            question_modify_time,
            question_text,
            question_group_id
           FROM
            question
           WHERE question_creator_id = ${user.id}
         """
        .map(x => fromResultSet(x))
        .list()
        .apply()
    }
  }

  protected def createId =
    using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"SELECT nextval('question_seq')"
          .map(x => x.bigInt(1))
          .single()
          .apply()
      }
    }

  def create(creator: User, text: String, group: QuestionGroup) = createId flatMap { id =>
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"""
           INSERT INTO question (question_id, question_creator_id, question_modifier_id, question_text, question_group_id)
            VALUES (${BigInt(id)}, ${creator.id}, ${creator.id}, $text, ${group.id})
         """
          .update()
          .apply()
      }
    }
    if (result > 0) findById(id) else None
  }
}
