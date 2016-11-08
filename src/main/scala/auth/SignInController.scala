package auth

import java.util.Calendar

import com.twitter.finagle.http.{Cookie, Request, Response, Status}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.FormParam
import com.twitter.util.Duration
import db.User

case class SignInFormRequest (
                      @FormParam `user_name`: String,
                      @FormParam `user_password`: String
                      )

class SignInController extends Controller {
  filter[GuestFilter].get("/signin") { request: Request =>
    response.ok.file("/html/signinpage.html")
  }

  filter[GuestFilter].post("/signin") { request: SignInFormRequest =>
    User.findByName(request.`user_name`)
      .filter(_.isCorrectPassword(request.`user_password`))
      .flatMap(x => okWithToken(x))
      .getOrElse(response.unauthorized)
  }

  filter[UserFilter].get("/signout") { request: Request =>
    val response = Response(Status.TemporaryRedirect)
    response.location = "/signin"
    response.removeCookie("access_token")
    response
  }

  protected def okWithToken(user: User) = user.createAccessToken map { token =>
    val cookie = new Cookie("access_token", token.token)
    cookie.maxAge = Duration.fromMilliseconds(token.validUntil.getTime - Calendar.getInstance().getTime.getTime)
    response.ok("It's ok").cookie(cookie)
  }
}
