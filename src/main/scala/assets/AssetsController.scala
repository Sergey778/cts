package assets

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

/**
  * @author Sergey Gerasimov
  * Controller that responses with requested resource.
  * URL must start with "resources". Than comes path to file(starting from classpath).
  * If file does not exist than Not Found status returned.
  *
  */
class AssetsController extends Controller {

  get("/resources/:*") { request: Request =>
    request.params.get("*") match {
      case Some(path) => response.ok.file(path)
      case None => response.notFound
    }
  }

}
