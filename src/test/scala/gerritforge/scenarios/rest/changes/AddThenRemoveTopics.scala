package gerritforge.scenarios.rest.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object AddThenRemoveTopics extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Add then Remove Topics")
      .feed(userIdFeeder.circular)
      .exec(listChangeWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Add Topic")
          .put(s"/changes/${testConfig.encodedProject}~#{changeNumber}/topic")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(
            StringBody("""{"topic":"testTopic"}""")
          )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Remove Topic")
          .delete(s"/changes/${testConfig.encodedProject}~#{changeNumber}/topic")
          .headers(postApiHeader(testConfig.xsrfToken, contentType = None))
      )
      .pause(pauseDuration, pauseStdDev)
}
