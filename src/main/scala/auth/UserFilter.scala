package auth

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import db.UserAuthToken
import util.{Paths, UserContext}

/**
  * Created by Sergey on 07.11.16.
  */
class UserFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] =
    request.cookies.get("access_token") flatMap { cookie =>
      UserAuthToken.forToken(cookie.value)
    } map { authToken =>
      service(UserContext.assignUser(request, authToken.user))
    } getOrElse relocationResponse

  private lazy val relocationResponse = {
    val response = Response(Status.TemporaryRedirect)
    response.location = Paths.signIn
    Future.value(response)
  }
}
