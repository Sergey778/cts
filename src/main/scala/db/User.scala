package db

import java.time.LocalDateTime

import com.github.t3hnar.bcrypt._
import db.core._
import scalikejdbc._

case class User(id: BigInt,
                name: String,
                password: String,
                email: String,
                signupTime: LocalDateTime,
                confirmed: Boolean = false) extends Table {

  def isCorrectPassword(pass: String): Boolean = pass.isBcrypted(password)
  def isCorrectPassword(pass: Option[String]): Boolean = pass.exists(_.isBcrypted(password))

  def accessTokens = UserAuthToken.forUser(this)

  def createAccessToken = UserAuthToken.create(this)

  def authReferences: List[UserAuthRef] = UserAuthRef.withUserAndValid(this)

  def createAuthReference(refType: AuthRefType): Option[UserAuthRef] = UserAuthRef.create(this, refType)

  def confirm = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      val result = sql"""UPDATE "user" SET user_confirmed = 1 WHERE user_id = $id"""
        .update()
        .apply()
      if (result > 0) Some(User(id, name, password, email, signupTime, confirmed = true))
      else None
    }
  }

  def delete = using(DB(ConnectionPool.borrow())) { db =>
    val result = db localTx { implicit session =>
      sql"""DELETE FROM "user" WHERE user_id = $id"""
        .update()
        .apply()
    }
    result > 0
  }

  def isMemberOfGroup(g: UserGroup) = using(DB(ConnectionPool.borrow())) { db =>
    val result = db readOnly { implicit session =>
      sql"""
           SELECT COUNT(*) FROM user_groups
           WHERE user_id = $id AND user_group_id = ${g.id} AND full_member = 'T'
         """
        .map(rs => rs.bigInt(1))
        .single()
        .apply()
    }
    result.exists(x => BigInt(x) > 0)
  }

  def groups = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
           SELECT
            u.user_group_id "user_group_id",
            u.user_group_name "user_group_name",
            u.user_group_leader "user_group_leader",
            u.user_group_parent_id "user_group_parent_id"
           FROM user_group u JOIN user_groups g ON u.user_group_id = g.user_group_id
           WHERE g.user_id = $id
         """
        .map(rs => UserGroup.fromResultSet(rs))
        .list()
        .apply()
    }
  }
}

object User extends TableObject[User] with IdHolder with TimeHolder {

  override def name: String = "\"user\""

  private final val $id = "id"
  private final val $name = "name"
  private final val $password = "password"
  private final val $email = "email"
  private final val $signupTime = "signupTime"
  private final val $confirmed = "confirmed"

  override def columns: Map[String, String] = Map (
    $id -> "user_id",
    $name -> "user_name",
    $password -> "user_password",
    $email -> "user_email",
    $signupTime -> "user_signup_time",
    $confirmed -> "user_confirmed"
  )

  def withId(id: BigInt): Option[User] = whereOption($id -> id)
  def withName(name: String): Option[User] = whereOption($name -> name)
  def withEmail(email: String): Option[User] = whereOption($email -> email)

  def fromResultSet(rs: WrappedResultSet,
                    id: Option[BigInt] = None,
                    name: Option[String] = None,
                    password: Option[String] = None,
                    email: Option[String] = None,
                    signupTime: Option[LocalDateTime] = None,
                    userConfirmed: Option[Boolean] = None) = User(
    id = id.getOrElse(rs.bigInt(columns($id))),
    name = name.getOrElse(rs.string(columns($name))),
    password = password.getOrElse(rs.string(columns($password))),
    email = email.getOrElse(rs.string(columns($email))),
    signupTime = signupTime.getOrElse(rs.timestamp(columns($signupTime)).toLocalDateTime),
    confirmed = userConfirmed.getOrElse(rs.int(columns($confirmed)) > 0)
  )

  override def fromResultSet(rs: WrappedResultSet): User = fromResultSet(rs, None, None, None, None, None, None)

  def create(name: String, password: String, email: String): Option[User] = nextId flatMap { id =>
    val time = currentTime
    val crypted = password.bcrypt
    val result = DB localTx { implicit session =>
      insertSql(
        $id -> BigInt(id),
        $name -> name,
        $password -> crypted,
        $email -> email,
        $signupTime -> time,
        $confirmed -> 0
      ).update().apply()
    }
    if (result > 0) Some(User(BigInt(id), name, crypted, email, time)) else None
  }
}
