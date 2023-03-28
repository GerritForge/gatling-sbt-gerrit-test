package gerritforge.restscenarios.changes

import io.gatling.core.structure.ScenarioBuilder

object ChangePrivateState extends ChangeScenarioBase {

  override def simulationName: String = "CHANGE_PRIVATE_STATE"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Change Private State")
      .exec(listChangeWithHashtag(simulationName))
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
