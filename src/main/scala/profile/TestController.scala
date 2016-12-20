package profile

import auth.UserFilter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import db._
import util.UserContext.RequestAdditions

import scala.util.Random
import scalatags.Text.all._


class TestController extends Controller {
  filter[UserFilter].get("/profile/tests") { request: Request =>
    val tests = Test.fromCreator(request.user)
    val src = html(
      scalatags.Text.all.head(
        tag("title")("CTS-Profile")
      ),
      body(
        div(
          h1("Tests"),
          tests.map(x => ul(a(href:=s"/profile/tests/${x.id}")(x.name)))
        ),
        div(
          a(href := "/profile/tests/add")(
            "Add test"
          )
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].get("/profile/tests/add") { request: Request =>
    val src = html (
      scalatags.Text.all.head(
        tag("title")("Add test")
      ),
      body (
        div (
          h1("Add Test")
        ),
        div (
          form(method := "POST")(
            div (
              input(id := "testName", name := "testName", `type` := "text", placeholder := "Test Name")
            ),
            div (
              input(`type` := "submit", value := "Create test")
            )
          )
        )
      )
    ).render
    response.ok.html(src)
  }

  filter[UserFilter].post("/profile/tests/add") { request: Request =>
    request.params.get("testName") map { testName =>
      Test.create(testName, request.user)
    } map { _ =>
      response.ok.location("/profile/tests")
    } getOrElse response.badRequest
  }

  filter[UserFilter].get("/profile/tests/:id") { request: Request =>
    request.params.get("id").map { id =>
      Test.fromId(BigInt(id))
    } map {
      case Some(test) =>
        val src = html (
          scalatags.Text.all.head (
            tag("title")("Test")
          ),
          body (
            div (
              h1 (
                test.name + " test"
              )
            ),
            div(a(href:=s"/profile/tests/addquestion/${test.id}")("Add question")),
            div(a(href:=s"/profile/tests/addqgroup/${test.id}")("Add question group"))
          )
        ).render
        response.ok.html(src)
      case None => response.badRequest
    }
  }

  filter[UserFilter].get("/profile/tests/addqgroup/:id") { request: Request =>
    request.params.get("id").map { id =>
      Test.fromId(BigInt(id))
    } map {
      case Some(test) =>
        val groups = QuestionGroup.withCreator(request.user)
        val src = html (
          scalatags.Text.all.head (
            tag("title")("Add question")
          ),
          body (
            div (
              h1 (
                test.name + "/Add question"
              )
            ),
            div (
              form(method := "POST") (
                select(id := "selectedQuestion", name := "selectedName")(
                  groupsHtml(groups)
                ),
                input(`type` := "number",
                  min := "0",
                  name := "questionCount",
                  id := "questionCount", placeholder := "Questions from this category"
                ),
                input(`type` := "submit", value := "Add")
              )
            )
          )
        ).render
        response.ok.html(src)
      case None => response.badRequest
    } getOrElse response.badRequest
  }

  def groupsHtml(groups: List[QuestionGroup], margin: Int = 0): List[ConcreteHtmlTag[String]] =
    groups.foldLeft(List[ConcreteHtmlTag[String]]()) { case (tagsList, group) =>
      val name = s"${(0 until margin).map(_ => "-").mkString}${group.name}(${group.questionsCount.getOrElse(0)})"
      groupsHtml(group.childs, margin + 2) ::: option(value := s"${group.id}")(name) :: tagsList
  }.reverse

  filter[UserFilter].post("/profile/tests/addqgroup/:id") { request: Request =>
    val testParam = request.params.get("id").flatMap(x => Test.fromId(BigInt(x)))
    val groupParam = request.params.get("selectedName").flatMap(x => QuestionGroup.withId(BigInt(x)))
    val count = request.params.get("questionCount").map(x => x.toInt).getOrElse(-1)
    val result = (testParam, groupParam) match {
      case (Some(test), Some(group)) =>
        test
          .addQuestionGroup(group, count)
          .map(_ => response.ok.html("Group added"))
      case _ => None
    }
    result getOrElse response.badRequest.html("Error")
  }

  filter[UserFilter].get("/profile/tests/:*") { request: Request =>
    val Array(testId, endPath, _*) = request.params("*").split("/")
    Test.fromId(BigInt(testId)).map { test =>
      if (endPath == "taketry") formTest(test, request.user)
      else response.badRequest
    } getOrElse response.badRequest
  }

  def formTest(test: Test, user: User) = {
    val questions = Random.shuffle(test.questions).distinct
    val attempt = TestTry.create(test, user).flatMap(a => a.createAnswers(questions))
    if (attempt.isEmpty) {
      response.badRequest
    } else {
      val src =
        html(
          scalatags.Text.all.head(
            tag("title")("Add question")
          ),
          body(
            div(
              h1("Test try")
            ),
            form(method := "POST", action := s"/testtry&id=${attempt.get.id}")(
              questions.map { question =>
                div(
                  div(
                    input(
                      `type` := "text",
                      placeholder := question.text,
                      name := s"question-${question.id.toString}",
                      id := s"question-${question.id.toString}"
                    )
                  )
                )
              },
              div(
                input(`type` := "submit", value := "Check It!")
              )
            )
          )
        ).render

      response.ok.html(src)
    }
  }
}
