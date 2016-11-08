package profile

import auth.UserFilter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.response.Mustache
import db.{Question, QuestionAnswer}
import util.UserContext.RequestAdditions

@Mustache("question_list")
case class QuestionListTemplate(names: List[QuestionListElement])

@Mustache("question_list")
case class QuestionListElement(id: String, name: String)

@Mustache("question_template")
case class AnswerListElement(answer: String, answerAuthor: String)

@Mustache("question_template")
case class QuestionTemplate(question: Question) {
  val questionName = question.text
  val groupRef = s"/profile/question-groups/${question.group.id}"
  val groupName = question.group.name
  val answers = QuestionAnswer
    .fromQuestion(question)
    .map(x => AnswerListElement(x.answer, x.creator.name))
  val questionId = question.id
}

@Mustache("add_answer_template")
case class AddAnswerTemplate(question: Question) {
  val questionName = question.text
}

class QuestionController extends Controller {
  filter[UserFilter].get("/profile/questions") { request: Request =>
    QuestionListTemplate(
      Question
        .findByCreator(request.user)
        .map(x => QuestionListElement(x.id.toString, x.text))
    )
  }

  filter[UserFilter].get("/profile/questions/:id") { request: Request =>
    request.params.get("id") flatMap { id =>
      Question.findById(BigInt(id))
    } map { question =>
      QuestionTemplate(question)
    }
  }

  filter[UserFilter].get("/profile/answers/add/:id") { request: Request =>
    request.params.get("id") flatMap { id =>
      Question.findById(BigInt(id))
    } map { question =>
      AddAnswerTemplate(question)
    } getOrElse response.badRequest
  }

  filter[UserFilter].post("/profile/answers/add/:id") { request: Request =>
    request.params.get("id") flatMap { id =>
      Question.findById(BigInt(id))
    } map { question =>
      QuestionAnswer.create(question, request.user, request.params.getOrElse("answer", ""))
    } map { answer =>
      response.temporaryRedirect.location(s"/profile/questions/${request.params("id")}")
    } getOrElse response.badRequest
  }

}
