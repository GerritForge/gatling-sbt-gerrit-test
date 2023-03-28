package gerritforge.restscenarios.changes

import io.gatling.core.structure.ScenarioBuilder

object AbandonThenRestoreChange extends ChangeScenarioBase {
  override def simulationName: String = "ABANDON_THEN_RESTORE_SCENARIO"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Abandon and then Restore Change")
      .exec(listChangeWithHashtag(simulationName))
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
