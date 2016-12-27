package checker
import java.io.FileWriter
import java.nio.file.{Files, Path, StandardCopyOption}
import java.time.LocalDateTime

import com.twitter.util.Future
import db._
import grizzled.slf4j.Logger
import util.FilePaths
import util.FilePaths.PathExtension

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML}


object TomitaChecker extends Checker {

  protected val logger: Logger = Logger("TomitaChecker")

  override def check(testTry: TestTry): Future[Seq[TestTryAnswer]] = {
    val answers = testTry.answers map {
      case e @ TestTryAnswer(_, question, Some(answer), _, _) =>
        runTomita(answer) flatMap {
          case Success((folder, process)) if process.waitFor() == 0 =>
            getTomitaOutput(folder).flatMap { xmlAnswer =>
              val checkResult = check(QuestionAnswer.fromQuestion(question), xmlAnswer)
              checkResult.foreach(x => logger.info(s"result => $x"))
              checkResult.map(x => e.updateSystemGrade(Some(x)))
            }
          case Success((_, process)) =>
            logger.error(s"Process finished with ${process.exitValue()}")
            Future.exception(new TomitaException("Tomita failure"))
          case Failure(e) =>
            logger.error(s"Following error has occured: $e with message ${e.getMessage}")
            Future.exception(e)
        }
      case e @ TestTryAnswer(_, question, _, _, _) => Future.value(e.updateSystemGrade(Some(0)))
    }
    Future
      .collect(answers)
  }

  protected def readTomitaOutput(folder: Path): Future[Boolean] = futurePool {
    logger.info("Read output.xml")
    val xml: NodeSeq = XML.loadFile(folder.resolve("output.xml").toFile)
    logger.info(xml)
    val document = parseDocument(xml)
    logger.info(document)
    logger.info("objects:" + document.objects)
    logger.info("subjects:" + document.subjects)
    logger.info("relations:" + document.relations)
    true
  }

  protected def parseDocument(xml: NodeSeq): TomitaDocument = {
    val root: NodeSeq = xml \ "document"
    val facts = root \ "facts" \\ "_"
    TomitaDocument(
      (root \@ "di"),
      (root \@ "bi"),
      LocalDateTime.now(), // TODO: Change this
      parseFacts(facts)
    )
  }

  protected def parseFacts(xml: NodeSeq): Seq[TomitaFact] = xml map { x =>
    x.label match {
    case "ObjectTag" => ObjectFact(ObjectVal((x \ "Object" \@ "val")))
    case "SubjectTag" => SubjectFact(SubjectVal(x \ "Subject" \@ "val"))
    case "RelationTag" => RelationFact(RelationVal(x \ "Relation" \@ "val"))
    case "NounTag" => NounTag(CanonicWordVal(x \ "CanonicWord" \@ "val"))
    case "VerbTag" => VerbTag(CanonicWordVal(x \ "CanonicWord" \@ "val"))
    case "AdjTag" => AdjTag(CanonicWordVal(x \ "CanonicWord" \@ "val"))
    case _ => OtherTag(CanonicWordVal((x \ "CanonicWord" \@ "val")))
    }
  }

  final private val args = List(FilePaths.tomitaExecutable, FilePaths.tomitaConfig)

  def runTomita(input: String): Future[Try[(Path, Process)]] = futurePool {
    setUpEnvironment() flatMap (folder => createInput(folder, input)) map { folder =>
      logger.info("Start process creation")
      folder -> new ProcessBuilder(args)
        .directory(folder.toFile)
        .start()
    }
  }

  def setUpEnvironment(): Try[Path] = Try {
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

  def createInput(path: Path, input: String): Try[Path] = Try {
    val writer = new FileWriter(path.resolve("input.txt").toFile)
    writer.write(input)
    writer.flush()
    logger.debug("Input created")
    path
  }

  def getOutput(input: String): Future[NodeSeq] = {
    runTomita(input) flatMap {
      case Success((folder, process)) if process.waitFor() == 0 =>
        getTomitaOutput(folder)
      case Success((_, process)) =>
        logger.error(s"Process finished with ${process.exitValue()}")
        Future.exception(new TomitaException("Tomita failure"))
      case Failure(e) =>
        logger.error(s"Following error has occured: $e with message ${e.getMessage}")
        Future.exception(e)
    }
  }

  def getTomitaOutput(folder: Path): Future[NodeSeq] = futurePool {
    XML.loadFile(folder.resolve("output.xml").toFile)
  }

  def check(standardAnswers: Seq[QuestionAnswer], answer: NodeSeq): Future[Int] = futurePool {
    val maxScore = standardAnswers.map { s =>
      val xml = s.xml.getOrElse("")
      compareAnswers(XML.loadString(xml), answer)
    }.max
    maxScore
  }

  def compareAnswers(standardAnswer: NodeSeq, answer: NodeSeq): Int = {
    val standardDocument = parseDocument(standardAnswer)
    val document = parseDocument(answer)
    standardDocument.compare(document)
  }
}
