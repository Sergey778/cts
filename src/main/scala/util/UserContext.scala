package util

import com.twitter.finagle.http.Request
import db.User

/**
  * Created by Sergey on 07.11.16.
  */
object UserContext {
  private val userField = Request.Schema.newField[User]()

  implicit class RequestAdditions(val request: Request) extends AnyVal {
    def user = request.ctx(userField)
  }

  def assignUser(req: Request, user: User): Request = {
    req.ctx.update(userField, user)
    req
  }
}
