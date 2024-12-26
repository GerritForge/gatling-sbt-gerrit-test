package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

object AddThenRemoveHashtags extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseType)
      .exec(
        authenticatedChangesPostRequest(
          "Add Hashtag",
          "/hashtags",
          """{"add":["test", "test1", "test2"]}"""
        )
      )
      .pause(pauseDuration, pauseType)
      .exec(
        authenticatedChangesPostRequest(
          "Remove Hastag",
          "/hashtags",
          """{"remove":["test"]}"""
        )
      )
      .pause(pauseDuration, pauseType)

  override def scnTitle: String = "Add then Remove Hashtags"
}
