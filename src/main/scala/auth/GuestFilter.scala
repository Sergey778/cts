package auth

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import db.UserAuthToken
import util.Paths

/**
  * Created by Sergey on 08.11.16.
  */
class GuestFilter extends SimpleFilter[Request, Response]{
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val token = request.cookies.get("access_token").map(c => c.value).getOrElse("")
    if (UserAuthToken.withToken(token).nonEmpty) redirectResponse
    else service(request)
  }

  private lazy val redirectResponse = {
    val response = Response(Status.TemporaryRedirect)
    response.location = Paths.profile
    Future.value(response)
  }
}
