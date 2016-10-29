package db

import java.sql.Timestamp

import com.github.t3hnar.bcrypt._
import scalikejdbc._

case class User(id: BigInt, name: String, password: String, email: String, signupTime: Timestamp) {
  def isCorrectPassword(pass: String): Boolean = pass.isBcrypted(password)
  def isCorrectPassword(pass: Option[String]): Boolean = pass.exists(_.isBcrypted(password))

  def accessTokens = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT user_access_token FROM user_access WHERE user_id = ${id}"
        .map(x => x.string("access_token"))
        .list
        .apply()
    }
  }

  def createAccessToken = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      val uuid = java.util.UUID.randomUUID().toString
      val result = sql"INSERT INTO user_access VALUES(${uuid}, ${id})"
        .update()
        .apply()
      if (result > 0) Some(uuid) else None
    }
  }
}

object User {
  def forId(id: BigInt) = get("user_id", id)
  def forName(name: String) = get("user_name", name)
  def forEmail(email: String) = get("user_email", email)

  def apply(name: String, password: String, email: String) = {
    forName(name) getOrElse createUser(name, password, email)
  }

  def resultSetToUser(rs: WrappedResultSet): User = User(
    rs.bigInt("user_id"),
    rs.string("user_name"),
    rs.string("user_password"),
    rs.string("user_email"),
    rs.timestamp("user_signup_time")
  )

  def get[A](columnName: String, value: A) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      val statement = """
          SELECT user_id, user_name, user_password, user_email, user_signup_time
          FROM "user"
          WHERE """ + s"$columnName = ?"
      session.single(statement, value)(resultSetToUser)
    }
  }

  def createUser(name: String, password: String, email: String) = {
    using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql""" INSERT INTO "user" (user_email, user_name, user_password)
          VALUES(${email}, ${name}, ${password.bcrypt})
        """.update().apply()
      }
    }
    forName(name)
  }
}
