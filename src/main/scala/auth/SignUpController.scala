package auth

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.FormParam
import db.{EmailConfirmation, User}

case class SignUpRequest(
                        @FormParam `user_name`: String,
                        @FormParam `user_pass`: String,
                        @FormParam `user_email`: String
                        )

class SignUpController extends Controller {

  get("/signup") { request: Request =>
    response.ok.file("/html/signuppage.html")
  }

  post("/signup") { request: SignUpRequest =>
    if (User.forName(request.`user_name`).nonEmpty) response.preconditionFailed("User already exists")
    else {
      User.createUser(request.`user_name`, request.`user_pass`, request.`user_email`) match {
        case Some(u) =>
          u.createAuthReference(EmailConfirmation).sendEmail("http://localhost:8888/signupconfirm")
          response.ok
        case None => response.internalServerError
      }
    }
  }

}
