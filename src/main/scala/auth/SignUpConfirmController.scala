package auth

import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.QueryParam
import db.UserAuthRef


case class SignUpConfirmRequest(
                               @QueryParam token: String
                               )

class SignUpConfirmController extends Controller {
  get("/signupconfirm") { request: SignUpConfirmRequest =>
    UserAuthRef.forReference(request.token).map { ref =>
      ref.user.confirm
      ref.remove
      response.ok.body("Your account has been confirmed")
    } getOrElse response.internalServerError
  }
}
