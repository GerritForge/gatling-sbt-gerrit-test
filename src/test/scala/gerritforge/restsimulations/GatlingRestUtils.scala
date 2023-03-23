package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocol

import java.util.Calendar
import scala.util.Random

object GatlingRestUtils {
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

  def createChange =
    http("Create Change")
      .post("/changes/")
      .headers(postApiHeader(testConfig.xsrfToken))
      .body(
        StringBody(s"""{"project":"${testConfig.project}",
             |"branch":"master",
             |"subject":"Test commit subject - ${Calendar.getInstance().getTime}"}""".stripMargin)
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

  def postApiHeader(xsrfCookie: Option[String]) = {
    val headers: Map[String, String] = restApiHeader + ("Content-Type" -> "application/json")
    xsrfCookie.fold(headers)(c => headers + ("x-gerrit-auth" -> c))
  }

}
