package gerritforge

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.Random

case class ChangeDetail(_number: Int, project: String) {
  lazy val url = s"/c/${project}/+/${_number}/"
}

object ChangesListScenario {

  val XSS_LEN = 5

  val restApiHeader = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Pragma" -> "no-cache",
    "Upgrade-Insecure-Requests" -> "1"
  )

  val random = new Random

  def listChanges(authCookie: Option[String] = None) =
    authCookie
      .fold(exec(flushSessionCookies)) { auth =>
        exec(addCookie(Cookie("GerritAccount", auth)))
      }
      .exec(
        http("changes list and get first change")
          .get("/q/status:open")
          .headers(restApiHeader)
          .resources(
            http("get server version")
              .get("/config/server/version"),
            http("get server info")
              .get("/config/server/info"),
            http("get list of changes")
              .get("/changes/?O=81&S=0&n=500&q=status%3Aopen")
              .check(
                bodyString.transform(_.drop(XSS_LEN))
                  .transform(decode[List[ChangeDetail]](_))
                  .transform(_.right.get)
                  .transform(changes => changes(random.nextInt(changes.size)))
                  .transform(_.url)
                  .saveAs("changeUrl"))))
      .exec(
        http("get change details")
          .get("${changeUrl}")
      )
      .pause(2 seconds)
}
