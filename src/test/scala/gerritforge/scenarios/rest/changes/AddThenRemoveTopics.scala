package gerritforge.scenarios.rest.changes

import gerritforge.config.GerritTestConfig.gerritTestConfig
import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object AddThenRemoveTopics extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Add then Remove Topics")
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Add Topic")
          .put(s"/changes/${simulationConfig.encodedProject}~#{changeNumber}/topic")
          .headers(addApiHeaders(gerritTestConfig.xsrfToken))
          .body(
            StringBody("""{"topic":"testTopic"}""")
          )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Remove Topic")
          .delete(s"/changes/${simulationConfig.encodedProject}~#{changeNumber}/topic")
          .headers(addApiHeaders(gerritTestConfig.xsrfToken, contentType = None))
      )
      .pause(pauseDuration, pauseStdDev)
}
