package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object SubmitChangeScn extends ChangeScenarioBase {

  override def simulationName: String = "SUBMIT_CHANGE"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Submit Change")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Approve Change",
          "/revisions/1/review",
          """{"labels":{"Code-Review":2}}"""
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Submit Change",
          "/revisions/1/submit"
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
