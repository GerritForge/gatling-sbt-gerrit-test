package gerritforge.scenarios.rest.tags

import gerritforge.config.GerritTestConfig.gerritTestConfig
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.rest.RestScenarioBase
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object CreateAndDeleteTag extends RestScenarioBase {

  private val timestamp = System.currentTimeMillis()

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Create a new Tag")
      .exec { session =>
        session.setAll("userId" -> session.userId)
      }
      .exec(
        http("create tag")
          .put(s"/projects/${simulationConfig.encodedProject}/tags/tag-$timestamp-#{userId}")
          .headers(addApiHeaders(gerritTestConfig.xsrfToken))
          .body(StringBody("""{"revision":"HEAD"}"""))
          .asJson
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("delete tag")
          .delete(s"/projects/${simulationConfig.encodedProject}/tags/tag-$timestamp-#{userId}")
          .headers(addApiHeaders(gerritTestConfig.xsrfToken, None))
      )
}
