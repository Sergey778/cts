package auth

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import db.Users

class SignInController extends Controller {
  get("/signin") { request: Request =>
    response.ok.file("/html/signinpage.html")
  }

  post("/signin") { request: Request =>
    val credentials = (request.params.get("loginName"), request.params.get("loginPass"))
    credentials match {
      case (Some(user), Some(pass)) if Users.checkPassword(user, pass) => okWithToken(user)
      case _ => response.unauthorized
    }
  }

  protected def okWithToken(username: String) = {
    Users.createToken(username).map { token =>
      response.ok("It's ok").cookie("access_token", token)
    } getOrElse {
      response.unauthorized
    }
  }
}
