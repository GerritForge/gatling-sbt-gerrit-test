package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

object DeleteVote extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .exec(
        authenticatedChangesPostRequest(
          "Vote On Change",
          "/revisions/1/review",
          """{"labels":{"Code-Review":-1}}""".stripMargin
        )
      )
      .pause(pauseDuration, pauseType)
      .exec(
        authenticatedChangesPostRequest(
          "Remove Vote for Label",
          "/reviewers/self/votes/Code-Review/delete",
          """{"notify": "NONE"}"""
        )
      )
      .pause(pauseDuration, pauseType)

  override def scnTitle: String = "Delete Vote"
}
