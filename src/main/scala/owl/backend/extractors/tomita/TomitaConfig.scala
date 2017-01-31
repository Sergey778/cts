package owl.backend.extractors.tomita

import java.nio.charset.Charset
import java.nio.file.Path

import scala.concurrent.duration._

/**
  * Used to configure tomitaparser calling and running
  * @param folder Folder with files for tomita parser
  * @param executableName Name of tomita parser executable
  * @param configName Name of tomita configuration file
  * @param charset Charset for file. Default is [[Charset.defaultCharset()]]
  * @param processTimeout Wait time for tomita parser process. Default is 2 minutes.
  * @param inputName Name of input file for tomita. Should be the same as in '[[configName]]' file.
  *                  Default is "input.txt"
  * @param outputName Name of output file for tomita. Should be the same as in '[[configName]]' file.
  *                   Default is "output.xml"
  * @param tempFolderPrefix Prefix for temporary folders which will contain inputs and outputs.
  *                         Default is "run"
  * @param deleteFolders Parameter that indicate delete temporary folders after work or not.
  -                      If true - folders will be deleted
  -                      If false - folders will stay untouched
  *                      Default is true.
  */
case class TomitaConfig (
                        folder: Path,
                        executableName: String,
                        configName: String,
                        charset: Charset = Charset.defaultCharset(),
                        processTimeout: Duration = 2.minutes,
                        inputName: String = "input.txt",
                        outputName: String = "output.xml",
                        tempFolderPrefix: String = "run",
                        deleteFolders: Boolean = true
                        ) {
  /**
    * Concatenates [[folder]] and [[executableName]]
    * @return Path to executable of tomita parser
    */
  def executablePath: Path = folder.resolve(executableName)

  /**
    * Concatenates [[folder]] and [[configName]]
    * @return Path to configuration file of tomita parser
    */
  def configPath: Path = folder.resolve(configName)
}