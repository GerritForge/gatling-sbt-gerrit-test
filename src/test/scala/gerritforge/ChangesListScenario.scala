package gerritforge

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

object ChangesListScenario {

  val restApiHeader = Map(
    "Accept"                    -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Pragma"                    -> "no-cache",
    "Upgrade-Insecure-Requests" -> "1"
  )

  def listChanges(authCookie: Option[String] = None) =
    authCookie
      .fold(exec(flushSessionCookies)) { auth =>
        exec(addCookie(Cookie("GerritAccount", auth)))
      }
      .exec(
        http("changes list")
          .get("/q/status:open")
          .headers(restApiHeader)
          .resources(
            http("get server version")
              .get("/config/server/version"),
            http("get server info")
              .get("/config/server/info"),
            http("get list of changes")
              .get("/changes/?O=81&S=0&n=500&q=status%3Aopen")
          )
      )
      .pause(2 seconds)
}
