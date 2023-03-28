package gerritforge.restscenarios.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.structure.ScenarioBuilder

object AddThenRemoveReviewer extends ChangeScenarioBase {

  override def simulationName: String = "ADD_THEN_REMOVE_REVIEWER"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Add and Remove Reviewer")
      .exec(listChangeWithHashtag(simulationName))
      .exec(pickRandomChange)
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
