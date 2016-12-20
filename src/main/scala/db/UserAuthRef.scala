package db

import java.sql.Timestamp

import com.twitter.util.Future
import scalikejdbc.{ConnectionPool, DB, WrappedResultSet, _}
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
    Email.sendMessage(user.email, refType.subject, refType.body(s"$refStart?token=$reference"))

  def remove = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      sql"DELETE FROM user_auth_ref WHERE user_auth_ref = $reference"
        .update()
        .apply()
    }
  }
}

object UserAuthRef {
  def resultSetToAuthRef(rs: WrappedResultSet): UserAuthRef =
    resultSetToAuthRef(rs, User.withId(rs.bigInt("user_id")).get)

  def resultSetToAuthRef(rs: WrappedResultSet, user: User): UserAuthRef = UserAuthRef(
    user,
    rs.string("user_auth_ref"),
    AuthRefType(rs.string("user_auth_ref_type")),
    rs.timestamp("user_auth_ref_valid_until")
  )

  def forReference(token: String) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""SELECT user_id, user_auth_ref, user_auth_ref_type, user_auth_ref_valid_until
            FROM user_auth_ref
            WHERE user_auth_ref = $token
        """
        .map(x => resultSetToAuthRef(x))
        .single()
        .apply()
    }
  }
}