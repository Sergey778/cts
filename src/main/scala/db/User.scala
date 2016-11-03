package db

import java.sql.Timestamp
import java.time.LocalDateTime

import com.github.t3hnar.bcrypt._
import scalikejdbc._

case class User(id: BigInt,
                name: String,
                password: String,
                email: String,
                signupTime: Timestamp,
                confirmed: Boolean = false) {

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

  def authReferences = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""SELECT user_id, user_auth_ref, user_auth_ref_type, user_auth_ref_valid_until
            FROM user_auth_ref WHERE user_id = ${id} AND user_auth_ref_valid_until > now()"""
        .map(x => UserAuthRef.resultSetToAuthRef(x, this))
        .list
        .apply()
    }
  }

  def createAuthReference(refType: AuthRefType) = using(DB(ConnectionPool.borrow())) { db =>
    val uuid = java.util.UUID.randomUUID().toString
    val now = Timestamp.valueOf(LocalDateTime.now().plusDays(1))
    db localTx { implicit session =>
      sql"""INSERT INTO user_auth_ref(user_id, user_auth_ref, user_auth_ref_type, user_auth_ref_valid_until)
               VALUES (${id}, ${uuid}, ${refType.toString}, ${now})"""
        .update()
        .apply()
    }
    UserAuthRef(this, uuid, refType, now)
  }

  def confirm = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      val result = sql"""UPDATE "user" SET user_confirmed = TRUE WHERE user_id = ${id}"""
        .update()
        .apply()
      if (result > 0) Some(User(id, name, password, email, signupTime, confirmed = true))
      else None
    }
  }

  def delete = using(DB(ConnectionPool.borrow())) { db =>
    val result = db localTx { implicit session =>
      sql"""DELETE FROM "user" WHERE user_id = ${id}"""
        .update()
        .apply()
    }
    result > 0
  }
}

object User {
  def findById(id: BigInt) = find("user_id", id)
  def findByName(name: String) = find("user_name", name)
  def findByEmail(email: String) = find("user_email", email)

  def apply(name: String, password: String, email: String) = {
    findByName(name) getOrElse create(name, password, email)
  }

  def fromResultSet(rs: WrappedResultSet): User = User(
    rs.bigInt("user_id"),
    rs.string("user_name"),
    rs.string("user_password"),
    rs.string("user_email"),
    rs.timestamp("user_signup_time"),
    rs.boolean("user_confirmed")
  )

  protected def find[A](columnName: String, value: A) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      val statement = """
          SELECT user_id, user_name, user_password, user_email, user_signup_time, user_confirmed
          FROM "user"
          WHERE """ + s"$columnName = ?"
      session.single(statement, value)(fromResultSet)
    }
  }

  def create(name: String, password: String, email: String) = {
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql""" INSERT INTO "user" (user_email, user_name, user_password)
          VALUES(${email}, ${name}, ${password.bcrypt})
        """.update().apply()
      }
    }
    if (result > 0) findByName(name) else None
  }
}
