package owl.backend.extractors.tomita

import java.nio.file._
import java.util.concurrent.TimeUnit

import scala.collection.JavaConversions._
import scala.concurrent._
import scala.util.{Failure, Success, Try}

/**
  * Used to run tomita on some inputs with specified configuration
  * @param config Configuration of tomita
  */
class TomitaRunner(val config: TomitaConfig)(implicit ec: ExecutionContext) {

  /**
    * Runs tomita parser of specified input and returns its output
    *
    * This method uses this sequence of actions:
    - Create temporary folder in which processing is held
    - Copy configuration and grammars to that folder
    - Create file in that folder with specified input
    - Run tomita
    - Delete temporary folder and return output
    * @param input String that should be processed with tomita parser
    * @return Output of tomita parser or error that has occurred
    */
  def run(implicit input: String): Future[String] =
    createFolder flatMap copyFiles flatMap createInput flatMap runTomita flatMap getOutput flatMap deleteFolder

  /**
    * Creates temporary folder for running tomita on some input
    * @return Path of temporary folder
    */
  private def createFolder: Future[Path] = Future {
    Files.createTempDirectory(config.folder, config.tempFolderPrefix).toAbsolutePath
  }

  /**
    * Copies files from tomita main folder to specified path
    * @param path Path of temporary folder
    * @return Path of temporary folder
    */
  private def copyFiles(path: Path): Future[Path] = Future {
    // iterator().toSeq is ugly conversion needed in Scala 2.11
    Files.list(config.folder).iterator().toSeq.filterNot { p =>
      p.getFileName.toString == config.executableName || p.getFileName.toString.startsWith(config.tempFolderPrefix)
    } foreach { p =>
      Files.copy(p, path.resolve(p.getFileName), StandardCopyOption.REPLACE_EXISTING)
    }
    path
  }

  /**
    * Creates input file for tomita and writes specified input in
    * @param path Path of temporary folder
    * @param input Sentence that should be parsed with tomita
    * @return Path of temporary folder
    */
  private def createInput(path: Path)(implicit input: String): Future[Path] = Future {
    Files.write(path.resolve(config.inputName), input.getBytes(config.charset), StandardOpenOption.CREATE)
    path
  }

  /**
    * Runs tomita parser process.
    * If process exits in time that specified in [[config.processTimeout]] and no error has occurred returns folder path
    * @param path Path of temporary folder
    * @return Path of temporary folder
    */
  private def runTomita(path: Path): Future[Path] = Future {
    val tomitaProcess = new ProcessBuilder(List(config.executablePath.toString, config.configPath.toString))
      .directory(path.toFile)
      .start()
    Try {
      blocking {
        tomitaProcess.waitFor(config.processTimeout.toNanos, TimeUnit.NANOSECONDS)
      }
    }
  } flatMap {
    case Success(true) => Future.successful(path)
    case Success(false) => Future.failed(new TimeoutException("Tomita process timeout"))
    case Failure(e) => Future.failed(e)
  }

  /**
    * Reads and returns containment of tomita output
    * @param path Path of temporary folder
    * @return Tuple that contains path of temporary folder and tomita output
    */
  private def getOutput(path: Path): Future[(Path, String)] = Future {
    (path, Files.readAllLines(path.resolve(config.outputName), config.charset).mkString)
  }

  /**
    * Deletes temporary folder and returns tomita output
    * @param p Tuple that contains path of temporary folder and tomita output
    * @return String that contains tomita output
    */
  private def deleteFolder(p: (Path, String)): Future[String] = Future {
    val (path, output) = p
    if (config.deleteFolders) Files.delete(path)
    output
  }

}
