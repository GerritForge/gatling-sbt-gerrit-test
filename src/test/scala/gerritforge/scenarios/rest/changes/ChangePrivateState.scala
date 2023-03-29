package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

object ChangePrivateState extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Change Private State")
      .feed(userIdFeeder.circular)
      .exec(listChangeWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
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
