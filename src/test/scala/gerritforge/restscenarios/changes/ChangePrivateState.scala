package gerritforge.restscenarios.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object ChangePrivateState extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupCookies("Change Private State")
      .exec(createChange.check(regex("_number\":(\\d+),").saveAs("changeNumber")))
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Mark Private",
          "/private",
          s"""{"message":"Marking change #{changeNumber} as private for testing purposes"}"""
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "UnMark Private",
          "/private.delete"
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
