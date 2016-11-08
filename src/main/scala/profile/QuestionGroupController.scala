package profile

import auth.UserFilter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.response.Mustache
import db.QuestionGroup
import util.UserContext.RequestAdditions
import util.templates.{HierarchyList, HierarchyListElement, HierarchySelect, HierarchySelectElement}

@Mustache("profile_question_groups_list")
case class GroupsList(override val list: List[HierarchyListElement]) extends HierarchyList

@Mustache("profile_create_question_group")
case class CreateQuestionGroup(override val list: List[HierarchySelectElement]) extends HierarchySelect

class QuestionGroupController extends Controller {

  def questionGroupToQGroup(t: List[QuestionGroup]): List[HierarchyListElement] = {
    if (t.isEmpty) List()
    else t.map(x => HierarchyListElement(x.name, questionGroupToQGroup(x.childs)))
  }

  def questionGroupToQGroup2(t: List[QuestionGroup], margin: Int = 0): List[HierarchySelectElement] = {
    if (t.isEmpty) List()
    else t.map(x => HierarchySelectElement(x.id.toString, x.name, questionGroupToQGroup2(x.childs, margin + 2), margin))
  }

  filter[UserFilter].get("/profile/question-groups") { request: Request =>
    GroupsList(questionGroupToQGroup(QuestionGroup.findByUser(request.user)))
  }

  filter[UserFilter].get("/profile/question-groups/create") { request: Request =>
    CreateQuestionGroup(questionGroupToQGroup2(QuestionGroup.findByUser(request.user)))
  }

  filter[UserFilter].post("/profile/question-groups/create") { request: Request =>
    val creator = request.user
    val name = request.params.get("name")
    val parentId = request.params.get("id").map(x => BigInt(x))
    val t = (name, parentId) match {
      case (Some(n), Some(id)) => QuestionGroup.create(n, creator, QuestionGroup.findById(id))
      case _ => None
    }
    t map { questionGroup =>
      response.ok.html("<body>It's okay!</body>")
    } getOrElse response.badRequest
  }

}
