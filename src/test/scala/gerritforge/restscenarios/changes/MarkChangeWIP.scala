package gerritforge.restscenarios.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object MarkChangeWIP extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupCookies("Make change WIP")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Mark Change as WIP",
          "/wip"
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Mark Change as Ready",
          "/ready"
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
