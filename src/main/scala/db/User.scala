package db

import com.github.t3hnar.bcrypt._
import scalikejdbc._

case class User(id: BigInt, name: String, password: String, email: String, avalonLogin: Option[String]) {
  def isCorrectPassword(pass: String): Boolean = pass.isBcrypted(password)
  def isCorrectPassword(pass: Option[String]): Boolean = pass.exists(_.isBcrypted(password))

  def accessTokens = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT access_token FROM user_access WHERE user_id = ${id}"
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

  def apply(name: String, password: String, email: String, avalonLogin: Option[String] = None) = {
    forName(name) getOrElse createUser(name, password, email, avalonLogin)
  }

  def resultSetToUser(rs: WrappedResultSet): User = User(
    rs.bigInt("user_id"),
    rs.string("user_name"),
    rs.string("user_password"),
    rs.string("user_email"),
    rs.stringOpt("user_avalon_login")
  )

  def get[A](columnName: String, value: A) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      val statement = """
          SELECT user_id, user_name, user_password, user_email, user_avalon_login
          FROM "user"
          WHERE """ + s"$columnName = ?"
      session.single(statement, value)(resultSetToUser)
    }
  }

  def createUser(name: String, password: String, email: String, avalonLogin: Option[String] = None) = {
    using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql""" INSERT INTO "user" (user_email, user_name, user_password, user_avalon_login)
          VALUES(${email}, ${name}, ${password}, ${avalonLogin})
        """.update().apply()
      }
    }
    forName(name)
  }
}
