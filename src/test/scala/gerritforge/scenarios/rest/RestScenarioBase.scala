package gerritforge.scenarios.rest

import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.ScenarioBase
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.{Cookie, addCookie}

import java.util.UUID
import scala.util.Random

trait RestScenarioBase extends ScenarioBase {

  val XSS_LEN = 5

  val restApiHeader = Map(
    "Accept"                    -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Pragma"                    -> "no-cache",
    "Upgrade-Insecure-Requests" -> "1"
  )

  val randomReview = Iterator.continually(
    Map(
      "reviewMessage" -> (1 to (10 + Random.nextInt(10)))
        .map(_ => Random.alphanumeric.take(Random.nextInt(10)).mkString)
        .mkString(" "),
      "reviewScore" -> (Random.nextInt(5) - 2)
    )
  )

  var randomFeeder = Iterator.continually(Map("randomValue" -> UUID.randomUUID()))

  def addApiHeaders(
      xsrfCookie: Option[String],
      contentType: Option[String] = Some("application/json")
  ) = {
    val headers: Map[String, String] = restApiHeader ++ contentType.map("Content-Type" -> _)
    xsrfCookie.fold(headers)(c => headers + ("x-gerrit-auth" -> c))
  }

  def setupCookies(scnTitle: String): ScenarioBuilder = {
    val builder = setupAuthenticatedSession(scnTitle)
    addStickyCookie(builder)
  }

  def setupAuthenticatedSession(scnTitle: String): ScenarioBuilder = {
    testConfig.accountCookie match {
      case Some(cookie) =>
        scenario(scnTitle)
          .exec(addCookie(Cookie("GerritAccount", cookie)))
      case None => throw new Exception("Requires authentication")
    }
  }

  def addStickyCookie(builder: ScenarioBuilder): ScenarioBuilder = {
    testConfig.stickyCookie
      .fold(builder) { c =>
        val cookie = c.split(':')
        builder.exec(addCookie(Cookie(cookie(0), cookie(1))))
      }
  }
}
