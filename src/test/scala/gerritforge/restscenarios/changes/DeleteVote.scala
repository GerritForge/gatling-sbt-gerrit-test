package gerritforge.restscenarios.changes

import io.gatling.core.structure.ScenarioBuilder

object DeleteVote extends ChangeScenarioBase {

  override def simulationName: String = "DELETE_VOTE"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Delete Vote")
      .feed(hashtagFeeder)
      .exec(
        listChangeWithHashtag
      )
      .exec(pickRandomChange)
      .exec(
        authenticatedChangesPostRequest(
          "Vote On Change",
          "/revisions/1/review",
          """{"labels":{"Code-Review":-1}}""".stripMargin
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Remove Vote for Label",
          "/reviewers/self/votes/Code-Review/delete",
          """{"notify": "NONE"}"""
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
