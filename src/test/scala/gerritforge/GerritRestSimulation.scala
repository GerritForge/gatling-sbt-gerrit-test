package gerritforge

import gerritforge.ChangesListScenario._
import gerritforge.GerritTestConfig._
import gerritforge.restsimulations.GatlingRestUtils.{httpProtocol, randomReview}
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder

class GerritRestSimulation extends Simulation {

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
