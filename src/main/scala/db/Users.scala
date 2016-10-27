package db

import com.github.t3hnar.bcrypt._
import scalikejdbc._

object Users {

  def getUserId(userName: String) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""SELECT user_id FROM "user" WHERE user_name = ${userName}"""
        .map(x => x.bigInt("user_id"))
        .single()
        .apply()
    }
  }

  def getPassword(userName: String) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""SELECT user_password FROM "user" WHERE user_name = ${userName}"""
        .map(x => x.string("user_password"))
        .single()
        .apply()
    }
  }

  def checkPassword(userName: String, userPassword: String) = getPassword(userName) map { passwordHash =>
    Password(userPassword).isBcrypted(passwordHash)
  } getOrElse {
    false
  }

  def createToken(userName: String) = using(DB(ConnectionPool.borrow())) { db =>
    val uuid = java.util.UUID.randomUUID().toString
    db localTx { implicit session =>
      getUserId(userName) map { id =>
        val t = scala.math.BigInt(id)
        sql"""INSERT INTO user_access VALUES(${uuid}, ${t})""".update().apply()
      } filter { result =>
         result > 0
      } map { _ =>
        uuid
      }
    }
  }

  def createUser(username: String, password: String, email: String, avalonLogin: Option[String]) = {
    val passwordHash = Password(password).bcrypt
    using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"""INSERT INTO "user"(user_email, user_name, user_password, user_avalon_login)
                           VALUES(${email}, ${username}, ${passwordHash}, ${avalonLogin})"""
          .update()
          .apply()
      }
    }
  }

}
