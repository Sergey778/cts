package checker
import com.twitter.util.Future
import db.{Question, QuestionAnswer, TestTry}

object SimpleChecker extends Checker {
  override def check(testTry: TestTry): Future[Map[Question, Boolean]] = futurePool {
    testTry.answers.map {
      case (question, Some(answer)) =>
        question -> QuestionAnswer
          .fromQuestion(question)
          .exists(qa => qa.answer.toLowerCase == answer.toLowerCase)
      case (question, _) => question -> false
    }
  }
}
