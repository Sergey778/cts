package runner

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.logging.Logger
import com.twitter.util.Future

class ResponseTimeFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val start = System.currentTimeMillis()
    val response = service(request)
    val finish = System.currentTimeMillis()
    Logger.get.info(s"Response for $request took ${finish - start}ms")
    request.path
    response
  }
}
