package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.GatlingRestUtils._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import java.util.Calendar

object ChangeScenarios extends ScenarioBase {

  val abandonAndRestoreChangeScn: ScenarioBuilder =
    setupAuthenticatedSession("Abandon and then Restore Change")
      .exec(
        http("abandon change")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/abandon")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )
      .exec(
        http("restore change")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/restore")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )

  val submitChangeScn: ScenarioBuilder = {
    setupAuthenticatedSession("Submit Change")
      .exec(
        http("Create Change")
          .post("/changes/")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody(s"""{"project":"${testConfig.project}",
            |"branch":"master",
            |"subject":"Test commit subject - ${Calendar.getInstance().getTime}"}""".stripMargin))
          .check(regex("_number\":(\\d+),").saveAs("newChangeNumber"))
      )
      .exec(
        http("Approve Change")
          .post(s"/changes/${testConfig.project}~#{newChangeNumber}/revisions/1/review")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{"labels":{"Code-Review":2}}"""))
          .asJson
      )
      .exec(
        http("Submit Change")
          .post(s"/changes/${testConfig.project}~#{newChangeNumber}/revisions/1/submit")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )
  }

  override val scns: List[ScenarioBuilder] = List(abandonAndRestoreChangeScn, submitChangeScn)
}
