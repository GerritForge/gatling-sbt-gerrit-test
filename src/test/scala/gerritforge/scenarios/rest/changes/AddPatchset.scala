package gerritforge.scenarios.rest.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object AddPatchset extends ChangeScenarioBase {

  override def scn: ScenarioBuilder =
    setupAuthenticatedSession("Add Patchset")
      .feed(userIdFeeder.circular)
      .feed(randomFeeder)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Add Patchset")
          .put(s"/changes/${testConfig.encodedProject}~#{changeNumber}/message")
          .headers(addApiHeaders(testConfig.xsrfToken))
          .body(
            StringBody(
              s"""{"message":"New commit message #{randomValue}\n\nChange-Id: #{changeId}\n","notify": "NONE"}"""
            )
          )
      )
}
