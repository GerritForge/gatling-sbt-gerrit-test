package gerritforge.scenarios.rest

import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.ScenarioBase
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.{Cookie, addCookie, http}
import io.gatling.http.protocol.HttpProtocol

import scala.util.Random

trait RestScenarioBase extends ScenarioBase {

  val randomNumber = new Random
  val XSS_LEN      = 5

  val httpProtocol: Option[HttpProtocol] = testConfig.httpUrl.map(
    url =>
      http
        .baseUrl(url)
        .inferHtmlResources(
          AllowList(),
          DenyList(""".*\.js""", """.*\.css""", """.*\.ico""", """.*\.woff2""", """.*\.png""")
        )
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("en-GB,en;q=0.5")
        .userAgentHeader("gatling-test")
  )

  val restApiHeader = Map(
    "Accept"                    -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Pragma"                    -> "no-cache",
    "Upgrade-Insecure-Requests" -> "1"
  )

  val randomInt = new Random
  val randomReview = Iterator.continually(
    Map(
      "reviewMessage" -> (1 to (10 + randomInt.nextInt(10)))
        .map(_ => Random.alphanumeric.take(randomInt.nextInt(10)).mkString)
        .mkString(" "),
      "reviewScore" -> (randomInt.nextInt(5) - 2)
    )
  )

  def postApiHeader(
      xsrfCookie: Option[String],
      contentType: Option[String] = Some("application/json")
  ) = {
    val headers: Map[String, String] = restApiHeader ++ contentType.map("Content-Type" -> _)
    xsrfCookie.fold(headers)(c => headers + ("x-gerrit-auth" -> c))
  }

  def setupAuthenticatedSession(scnTitle: String): ScenarioBuilder = {
    testConfig.accountCookie match {
      case Some(cookie) =>
        scenario(scnTitle)
          .exec(addCookie(Cookie("GerritAccount", cookie)))
      case None => throw new Exception("Requires authentication")
    }
  }
}
