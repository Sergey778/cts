package profile

import auth.UserFilter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import db.{User, UserGroup}
import util.Paths.PathExtension
import util.UserContext.RequestAdditions
import util.{Email, Paths}

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

  filter[UserFilter].get(Paths.userGroupsAcceptInvite.wildcard("id")) { request: Request =>
    request.params.get("id") flatMap { groupId =>
      UserGroup.findById(BigInt(groupId))
    } flatMap { group =>
      val user =
        if (request.user == group.leader)
          request.params.get("userId").flatMap(x => User.findById(BigInt(x)))
        else
          Some(request.user)
      user.map(user => group.makeFullMember(user)).filter(p => p).map(x => response.ok.html("You are in group!"))
    } getOrElse response.badRequest
  }

  filter[UserFilter].get(Paths.userGroups.wildcard("id")) { request: Request =>
    request.params.get("id") flatMap { groupId =>
      UserGroup.findById(BigInt(groupId))
    } map { group =>
      val src = html (
        scalatags.Text.tags.head (
          tag("title")("User Groups")
        ),
        body (
          div (
            p(group.name),
            if (request.user.isMemberOfGroup(group)) ""
            else a(href := Paths.userGroupsInviteSend.element(group.id.toString()))("Apply"),
            if (request.user == group.leader)
              a(href := Paths.userGroupsInviteUser.element(group.id.toString()))("Invite user")
            else
              ""
          )
        )
      ).render
      response.ok.html(src)
    } getOrElse response.badRequest
  }

  filter[UserFilter].get(Paths.userGroupsInviteSend.wildcard("id")) { request: Request =>
    request.params.get("id") flatMap { groupId =>
      UserGroup.findById(BigInt(groupId))
    } flatMap { group =>
      group.addMember(request.user)
    } map { group =>
      sendInviteRequest(request.user, group)
      response.ok.html("Request was send")
    } getOrElse response.badRequest()
  }

  filter[UserFilter].get(Paths.userGroupsInviteUser.wildcard("id")) { request: Request =>
    val src = html (
      scalatags.Text.all.head (
        tag("title")("Invite user")
      ),
      body (
        form(method := "POST") (
          input(`type` := "input", placeholder := "User name or email", name := "userName", id := "userName"),
          input(`type` := "submit", value := "Invite!")
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].post(Paths.userGroupsInviteUser.wildcard("id")) { request: Request =>
    request.params.get("id") flatMap { groupId =>
      UserGroup.findById(BigInt(groupId))
    } filter { group =>
      group.leader == request.user
    } flatMap { group =>
      request.params.get("userName")
        .flatMap(x => User.findByName(x) orElse User.findByEmail(x))
        .map(x => (x, group.addMember(x)))
    } flatMap { case (user, group) =>
      group.map { g =>
        sendInvite(user, g)
      }
    } map { _ =>
      response.ok.html("Invite sended")
    } getOrElse response.badRequest
  }

  filter[UserFilter].get(Paths.userGroupsMy) { request: Request =>
    val groups = request.user.groups
    val (leader, other) = groups.partition(g => g.leader == request.user)
    val src = html (
      scalatags.Text.all.head (
        tag("title")("My user groups")
      ),
      body (
        div (
          "Managed groups:",
          leader.map(x => ul(a(href := Paths.userGroups.element(x.id.toString()))(x.name)))
        ),
        div (
          "Just member:",
          other.map(x => ul(a(href := Paths.userGroups.element(x.id.toString()))(x.name)))
        )
      )
    ).render
    response.ok.html(src)
  }

  def sendInvite(to: User, group: UserGroup) = {
    val text = s"""
      User ${group.leader.name} invite you to his group ${group.fullName()}
      To accept request follow this link: ${Paths.domain + Paths.userGroupsAcceptInvite.element(group.id.toString())}
    """
    Email.sendMessage(to.email, "Invite to group!", text)
  }

  def sendInviteRequest(from: User, group: UserGroup) = {
    val text = s"""
      User ${from.name} request you to accept him to your group ${group.fullName()}
      To accept request follow this link: ${Paths.domain + Paths.userGroupsAcceptInvite.element(group.id.toString())}
    """
    Email.sendMessage(group.leader.email, "Request for group membership", text)
  }
}
