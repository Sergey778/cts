package db

import org.scalatest.{FlatSpec, Matchers}
import scalikejdbc.config.DBs

/**
  * Created by Sergey on 02.11.16.
  */
class UserTest extends FlatSpec with Matchers {

  val testName = "Test"
  val testPassword = "Password"
  val testEmail = "test@gmail.com"

  DBs.setupAll()

  "User" should "created on create function calling" in {
    val user = User.create(testName, testPassword, testEmail)
    assert(user.nonEmpty)
  }

  "User" can "be obtained from functions that start with `find`" in {
    val users = (User.withName(testName), User.withEmail(testEmail))
    users match {
      case (Some(u1), Some(u2)) =>
        u1 shouldEqual u2
        val u3 = User.withId(u1.id)
        assert(u3.nonEmpty)
        u3.get shouldEqual u2
      case _ => fail()
    }
  }

  "User received from object functions" should
    "have the same parameters are the same when it is created" in {
    val user = User.withName(testName)
    assert(user.nonEmpty)
    user.get.name shouldEqual testName
    user.get.isCorrectPassword(testPassword)
    user.get.email shouldEqual testEmail
  }

  "Newly created user" must "be not confirmed" in {
    val user = User.withEmail(testEmail)
    user.get.confirmed shouldEqual false
  }

  "User" can "be confirmed using `confirm` method" in {
    val first = User.withEmail(testEmail).get.confirm.get
    first.confirmed shouldEqual true
    val second = User.withEmail(testEmail).get
    second.confirmed shouldEqual true
  }

  "User.delete" should "remove user from db" in {
    val deletion = User.withEmail(testEmail).get.delete
    assert(deletion)
    User.withEmail(testEmail) shouldEqual None
  }
}
