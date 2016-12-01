package runner

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter

class Server extends HttpServer {

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .filter[ResponseTimeFilter]
      .add[assets.AssetsController]
      .add[auth.SignInController]
      .add[auth.SignUpController]
      .add[auth.SignUpConfirmController]
      .add[profile.ProfileController]
      .add[profile.QuestionGroupController]
      .add[profile.QuestionController]
      .add[profile.TestController]
      .add[profile.UserGroupController]
      .add[profile.TestTryController]
      .add[course.CourseController]
  }

}
