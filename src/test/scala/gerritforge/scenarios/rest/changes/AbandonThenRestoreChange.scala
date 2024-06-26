package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

case class AbandonThenRestoreChange(
    queryFilter: List[String] = List("AbandonThenRestoreChange", "#{userId}")
) extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(queryFilter))
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

  override def scnTitle: String = "Abandon and then Restore Change"
}
