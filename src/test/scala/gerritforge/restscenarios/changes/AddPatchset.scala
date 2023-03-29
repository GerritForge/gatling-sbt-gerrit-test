package gerritforge.restscenarios.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object AddPatchset extends ChangeScenarioBase {

  override def scn: ScenarioBuilder =
    setupAuthenticatedSession("Add Patchset")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Add Patchset")
          .put(s"/changes/${testConfig.encodedProject}~#{changeNumber}/edit/test-patchset.txt")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{"binary_content":"data:text/plain;base64,c29tZSB0ZXN0Cg=="}"""))
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("Publish Patchset")
          .post(s"/changes/${testConfig.encodedProject}~#{changeNumber}/edit:publish")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{"notify":"NONE"}"""))
      )
      .pause(pauseDuration, pauseStdDev)
}
