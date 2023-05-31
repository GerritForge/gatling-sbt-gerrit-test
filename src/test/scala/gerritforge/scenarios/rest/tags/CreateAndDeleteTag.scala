package gerritforge.scenarios.rest.tags

import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.rest.RestScenarioBase
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object CreateAndDeleteTag extends RestScenarioBase {

  private val timestamp = System.currentTimeMillis()

  override val scn: ScenarioBuilder =
  setupCookies("Create a new Tag")
      .exec { session =>
        session.setAll("userId" -> session.userId)
      }
      .exec(
        http("create tag")
          .put(s"/projects/${testConfig.encodedProject}/tags/tag-$timestamp-#{userId}")
          .headers(addApiHeaders(testConfig.xsrfToken))
          .body(StringBody("""{"revision":"HEAD"}"""))
          .asJson
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("delete tag")
          .delete(s"/projects/${testConfig.encodedProject}/tags/tag-$timestamp-#{userId}")
          .headers(addApiHeaders(testConfig.xsrfToken, None))
      )
}
