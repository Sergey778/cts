package profile

import auth.UserFilter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.response.Mustache
import util.Paths
import util.UserContext.RequestAdditions

@Mustache("profile")
case class ProfileTemplate(name: String)

class ProfileController extends Controller {

  filter[UserFilter].get(Paths.profile) { request: Request =>
    ProfileTemplate(request.user.name)
  }

}
