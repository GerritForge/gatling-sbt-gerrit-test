package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

object MarkChangeWIP extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Make change WIP")
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Mark Change as WIP",
          "/wip"
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Mark Change as Ready",
          "/ready"
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
