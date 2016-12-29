package db

import checker.TomitaChecker
import com.twitter.util.Future
import scalikejdbc._

case class QuestionAnswer(id: BigInt, question: Question, answer: String, creator: User, xml: Option[String] = None) {
  def updateXml(): Future[QuestionAnswer] = TomitaChecker.getOutput(answer).map { xml =>
    val result = DB localTx { implicit session =>
      sql"""
           UPDATE question_answer
           SET question_answer_tomita_xml = ${xml.toString}
           WHERE question_answer_id = $id AND question_id = ${question.id}
        """
        .update()
        .apply()
    }
    if (result > 0) QuestionAnswer(id, question, answer, creator, Some(xml.toString())) else this
  }
}

object QuestionAnswer {

  private var cache = Map[Question, BigInt]()

  private def getIdFromDb(question: Question) = DB readOnly { implicit session =>
    sql"SELECT COUNT(*) FROM question_answer WHERE question_id = ${question.id}"
      .map(rs => BigInt(rs.bigInt(1)))
      .single()
      .apply()
  }

  private def getId(question: Question) = {
    val value = cache.getOrElse(question, getIdFromDb(question).getOrElse(BigInt(0))) + 1
    cache = cache.updated(question, value)
    value
  }

  def create(question: Question, user: User, answer: String): Option[QuestionAnswer] = {
    val id = getId(question)
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"""
             INSERT INTO question_answer(question_answer_id, question_id, question_answer_value, question_answer_creator_id)
              VALUES ($id, ${question.id}, $answer, ${user.id})
           """
          .update()
          .apply()
      }
    }
    if (result > 0) Some(QuestionAnswer(id, question, answer, user)) else None
  }

  def fromQuestion(question: Question) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            question_answer_id,
            question_id,
            question_answer_value,
            question_answer_creator_id,
            question_answer_tomita_xml
           FROM
            question_answer
           WHERE question_id = ${question.id}
         """
        .map(x => fromResultSet(x, question))
        .list()
        .apply()
    }
  }

  def fromResultSet(x: WrappedResultSet, question: Question) = QuestionAnswer(
    id = x.bigInt("question_answer_id"),
    question = question,
    answer = x.string("question_answer_value"),
    creator = User.withId(x.bigInt("question_answer_creator_id")).get,
    xml = x.stringOpt("question_answer_tomita_xml")
  )

  def fromResultSet(x: WrappedResultSet) = QuestionAnswer(
    id = x.bigInt("question_answer_id"),
    question = Question.withId(x.bigInt("question_id")).get,
    answer = x.string("question_answer_value"),
    creator = User.withId(x.bigInt("question_answer_creator_id")).get,
    xml = x.stringOpt("question_answer_tomita_xml")
  )
}
