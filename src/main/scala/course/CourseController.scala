package course

import auth.UserFilter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import db.{Course, Test, UserGroup}
import util.Paths
import util.Paths.PathExtension
import util.UserContext.RequestAdditions

import scalatags.Text.all._

class CourseController extends Controller {

  filter[UserFilter].get(Paths.courses) { request: Request =>
    val src = html (
      scalatags.Text.tags.head (
        tag("title")("User Groups")
      ),
      body (
        div(
          h1("Created courses:")
        ),
        Course.withCreator(request.user).map { course =>
          div(a(href := Paths.courses.element(course.id.toString))(course.name))
        },
        div (
          h1("Available courses:")
        ),
        Course.availableForUser(request.user).map { course =>
          div(a(href := Paths.courses.element(course.id.toString))(course.name))
        },
        div (
          a(href := Paths.coursesCreate)("Create course")
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].get(Paths.coursesCreate) { request: Request =>
    val src = html (
      scalatags.Text.tags.head (
        tag("title")("User Groups")
      ),
      body (
        div (
          form(method := "POST") (
            input(`type` := "input", placeholder := "Course Name", name := "courseName", id := "courseName"),
            input(
              `type` := "input",
              placeholder := "Course Description",
              name := "courseDescription",
              id := "courseDescription"
            ),
            input(`type` := "submit", value := "Create course")
          )
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].post(Paths.coursesCreate) { request: Request =>
    val courseName = request.params.get("courseName")
    val courseDescription = request.params.get("courseDescription")
    val creator = request.user
    val result = (courseName, courseDescription) match {
      case (Some(name), Some(description)) => Course.create(name, description, creator)
      case _ => None
    }
    result.map(x => response.ok.html("Course created")).getOrElse(response.badRequest)
  }

  filter[UserFilter].get(Paths.courses.wildcard("id")) { request: Request =>
    val courseRef = request.params.get("id").flatMap(id => Course.withId(BigInt(id)))
    def htmlCode(course: Course) = html (
      scalatags.Text.tags.head (
        tag("title")("User Groups")
      ),
      body (
        div (
          h1(s"Course: ${course.name}"),
          div (
            h2("User groups:"),
            div(
              course.userGroups.map(x => div(p(x.fullName())))
            )
          ),
          div (
            h2("Tests:"),
            div (
              course.tests.map(x => div(p(x.name)))
            )
          ),
          if (course.creator == request.user) {
            div (
              div(
                a(href := Paths.coursesAddUserGroup.element(s"${course.id}"))("Add user group")
              ),
              div(
                a(href := Paths.coursesAddTest.element(s"${course.id}"))("Add test")
              )
            )
          } else ""
        )
      )
    ).render
    courseRef.map(c => response.ok.html(htmlCode(c))).getOrElse(response.badRequest)
  }

  filter[UserFilter].get(Paths.coursesAddUserGroup.wildcard("id")) { request: Request =>
    val src = html (
      scalatags.Text.tags.head (
        tag("title")("Add course User Group")
      ),
      body (
        div (
          form(method := "POST") (
            div (
              select(name := "selectedGroup", id := "selectedGroup", placeholder := "Choose Group") (
                groupsHtml(UserGroup.all.filter(p => p.parentGroup.isEmpty))
              ),
              input(`type` := "submit", value := "Add Group")
            )
          )
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].post(Paths.coursesAddUserGroup.wildcard("id")) { request: Request =>
    val course = request.params.get("id").flatMap(x => Course.withId(BigInt(x)))
    val group = request.params.get("selectedGroup").flatMap(x => UserGroup.findById(BigInt(x)))
    val result = (course, group) match {
      case (Some(c), Some(g)) => c.addUserGroup(g)
      case _ => None
    }
    result.map(x => response.ok.html("Group added")).getOrElse(response.badRequest)
  }

  filter[UserFilter].get(Paths.coursesAddTest.wildcard("id")) { request: Request =>
    val src = html (
      scalatags.Text.tags.head (
        tag("title")("Add course User Group")
      ),
      body (
        div (
          form(method := "POST") (
            div (
              select(name := "selectedTest", id := "selectedTest", placeholder := "Choose Test") (
                Test.withCreator(request.user).map(test => option(value := s"${test.id}")(s"${test.name}"))
              ),
              input(`type` := "submit", value := "Add Test")
            )
          )
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].post(Paths.coursesAddTest.wildcard("id")) { request: Request =>
    val course = request.params.get("id").flatMap(x => Course.withId(BigInt(x)))
    val test = request.params.get("selectedTest").flatMap(x => Test.withId(BigInt(x)))
    val result = (course, test) match {
      case (Some(c), Some(t)) => c.addTest(t)
      case _ => None
    }
    result.map(x => response.ok.html("Test added")).getOrElse(response.badRequest)
  }

  def groupsHtml(groups: List[UserGroup], margin: Int = 0): List[ConcreteHtmlTag[String]] =
    groups.foldLeft(List[ConcreteHtmlTag[String]]()) { case (tagsList, group) =>
      val name = s"${(0 until margin).map(_ => "-").mkString}${group.name}"
      groupsHtml(group.childs, margin + 2) ::: option(value := s"${group.id}")(name) :: tagsList
    }.reverse

}
