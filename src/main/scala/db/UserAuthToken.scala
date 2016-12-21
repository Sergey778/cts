package db

import java.time.LocalDateTime

import db.core.{Table, TableObject, TimeHolder}
import scalikejdbc._

/**
  * Created by Sergey on 07.11.16.
  */
case class UserAuthToken(user: User, token: String, validUntil: LocalDateTime) extends Table

object UserAuthToken extends TableObject[UserAuthToken] with TimeHolder {
  override def name: String = "user_token"

  private final val $user = "user"
  private final val $token = "token"
  private final val $validUntil = "validUntil"

  override def columns: Map[String, String] = Map (
    $user -> "user_id",
    $token -> "user_token_value",
    $validUntil -> "user_token_valid_until"
  )

  def withToken(token: String): Option[UserAuthToken] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
            SELECT
              u.user_name user_name,
              u.user_id   user_id,
              u.user_confirmed user_confirmed,
              u.user_email user_email,
              u.user_password user_password,
              u.user_signup_time user_signup_time,
              ut.user_token_valid_until user_token_valid_until,
              ut.user_token_value user_token_value
            FROM user_token ut
            JOIN "user" u ON ut.user_id = u.user_id
            WHERE ut.user_token_value = $token AND ut.user_token_valid_until > now()
         """
        .map(x => fromResultSet(x))
        .single()
        .apply()
    }
  }

  def withUser(user: User): List[UserAuthToken] =
    whereList($user -> user.id)
      .filter(p => p.validUntil.isAfter(LocalDateTime.now()))

  def fromResultSet(rs: WrappedResultSet,
                    user: Option[User] = None,
                    token: Option[String] = None,
                    validUntil: Option[LocalDateTime] = None): UserAuthToken = UserAuthToken(
    user = user.orElse(User.withId(rs.bigInt(columns($user)))).get,
    token = token.getOrElse(rs.string(columns($token))),
    validUntil = validUntil.getOrElse(rs.timestamp(columns($validUntil)).toLocalDateTime)
  )

  override def fromResultSet(rs: WrappedResultSet): UserAuthToken = fromResultSet(rs, None, None, None)

  def create(user: User): Option[UserAuthToken] = {
    val token = java.util.UUID.randomUUID().toString
    val validUntil = currentTime.plusDays(21)
    val result = DB localTx { implicit session =>
      insertSql(
        $user -> user.id,
        $token -> token,
        $validUntil -> validUntil
      ).update().apply()
    }
    if (result > 0) Some(UserAuthToken(user, token, validUntil)) else None
  }
}
