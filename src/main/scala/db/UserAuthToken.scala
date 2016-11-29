package db

import java.sql.Timestamp

import scalikejdbc._

/**
  * Created by Sergey on 07.11.16.
  */
case class UserAuthToken(user: User, token: String, validUntil: Timestamp) {

}

object UserAuthToken {
  def forToken(token: String): Option[UserAuthToken] = using(DB(ConnectionPool.borrow())) { db =>
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

  def forUser(user: User): List[UserAuthToken] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
            SELECT
              ut.user_token_valid_until user_token_valid_until,
              ut.user_token_value user_token_value
            FROM user_token ut
            JOIN "user" u ON ut.user_id = u.user_id
            WHERE ut.user_id = ${user.id} AND ut.user_token_valid_until > now()
         """
        .map(x => fromResultSet(x, user))
        .list()
        .apply()
    }
  }

  private def fromResultSet(rs: WrappedResultSet, user: User) = UserAuthToken(
    user = user,
    token = rs.string("user_token_value"),
    validUntil = rs.timestamp("user_token_valid_until")
  )

  def fromResultSet(rs: WrappedResultSet): UserAuthToken =
    fromResultSet(rs, User.fromResultSet(rs))

  def create(user: User) = {
    val token = java.util.UUID.randomUUID().toString
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"INSERT INTO user_token(user_id, user_token_value) VALUES (${user.id}, $token)"
          .update()
          .apply()
      }
    }
    if (result > 0) forToken(token) else None
  }
}
