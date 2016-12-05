package util
import java.nio.file.Path

import com.typesafe.config.Config


object FilePaths extends ConfigHolder {
  protected override final val configuration: Config = rootConfiguration.getConfig("files")

  implicit class PathExtension(val s: String) extends AnyVal {
    def asPath: Path = java.nio.file.Paths.get(s)
  }

  final val tomitaPath: String = config("tomita.folder") getOrElse "./tomita/"
  final val tomitaName: String = config("tomita.name") getOrElse "tomitaparser"
  final val tomitaExecutable: String = tomitaPath + tomitaName
  final val tomitaConfig: String = config("tomita.config") getOrElse s"$tomitaPath/config.proto"
}
