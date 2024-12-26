package gerritforge.scenarios.rest.changes

import gerritforge.config.GerritConfig.gerritConfig
import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

object AddThenRemoveTopics extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseType)
      .exec(
        http("Add Topic")
          .put(s"/changes/${simulationConfig.encodedProject}~#{changeNumber}/topic")
          .headers(addApiHeaders(gerritConfig.xsrfToken))
          .body(
            StringBody("""{"topic":"testTopic"}""")
          )
      )
      .pause(pauseDuration, pauseType)
      .exec(
        http("Remove Topic")
          .delete(s"/changes/${simulationConfig.encodedProject}~#{changeNumber}/topic")
          .headers(addApiHeaders(gerritConfig.xsrfToken, contentType = None))
      )
      .pause(pauseDuration, pauseType)

  override def scnTitle: String = "Add then Remove Topics"
}
