package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

object AddThenRemoveHashtags extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Add then Remove Hashtags")
      .feed(userIdFeeder.circular)
      .exec(listChangeWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Add Hashtag",
          "/hashtags",
          """{"add":["test", "test1", "test2"]}"""
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Remove Hastag",
          "/hashtags",
          """{"remove":["test"]}"""
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
