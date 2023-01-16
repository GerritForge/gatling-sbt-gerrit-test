package gerritforge

import io.gatling.core.scenario.Simulation

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import GerritTestConfig._
import ChangesListScenario._
import io.gatling.core.structure.{PopulationBuilder, ScenarioBuilder}
import io.gatling.http.protocol.HttpProtocol

import scala.util.Random

class GerritRestSimulation extends Simulation {

  val httpProtocol: Option[HttpProtocol] = testConfig.httpUrl.map(
    url =>
      http
        .baseUrl(url)
        .inferHtmlResources(
          BlackList(""".*\.js""", """.*\.css""", """.*\.ico""", """.*\.woff2""", """.*\.png"""),
          WhiteList()
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

  val anonymousUserChangeList = List(
    scenario("Anonymous user").exec(listChanges(testConfig.project))
  )
  val authenticatedChangeList: List[ScenarioBuilder] = List(
    scenario("Regular user")
      .feed(randomReview)
      .exec(listChanges(testConfig.project, testConfig.accountCookie, testConfig.xsrfToken))
  )

  val scenarios =
    if (testConfig.restRunAnonymousUser) authenticatedChangeList ++ anonymousUserChangeList
    else authenticatedChangeList

  require(httpProtocol.isDefined, "GERRIT_HTTP_URL must be defined to run REST-API simulation")

  setUp(
    scenarios.map(
      _.inject(rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration))
    )
  ).protocols(httpProtocol)
}
