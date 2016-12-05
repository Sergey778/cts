package util

import com.typesafe.config.{Config, ConfigException, ConfigFactory}

protected trait ConfigHolder {
  protected val rootConfiguration: Config = ConfigFactory.load()
  protected val configuration: Config

  protected def config(name: String): Option[String] =
    try {
      Some(configuration.getString(name))
    } catch {
      case _: ConfigException => None
    }
}
