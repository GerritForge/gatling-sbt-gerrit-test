package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.GatlingRestUtils._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
object ChangeScenarios extends ScenarioBase {

  val abandonChangeScn: ScenarioBuilder = {
    setupAuthenticatedSession("Abandon Change")
      .exec(
        http("Abandon change")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/abandon")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )
  }
  override val scns: List[ScenarioBuilder] = List(abandonChangeScn)
}
