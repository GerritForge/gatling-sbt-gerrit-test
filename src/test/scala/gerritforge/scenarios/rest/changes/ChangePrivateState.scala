package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

object ChangePrivateState extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
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

  override def scnTitle: String = "Change Private State"
}
