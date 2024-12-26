package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

object MarkChangeWIP extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseType)
      .exec(
        authenticatedChangesPostRequest(
          "Mark Change as WIP",
          "/wip"
        )
      )
      .pause(pauseDuration, pauseType)
      .exec(
        authenticatedChangesPostRequest(
          "Mark Change as Ready",
          "/ready"
        )
      )
      .pause(pauseDuration, pauseType)

  override def scnTitle: String = "Make change WIP"
}
