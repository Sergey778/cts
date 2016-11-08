package profile

import auth.UserFilter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.response.Mustache
import db.Question
import util.UserContext.RequestAdditions

@Mustache("question_list")
case class QuestionListTemplate(names: List[QuestionListElement])

@Mustache("question_list")
case class QuestionListElement(id: String, name: String)

@Mustache("question_template")
case class QuestionTemplate(question: Question) {
  val questionName = question.text
  val groupRef = s"/profile/question-groups/${question.group.id}"
  val groupName = question.group.name
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

}
