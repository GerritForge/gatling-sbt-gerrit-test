package gerritforge.scenarios.rest.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object AddPatchset extends ChangeScenarioBase {

  override def scn: ScenarioBuilder =
    setupAuthenticatedSession("Add Patchset")
      .feed(userIdFeeder.circular)
      .exec(listChangeWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
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
}
