package db

import java.time.LocalDateTime
import java.util.UUID

import com.twitter.util.Future
import db.core.{Table, TableObject, TimeHolder}
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

case class UserAuthRef(user: User, reference: String, refType: AuthRefType, validUntil: LocalDateTime) extends Table {
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

object UserAuthRef extends TableObject[UserAuthRef] with TimeHolder {

  override def name: String = "user_auth_ref"

  private final val $user = "user"
  private final val $reference = "reference"
  private final val $refType = "refType"
  private final val $validUntil = "validUntil"

  override def columns: Map[String, String] = Map (
    $user -> "user_id",
    $reference -> "user_auth_ref",
    $refType -> "user_auth_ref_type",
    $validUntil -> "user_auth_ref_valid_until"
  )

  def fromResultSet(rs: WrappedResultSet,
                    user: Option[User] = None,
                    reference: Option[String] = None,
                    refType: Option[AuthRefType] = None,
                    validUntil: Option[LocalDateTime] = None) = UserAuthRef(
    user = user.orElse(User.withId(rs.bigInt(columns($user)))).get,
    reference = reference.getOrElse(rs.string(columns($reference))),
    refType = refType.getOrElse(AuthRefType(rs.string(columns($refType)))),
    validUntil = validUntil.getOrElse(rs.timestamp(columns($validUntil)).toLocalDateTime)
  )

  override def fromResultSet(rs: WrappedResultSet): UserAuthRef = fromResultSet(rs, None, None, None, None)

  def withReference(token: String): Option[UserAuthRef] = whereOption($reference -> token)
  def withUser(user: User): List[UserAuthRef] = whereList($user -> user.id)
  def withUserAndValid(user: User): List[UserAuthRef] =
    withUser(user)
      .filter(p => p.validUntil.isAfter(LocalDateTime.now()))

  def create(user: User, refType: AuthRefType): Option[UserAuthRef] = {
    val uuid = UUID.randomUUID().toString
    val validUntil = currentTime.plusDays(1)
    val result = DB localTx { implicit session =>
      insertSql(
        $user -> user.id,
        $reference -> uuid,
        $refType -> refType.toString,
        $validUntil -> validUntil
      )
        .update()
        .apply()
    }
    if (result > 0) Some(UserAuthRef(user, uuid, refType, validUntil)) else None
  }
}