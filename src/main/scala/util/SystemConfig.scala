package util

import com.typesafe.config.Config

import scala.util.Try

object SystemConfig extends ConfigHolder {

  override protected final val configuration: Config = rootConfiguration.getConfig("system")

  final val systemTerminal: Option[String] = config("terminal") orElse {
    val osName = Try(System.getProperty("os.name").toLowerCase).getOrElse("")
    val bashSystems = Array("mac", "linux", "freebsd", "solaris")
    if (bashSystems.exists(p => osName.startsWith(p))) Some("/bin/bash")
    else if (osName.startsWith("windows")) Some("C:\\Windows\\System32\\cmd.exe")
    else None
  }
}
