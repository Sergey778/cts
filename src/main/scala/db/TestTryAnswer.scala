package db

import scalikejdbc._

case class TestTryAnswer(testTry: TestTry, question: Question, answer: Option[String], isCorrect: Option[Boolean]) {
  def updateAnswer(newAnswer: Option[String]): TestTryAnswer = DB localTx { implicit session =>
    val result = sql"""
         UPDATE test_try_answers
         SET answer = $newAnswer
         WHERE test_try_id = ${testTry.id} AND question_id = ${question.id}
       """
      .update()
      .apply()
    if (result > 0) TestTryAnswer(testTry, question, newAnswer, isCorrect) else this
  }

  def updateCorrectness(correct: Option[Boolean]): TestTryAnswer = DB localTx { implicit session =>
    val result =
      sql"""
         UPDATE test_try_answers
         SET is_correct = $correct
         WHERE test_try_id = ${testTry.id} AND question_id = ${question.id}
         """
      .update()
      .apply()
    if (result > 0) TestTryAnswer(testTry, question, answer, correct) else this
  }

}

object TestTryAnswer {
  def create(testTry: TestTry, question: Question): Option[TestTryAnswer] = DB localTx { implicit session =>
    val result = sql"""
         INSERT INTO test_try_answers (test_try_id, question_id)
         VALUES (${testTry.id}, ${question.id})
       """
      .update()
      .apply()
    if (result > 0) Some(TestTryAnswer(testTry, question, None, None)) else None
  }

  def fromTestTry(testTry: TestTry): List[TestTryAnswer] = DB readOnly { implicit session =>
    sql"SELECT test_try_id, question_id, answer, is_correct FROM test_try_answers WHERE test_try_id = ${testTry.id}"
      .map(rs => fromResultSet(rs, testTry))
      .list()
      .apply()
  }

  def fromResultSet(rs: WrappedResultSet, testTry: TestTry) = TestTryAnswer(
    testTry = testTry,
    question = Question.findById(rs.bigInt("question_id")).get,
    answer = rs.stringOpt("answer"),
    isCorrect = rs.booleanOpt("is_correct")
  )
}
