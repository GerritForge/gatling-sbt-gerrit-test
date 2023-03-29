package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object DeleteVote extends ChangeScenarioBase {

  override def simulationName: String = "DELETE_VOTE"

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Delete Vote")
      .exec(createChange.check(regex("_number\":(\\d+),").saveAs("changeNumber")))
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
