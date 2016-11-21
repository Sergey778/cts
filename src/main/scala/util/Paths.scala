package util

import com.typesafe.config.{ConfigException, ConfigFactory}

object Paths {

  protected val configuration = ConfigFactory.load().getConfig("paths")

  protected def config(name: String) =
    try {
      Some(configuration.getString(name))
    } catch {
      case e: ConfigException => None
    }

  final val signIn = config("auth.signIn") getOrElse "/signin"
  final val signUp = config("auth.signUp") getOrElse "/signup"
  final val signUpConfirmation = config("auth.signUpConfirm") getOrElse "/signupconfirm"
  final val signOut = config("auth.signOut") getOrElse "/signout"
  final val signUpConfirmInfo = config("auth.signUpConfirmInfo") getOrElse "/signupconfirminfo"

  final val profile = config("profile.index") getOrElse "/profile"
}
