package auth

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.FormParam
import db.User

case class SignInFormRequest (
                      @FormParam `user_name`: String,
                      @FormParam `user_password`: String
                      )

class SignInController extends Controller {
  get("/signin") { request: Request =>
    response.ok.file("/html/signinpage.html")
  }

  post("/signin") { request: SignInFormRequest =>
    User.forName(request.`user_name`)
      .filter(_.isCorrectPassword(request.`user_password`))
      .flatMap(x => okWithToken(x))
      .getOrElse(response.unauthorized)
  }

  protected def okWithToken(user: User) = user.createAccessToken map { token =>
      response.ok("It's ok").cookie("access_token", token)
  }
}
