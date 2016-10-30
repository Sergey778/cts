package db

import java.sql.Timestamp

import com.twitter.util.Future
import scalikejdbc.WrappedResultSet
import util.Email

sealed trait AuthRefType {

  override def toString: String = this match {
    case EmailConfirmation => "C"
    case PasswordReset => "R"
  }

  def subject: String = this match {
    case EmailConfirmation => "Sign up confirmation"
    case PasswordReset => "Password reset"
  }

  def body(link: String): String = this match {
    case EmailConfirmation =>
      s"""
        |Hey, thank you for sign up!
        |To finish sign up, please click follow this link: $link
        |Sincerely, your CTS team!
      """.stripMargin
    case PasswordReset =>
      s"""
         | Hello.
         | To reset your password follow this link: $link
         | Sincerely, your CTS team!
       """.stripMargin
  }
}

object AuthRefType {
  def apply(s: String) = {
    if (s == "R") PasswordReset
    else EmailConfirmation
  }
}

case object EmailConfirmation extends AuthRefType
case object PasswordReset extends AuthRefType

case class UserAuthRef(user: User, reference: String, refType: AuthRefType, validUntil: Timestamp) {
  def sendEmail(refStart: String): Future[Unit] =
    Email.sendMessage(user.email, refType.subject, refType.body(refStart + reference))
}

object UserAuthRef {
  def resultSetToAuthRef(rs: WrappedResultSet): UserAuthRef =
    resultSetToAuthRef(rs, User.forId(rs.bigInt("user_id")).get)

  def resultSetToAuthRef(rs: WrappedResultSet, user: User): UserAuthRef = UserAuthRef(
    user,
    rs.string("user_auth_ref"),
    AuthRefType(rs.string("user_auth_ref_type")),
    rs.timestamp("user_auth_ref_valid_until")
  )
}