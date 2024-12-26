package gerritforge.scenarios.rest.changes

import gerritforge.config.GerritConfig.gerritConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

object AddThenRemoveReviewer extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseType)
      .exec(
        authenticatedChangesPostRequest(
          "Add Reviewer",
          "/reviewers",
          s"""{"reviewer":${gerritConfig.reviewerAccount}}"""
        )
      )
      .pause(pauseDuration, pauseType)
      .exec(
        authenticatedChangesPostRequest(
          "Remove Reviewer",
          s"/reviewers/${gerritConfig.reviewerAccount}/delete",
          """{"notify": "NONE"}"""
        )
      )
      .pause(pauseDuration, pauseType)

  override def scnTitle: String = "Add and Remove Reviewer"
}
