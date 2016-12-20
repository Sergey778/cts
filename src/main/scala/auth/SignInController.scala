package auth

import java.util.Calendar

import _root_.util.Paths
import com.twitter.finagle.http._
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.FormParam
import com.twitter.util.Duration
import db.User

case class SignInFormRequest (
                      @FormParam `user_name`: String,
                      @FormParam `user_password`: String
                      )

class SignInController extends Controller {
  filter[GuestFilter].get(Paths.signIn) { request: Request =>
    response.ok.file("/html/signinpage.html")
  }

  filter[GuestFilter].post(Paths.signIn) { request: SignInFormRequest =>
    User.withName(request.`user_name`)
      .filter(_.isCorrectPassword(request.`user_password`))
      .flatMap(x => okWithToken(x))
      .getOrElse(response.unauthorized)
  }

  filter[UserFilter].get(Paths.signOut) { request: Request =>
    val response = Response(Status(303))
    val cookie = request.cookies("access_token")
    cookie.maxAge = Duration.fromMilliseconds(-1)
    response.location = Paths.signIn
    response.cookies.add(cookie)
    response
  }

  protected def okWithToken(user: User) = user.createAccessToken map { token =>
    val cookie = new Cookie("access_token", token.token)
    cookie.maxAge = Duration.fromMilliseconds(token.validUntil.getTime - Calendar.getInstance().getTime.getTime)
    response.ok("It's ok").cookie(cookie)
  }
}
