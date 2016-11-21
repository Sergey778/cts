package auth

import com.twitter.finagle.http.{MediaType, Request}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.QueryParam
import db.UserAuthRef
import util.Paths


case class SignUpConfirmRequest(
                               @QueryParam token: String
                               )

class SignUpConfirmController extends Controller {
  get(Paths.signUpConfirmation) { request: SignUpConfirmRequest =>
    UserAuthRef.forReference(request.token).map { ref =>
      ref.user.confirm
      ref.remove
      response.ok("<body>Your account has been confirmed</body>").contentType(MediaType.Html)
    } getOrElse response.internalServerError.body("<body>Server error</body>").contentType(MediaType.Html)
  }

  get("/signupconfirminfo") { request: Request =>
    response.ok.body("<body>Confirmation email has been sent. Please check it.</body>").contentType(MediaType.Html)
  }
}
