package owl.backend.extractors.tomita

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

class TomitaRunnerTest extends FlatSpec with Matchers {

  private val testConfig = TomitaConfig(Paths.get("tomita").toAbsolutePath,
    "tomitaparser",
    "config.proto",
    deleteFolders = false,
    charset = StandardCharsets.UTF_8)

  private val testInput = "Мир"

  "'run' method" should "return Success" in {
    val runner = new TomitaRunner(testConfig)
    val output = Try(Await.result(runner.run(testInput), 2.minutes))
    output.isSuccess shouldEqual true
  }

}
