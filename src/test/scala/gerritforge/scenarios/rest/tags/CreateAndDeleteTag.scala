package gerritforge.scenarios.rest.tags

import gerritforge.config.GerritConfig.gerritConfig
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.rest.RestScenarioBase
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

object CreateAndDeleteTag extends RestScenarioBase {

  private val timestamp = System.currentTimeMillis()

  override val scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .exec { session =>
        session.setAll("userId" -> session.userId)
      }
      .exec(
        http("create tag")
          .put(s"/projects/${simulationConfig.encodedProject}/tags/tag-$timestamp-#{userId}")
          .headers(addApiHeaders(gerritConfig.xsrfToken))
          .body(StringBody("""{"revision":"HEAD"}"""))
          .asJson
      )
      .pause(pauseDuration, pauseType)
      .exec(
        http("delete tag")
          .delete(s"/projects/${simulationConfig.encodedProject}/tags/tag-$timestamp-#{userId}")
          .headers(addApiHeaders(gerritConfig.xsrfToken, None))
      )

  override def scnTitle: String = "Create a new Tag"
}
