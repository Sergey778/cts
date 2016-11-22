package profile

import auth.UserFilter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import db.UserGroup
import util.Paths
import util.Paths.PathExtension
import util.UserContext.RequestAdditions

import scalatags.Text.all._

class UserGroupController extends Controller {
  filter[UserFilter].get(Paths.userGroups) { request: Request =>
    val src = html (
      scalatags.Text.tags.head (
        tag("title")("User Groups")
      ),
      body (
        div (
          a(href := Paths.userGroupsAll)("All user groups")
        ),
        div (
          a(href := Paths.userGroupsMy)("My user groups")
        ),
        div (
          a(href := Paths.userGroupsCreate)("Create user group")
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].get(Paths.userGroupsAll) { request: Request =>
    val src = html (
      scalatags.Text.tags.head (
        tag("title")("User Groups")
      ),
      body (
        div (
          UserGroup.all.map { group =>
            div (
              a(href := s"${group.id}")(group.fullName("/"))
            )
          }
        )
      )
    ).render
    response.ok.html(src)
  }

  def groupsHtml(groups: List[UserGroup], margin: Int = 0): List[ConcreteHtmlTag[String]] =
    groups.foldLeft(List[ConcreteHtmlTag[String]]()) { case (tagsList, group) =>
      val name = s"${(0 until margin).map(_ => "-").mkString}${group.name}"
      groupsHtml(group.childs, margin + 2) ::: option(value := s"${group.id}")(name) :: tagsList
    }.reverse

  filter[UserFilter].get(Paths.userGroupsCreate) { request: Request =>
    val src = html (
      scalatags.Text.tags.head (
        tag("title")("User Groups")
      ),
      body (
        div (
          form(method := "POST") (
            div (
              input(`type` := "text", id := "groupName", name := "groupName", placeholder := "Group name")
            ),
            div (
              select(id := "parentGroup", name := "parentGroup", placeholder := "Parent Group")(
                groupsHtml(UserGroup.all.filter(_.parentGroup.isEmpty)),
                option(value := "-1")("No parent group")
              )
            ),
            div (
              input(`type` := "submit")
            )
          )
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].post(Paths.userGroupsCreate) { request: Request =>
    val groupName = request.params.get("groupName")
    val parentGroup = request.params.get("parentGroup").flatMap(x => UserGroup.findById(BigInt(x)))
    groupName flatMap { name =>
      UserGroup.create(name, request.user, parentGroup)
    } map { group =>
      response.status(303).location(Paths.userGroups.element(group.id.toString))
    } getOrElse response.badRequest
  }
}
