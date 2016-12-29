package profile

import auth.UserFilter
import checker.TomitaChecker
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import db._

import scalatags.Text.all._

object TestTryContext {
  private val testTryField = Request.Schema.newField[TestTry]
  private val userAnswersField = Request.Schema.newField[List[TestTryAnswer]]

  implicit class RequestAdditions(val request: Request) extends AnyVal {
    def testTry: TestTry = request.ctx(testTryField)
    def userAnswers: List[TestTryAnswer] = request.ctx(userAnswersField)
  }

  def assign(request: Request, testTry: TestTry, answers: List[TestTryAnswer]): Request = {
    request.ctx.update(testTryField, testTry).update(userAnswersField, answers)
    request
  }
}

class TestTryFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = assign(request) match {
    case Some(req: Request) => service(req)
    case None => Future(Response(Status.BadRequest))
  }

  def assign(request: Request): Option[Request] =
    for {
      id <- request.params.get("id")
      testTry: TestTry <- TestTry.fromId(id)
    } yield {
      val answers = testTry.answers.map { case TestTryAnswer(idd, q, _, sg, tg) =>
        TestTryAnswer(idd, q, request.params.get(s"question-${q.id.toString}"), sg, tg)
      }
      TestTryContext.assign(request, testTry, answers)
    }
}

class TestTryController extends Controller {
  import TestTryContext.RequestAdditions

  private var attemptsChecks = scala.collection.immutable.HashMap[TestTry, Map[Question, Boolean]]()

  filter[UserFilter].filter[TestTryFilter].post("/testtry&id=:id") { request: Request =>
    request.userAnswers.foreach {
      case e @ TestTryAnswer(_, question, answer, _, _) => e.updateAnswer(answer)
    }
    TomitaChecker
      .check(request.testTry)
    response.ok.html("Your answers are gonna be checked in 10 minutes. Take cup of coffee and come back for results")
  }

  filter[UserFilter].filter[TestTryFilter].get("/testtry&id=:id") { request: Request =>
      val src = html(
        scalatags.Text.all.head(
          tag("title")("Test attempt")
        ),
        body(
          h1("Results:"),
          div (
            request.testTry.answers.map { case TestTryAnswer(_, question, answer, grade, _) =>
              div(
                p(question.text),
                div(
                  p(s"Your answer: ${answer.getOrElse("-")}")
                ),
                div(
                  p(s"Teacher answer: ${QuestionAnswer.fromQuestion(question).headOption.map(_.answer).getOrElse("-")}")
                ),
                div(
                  p(s"Correct: ${grade.getOrElse(0)}")
                )
              )
            }.toList
          )
        )
      ).render
      response.ok.html(src)
  }
}
