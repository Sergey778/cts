package profile

import auth.UserFilter
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import db.{Question, TestTry}

object TestTryContext {
  private val testTryField = Request.Schema.newField[TestTry]
  private val userAnswersField = Request.Schema.newField[Map[Question, Option[String]]]

  implicit class RequestAdditions(val request: Request) extends AnyVal {
    def testTry: TestTry = request.ctx(testTryField)
    def userAnswers: Map[Question, Option[String]] = request.ctx(userAnswersField)
  }

  def assign(request: Request, testTry: TestTry, answers: Map[Question, Option[String]]): Request = {
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
      val answers = testTry.answers.map { case (q, _) =>
        q -> request.params.get(s"question-${q.id.toString}")
      }
      TestTryContext.assign(request, testTry, answers)
    }
}

class TestTryController extends Controller {
  import TestTryContext.RequestAdditions
  filter[UserFilter].filter[TestTryFilter].post("/testtry&id=:id") { request: Request =>
    request.userAnswers.foreach {
      case (question, Some(answer)) => request.testTry.updateAnswer(question, answer)
      case _ => ()
    }
    //Checker.startCheck(request.testTry)
    response.ok.html("Your answers are gonna be checked in 10 minutes. Take cup of coffee and come back for results")
  }

  filter[UserFilter].filter[TestTryFilter].get("/testtry&id=:id") { request: Request =>
    response.ok.html("no results yet...")
  }
}
