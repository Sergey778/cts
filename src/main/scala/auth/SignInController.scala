package auth

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import db.User

class SignInController extends Controller {
  get("/signin") { request: Request =>
    response.ok.file("/html/signinpage.html")
  }

  post("/signin") { request: Request =>
    User(request.params)
      .filter(x => x.isCorrectPassword(request.params))
      .flatMap(x => okWithToken(x))
      .getOrElse(response.unauthorized)
  }

  protected def okWithToken(user: User) = user.createAccessToken map { token =>
      response.ok("It's ok").cookie("access_token", token)
  }
}
