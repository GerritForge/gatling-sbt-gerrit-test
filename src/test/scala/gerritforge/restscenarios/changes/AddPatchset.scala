package gerritforge.restscenarios.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object AddPatchset extends ChangeScenarioBase {

  override def scn: ScenarioBuilder =
    setupAuthenticatedSession("Add Patchset")
      .exec(
        createChange
          .check(
            regex("_number\":(\\d+),").saveAs("changeNumber"),
            regex("change_id\":\"(.*)\",").saveAs("changeId")
          )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Add Patchset")
          .put(s"/changes/${testConfig.encodedProject}~#{changeNumber}/message")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(
            StringBody(
              s"""{"message":"New commit message\n\nChange-Id: #{changeId}\n","notify": "NONE"}"""
            )
          )
      )
      .pause(pauseDuration, pauseStdDev)
}
