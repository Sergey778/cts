package assets

import com.twitter.finagle.http.Status
import com.twitter.finatra.http._
import com.twitter.inject.server.FeatureTest
import runner.Server

import scala.io.Source

/**
  * Created by Sergey on 26.10.16.
  */

class AssetsControllerTest extends FeatureTest {

  override val server = new EmbeddedHttpServer(new Server)

  "Get request with path \"resources/libs/jquery-3.1.1.min.js\"" should {

    "return jquery-3.1.1.min.js file" in {
      val response = server.httpGet(path = "/resources/libs/jquery-3.1.1.min.js")
      response.status shouldEqual Status.Ok
      val fileContent = Source.fromURL(getClass.getResource("/jquery-3.1.1.min.js")).mkString
      response.contentString shouldEqual fileContent
    }

  }

  val nonExistPath = """/resources/non/exist/path"""

  s"Get request with path $nonExistPath" should {
    "return 'not found' status" in {
      server.httpGet(nonExistPath, andExpect = Status.NotFound)
    }
  }

  "Get request without concrete file" should {
    "return 'not found' status" in {
      server.httpGet("/resources", andExpect = Status.NotFound)
    }
  }
}
