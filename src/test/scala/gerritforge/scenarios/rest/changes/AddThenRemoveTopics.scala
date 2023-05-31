package gerritforge.scenarios.rest.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object AddThenRemoveTopics extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
  setupCookies("Add then Remove Topics")
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Add Topic")
          .put(s"/changes/${testConfig.encodedProject}~#{changeNumber}/topic")
          .headers(addApiHeaders(testConfig.xsrfToken))
          .body(
            StringBody("""{"topic":"testTopic"}""")
          )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Remove Topic")
          .delete(s"/changes/${testConfig.encodedProject}~#{changeNumber}/topic")
          .headers(addApiHeaders(testConfig.xsrfToken, contentType = None))
      )
      .pause(pauseDuration, pauseStdDev)
}
