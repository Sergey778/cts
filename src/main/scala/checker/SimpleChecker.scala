package checker
import com.twitter.util.Future
import db._

object SimpleChecker extends Checker {
  override def check(testTry: TestTry): Future[Seq[TestTryAnswer]] = futurePool {
    testTry.answers.map {
      case e @ TestTryAnswer(_, question, Some(answer), _, _)
        if QuestionAnswer
          .fromQuestion(question)
          .exists(p => p.answer.toLowerCase == answer.toLowerCase) =>
        e.updateSystemGrade(Some(100))
      case e: TestTryAnswer => e.updateSystemGrade(Some(0))
    }
  }
}
