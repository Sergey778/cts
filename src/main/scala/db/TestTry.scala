package db

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import scalikejdbc._


case class TestTry(id: String, test: Test, user: User, startedTime: LocalDateTime) {
  def answers: Map[Question, Option[String]] = DB readOnly { implicit session =>
    sql"""
         SELECT answer, question_id
         FROM test_try_answers
         WHERE test_try_id = $id
       """
      .map(rs => Question.withId(rs.bigInt("question_id")) -> rs.stringOpt("answer"))
      .list()
      .apply()
  } filter {
    case ((Some(_), _)) => true
    case _ => false
  } map { case (q, a) =>
    q.get -> a
  } toMap

  def updateAnswer(q: Question, answer: String): Option[TestTry] = DB localTx { implicit session =>
    val result = sql"UPDATE test_try_answers SET answer = $answer WHERE test_try_id = $id AND question_id = ${q.id}"
      .update()
      .apply()

    if (result > 0) Some(this) else None
  }


  def setAnswersChecked(answers: Map[Question, Boolean]): Option[Map[Question, Boolean]] =
    DB localTx { implicit session =>
    val result = answers map { case (question, isCorrect) =>
      sql"UPDATE test_try_answers SET is_correct = ${if (isCorrect) 1 else 0} WHERE test_try_id = $id AND question_id = ${question.id}"
          .update()
          .apply()
    }
    if (result.sum == answers.size) Some(answers) else None
  }

  def createAnswers(questions: List[Question]): Option[TestTry] = DB localTx { implicit session =>
    val result = questions map { q =>
      sql"INSERT INTO test_try_answers (test_try_id, question_id) VALUES ($id, ${q.id})"
        .update()
        .apply()
    }
    if (result.sum == questions.size) Some(this) else None
  }
}

object TestTry {
  def create(test: Test, user: User): Option[TestTry] = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      val uuid = UUID.randomUUID().toString
      val time = LocalDateTime.now()
      val result = sql"""
           INSERT INTO test_try(test_try_id, test_id, user_id, test_try_start)
            VALUES ($uuid, ${test.id}, ${user.id}, ${Timestamp.valueOf(time)})
         """
        .update()
        .apply()
      if (result > 0) Some(TestTry(uuid, test, user, time)) else None
    }
  }

  def fromResultSet(rs: WrappedResultSet): TestTry = TestTry(
    id = rs.string("test_try_id"),
    test = Test.withId(rs.bigInt("test_id")).get,
    user = User.withId(rs.bigInt("user_id")).get,
    startedTime = rs.timestamp("test_try_start").toLocalDateTime
  )

  def fromId(id: String): Option[TestTry] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT test_try_id, test_id, user_id, test_try_start
           FROM test_try
           WHERE test_try_id = $id
        """
        .map(rs => fromResultSet(rs))
        .single()
        .apply()
    }
  }
}