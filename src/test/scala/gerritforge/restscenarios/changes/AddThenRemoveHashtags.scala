package gerritforge.restscenarios.changes

import io.gatling.core.structure.ScenarioBuilder

object AddThenRemoveHashtags extends ChangeScenarioBase {
  override def simulationName: String = "ADD_THEN_REMOVE_HASHTAG"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Add then Remove Hashtags")
      .feed(hashtagFeeder)
      .exec(
        listChangeWithHashtag
      )
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
