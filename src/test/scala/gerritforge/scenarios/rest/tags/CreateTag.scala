package gerritforge.scenarios.rest.tags

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.UUID

object CreateTag extends TagScenarioBase {

  override def simulationName: String = "CREATE_TAG"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Create a new Tag")
      .feed(tagGroupIds)
      .feed(
        Iterator.continually(Map("tagId" -> s"${System.currentTimeMillis()}-${UUID.randomUUID()}"))
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("create tag")
          .put(s"/projects/${testConfig.encodedProject}/tags/tag-#{tagId}-#{tagGroupId}")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{"revision":"HEAD"}"""))
          .asJson
      )
      .pause(pauseDuration, pauseStdDev)
}
