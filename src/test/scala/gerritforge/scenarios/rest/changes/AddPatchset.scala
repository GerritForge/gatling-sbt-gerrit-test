package gerritforge.scenarios.rest.changes

import gerritforge.config.GerritConfig.gerritConfig
import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

object AddPatchset extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .feed(randomFeeder)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseType)
      .exec(
        http("Add Patchset")
          .put(s"/changes/${simulationConfig.encodedProject}~#{changeNumber}/message")
          .headers(addApiHeaders(gerritConfig.xsrfToken))
          .body(
            StringBody(
              s"""{"message":"New commit message #{randomValue}\n\nChange-Id: #{changeId}\n","notify": "NONE"}"""
            )
          )
      )

  override def scnTitle: String = "Add Patchset"
}
