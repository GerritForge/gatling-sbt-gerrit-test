package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object AbandonThenRestoreChange extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Abandon and then Restore Change")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
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
