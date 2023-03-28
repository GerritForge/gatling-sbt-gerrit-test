package gerritforge.restscenarios.changes

import io.gatling.core.structure.ScenarioBuilder

object MarkChangeWIP extends ChangeScenarioBase {

  override def simulationName: String = "MARK_CHANGE_WIP_SCENARIO"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Make change WIP")
      .exec(listChangeWithHashtag(simulationName))
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
