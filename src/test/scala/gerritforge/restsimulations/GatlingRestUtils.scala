package gerritforge.restsimulations

import gerritforge.ChangeDetail
import gerritforge.ChangesListScenario.XSS_LEN
import gerritforge.GerritTestConfig.testConfig
import io.circe.generic.auto._
import io.circe.parser._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocol

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

  def firstOpenChangeDetails(
      projectName: String
  ) =
    http("changes list and get first change")
      .get(s"/q/status:open+project:$projectName")
      .headers(restApiHeader)
      .resources(
        http("get list of changes")
          .get(
            s"/changes/?O=81&S=0&n=500&q=status%3Aopen+project:$projectName&o=CURRENT_REVISION"
          )
          .check(
            bodyString
              .transform(_.drop(XSS_LEN))
              .transform(decode[List[ChangeDetail]](_))
              .transform {
                case Right(changeDetailList) => changeDetailList
                case Left(decodingError)     => throw decodingError
              }
              .saveAs("changeDetails")
          )
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