package checker
import java.io.FileWriter
import java.nio.file.{Files, Path, StandardCopyOption}

import com.twitter.util.Future
import db.{Question, TestTry}
import grizzled.slf4j.Logger
import util.FilePaths
import util.FilePaths.PathExtension

import scala.collection.JavaConversions._
import scala.io.Source
import scala.util.{Failure, Success, Try}


object TomitaChecker extends Checker {

  protected val logger: Logger = Logger("TomitaChecker")

  override def check(testTry: TestTry): Future[Map[Question, Boolean]] = {
    val answers = testTry.answers map {
      case (question, Some(answer)) =>
        runTomita(answer) flatMap {
          case Success((folder, process)) if process.waitFor() == 0 =>
            readTomitaOutput(folder)
          case Success((_, process)) =>
            logger.error(s"Process finished with ${process.exitValue()}")
            Future.exception(new TomitaException("Tomita failure"))
          case Failure(e) =>
            logger.error(s"Following error has occured: $e with message ${e.getMessage}")
            Future.exception(e)
        } map { b: Boolean =>
          question -> b
        }
      case (question, _) => Future.value(question -> false)
    }
    Future
      .collect(answers.toSeq)
      .map(seq => seq.toMap)
  }

  protected def readTomitaOutput(folder: Path): Future[Boolean] = futurePool {
    logger.info("Read output.xml")
    val lines = Source.fromFile(folder.resolve("output.xml").toFile).getLines().toList
    lines.foreach(x => logger.info(s"read this: $x"))
    true
  }

  final private val args = List(FilePaths.tomitaExecutable, FilePaths.tomitaConfig)

  protected def runTomita(input: String): Future[Try[(Path, Process)]] = futurePool {
    setUpEnvironment() flatMap (folder => createInput(folder, input)) map { folder =>
      logger.info("Start process creation")
      folder -> new ProcessBuilder(args)
        .directory(folder.toFile)
        .start()
    }
  }

  protected def setUpEnvironment(): Try[Path] = Try {
    logger.debug("Start to set up environment")
    val folder = java.nio.file.Files.createTempDirectory(FilePaths.tomitaPath.asPath, "tmp")
    logger.debug(s"Create temporary folder with path $folder")
    val fileStream: Seq[Path] = Files.list(FilePaths.tomitaPath.asPath).iterator().toSeq
    fileStream
      .filter(p => p.getFileName.toString != FilePaths.tomitaName && !p.getFileName.toString.startsWith("tmp"))
      .foreach { filePath =>
      logger.debug(s"Copy $filePath to folder")
      Files.copy(filePath, folder.resolve(filePath.getFileName), StandardCopyOption.REPLACE_EXISTING)
    }
    folder
  }

  protected def createInput(path: Path, input: String): Try[Path] = Try {
    val writer = new FileWriter(path.resolve("input.txt").toFile)
    writer.write(input)
    writer.flush()
    logger.debug("Input created")
    path
  }
}
