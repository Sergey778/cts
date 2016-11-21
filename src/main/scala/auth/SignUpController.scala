package auth

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.FormParam
import db.{EmailConfirmation, User}
import util.Paths

case class SignUpRequest(
                        @FormParam `user_name`: String,
                        @FormParam `user_pass`: String,
                        @FormParam `user_email`: String
                        )

class SignUpController extends Controller {

  filter[GuestFilter].get(Paths.signUp) { request: Request =>
    response.ok.file("/html/signuppage.html")
  }

  filter[GuestFilter].post(Paths.signUp) { request: SignUpRequest =>
    if (User.findByName(request.`user_name`).nonEmpty) response.preconditionFailed("User already exists")
    else {
      User.create(request.`user_name`, request.`user_pass`, request.`user_email`) match {
        case Some(u) =>
          u.createAuthReference(EmailConfirmation).sendEmail("http://localhost:8888/signupconfirm")
          response.ok
        case None => response.internalServerError
      }
    }
  }

}
