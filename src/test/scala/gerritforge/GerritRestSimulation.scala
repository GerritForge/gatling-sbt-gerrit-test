package gerritforge

import gerritforge.GerritTestConfig._
import gerritforge.restsimulations.TagScenarios.httpProtocol
import gerritforge.restsimulations.{ChangeScenarios, TagScenarios}
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

class GerritRestSimulation extends Simulation {

  val authenticatedScenarios = ChangeScenarios.authenticatedScns ++ TagScenarios.authenticatedScns

  val scenarios =
    if (testConfig.restRunAnonymousUser)
      ChangeScenarios.anonymousScns ++ authenticatedScenarios
    else authenticatedScenarios

  require(httpProtocol.isDefined, "GERRIT_HTTP_URL must be defined to run REST-API simulation")

  setUp(
    scenarios.map(
      _.inject(rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration))
    )
  ).protocols(httpProtocol)
}
