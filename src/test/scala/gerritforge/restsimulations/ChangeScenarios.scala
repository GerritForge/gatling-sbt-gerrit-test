package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.GatlingRestUtils._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
object ChangeScenarios extends ScenarioBase {

  val abandonAndRestoreChangeScn: ScenarioBuilder =
    setupAuthenticatedSession("Abandon and then Restore Change")
      .exec(
        http("Abandon Change")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/abandon")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )
      .pause(1 second)
      .exec(
        http("Restore change")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/restore")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )

  val submitChangeScn : ScenarioBuilder = {
    setupAuthenticatedSession("Submit Change")
      .exec(http("request_1")
        .post(s"/changes/${testConfig.project}~#{changeNumber}/revisions/1/review")
        .headers(postApiHeader(testConfig.xsrfToken))
        .body(StringBody("""{"labels":{"Code-Review":2}}""")).asJson,
      )
      .exec(http("Submit Change")
        .post(s"/changes/${testConfig.project}~#{changeNumber}/revisions/1/submit")
        .headers(postApiHeader(testConfig.xsrfToken))
        .body(StringBody("""{}""")),
    )
  }

  override val scns: List[ScenarioBuilder] = List(abandonAndRestoreChangeScn, submitChangeScn)
}
