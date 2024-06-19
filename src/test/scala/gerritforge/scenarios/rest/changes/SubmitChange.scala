package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

object SubmitChange extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Approve Change",
          "/revisions/1/review",
          """{"labels":{"Code-Review":2}}"""
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Submit Change",
          "/revisions/1/submit"
        )
      )
      .pause(pauseDuration, pauseStdDev)

  override def scnTitle: String = "Submit Change"
}
