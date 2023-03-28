package gerritforge.restscenarios.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object AddThenRemoveReviewer extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Add and Remove Reviewer")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Add Reviewer",
          "/reviewers",
          s"""{"reviewer":${testConfig.reviewerAccount}}"""
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Remove Reviewer",
          s"/reviewers/${testConfig.reviewerAccount}/delete",
          """{"notify": "NONE"}"""
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
