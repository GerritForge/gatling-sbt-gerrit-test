package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.TagScenarios.XSS_LEN
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocol
import io.circe.parser._
import io.circe.generic.auto._

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Calendar
import scala.util.Random

case class ChangeDetail(
    _number: Int,
    project: String,
    current_revision: String
) {
  lazy val url = s"/c/$project/+/${_number}/"
}
object GatlingRestUtils {
  val encoding = StandardCharsets.UTF_8.toString()

  val randomNumber = new Random

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

  def listChanges =
    http("changes list and get first change")
      .get(s"/q/status:open+project:${testConfig.project}")
      .headers(restApiHeader)
      .resources(
        http("get list of changes")
          .get(
            s"/changes/?n=500&q=status%3Aopen+project:${testConfig.project}&o=CURRENT_REVISION"
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

  def pickRandomChange =
    doIf(session => session("changeDetails").as[List[ChangeDetail]].nonEmpty) {
      exec { session =>
        val changes = session("changeDetails").as[List[ChangeDetail]]
        val change  = changes(randomNumber.nextInt(changes.size))
        session
          .set("id", s"${encode(change.project)}~${change._number}")
          .set("url", change.url)
          .set("changeNum", change._number)
          .set("revision", encode(change.current_revision))
      }
    }

  def encode(value: String): String = {
    URLEncoder.encode(value, encoding)
  }

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
