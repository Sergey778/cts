package util

import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.{Message, Session, Transport}

import com.twitter.util.{Future, FuturePool}
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

object Email {
  val configuration = ConfigFactory.load()
  val properties = {
    val p = new Properties()
    val m = configuration.entrySet().map { x =>
      x.getKey -> x.getValue.unwrapped()
    }.toMap
    p.putAll(m)
    p
  }

  val session = Session.getInstance(properties)

  def sendMessage(message: MimeMessage) = {
    val user = configuration.getString("mail.user")
    val pass = configuration.getString("mail.password")
    message.setFrom(user)
    Transport.send(message, user, pass)
  }

  def sendMessage(recepeint: String, subject: String, text: String): Future[Unit] = {
    try {
      val message = new MimeMessage(session)

      val body = new MimeMultipart()
      val bodyPart = new MimeBodyPart()
      bodyPart.setText(text)

      body.addBodyPart(bodyPart)

      message.setSubject(subject)
      message.setContent(body)
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepeint))

      FuturePool.unboundedPool(Email.sendMessage(message))
    } catch {
      case e: Throwable => Future.exception[Unit](e)
    }
  }
}
