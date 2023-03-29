package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

object AbandonThenRestoreChange extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Abandon and then Restore Change")
      .feed(userIdFeeder.circular)
      .exec(listChangeWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "abandon change",
          "/abandon"
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "restore change",
          "/restore"
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
