package gerritforge

import gerritforge.GerritTestConfig._
import gerritforge.restscenarios.changes._
import gerritforge.restscenarios.tags.CreateTag.httpProtocol
import gerritforge.restscenarios.tags._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import scala.concurrent.duration.FiniteDuration

class GerritRestSimulation extends Simulation {

  val authenticatedScenarios = List(
    AbandonThenRestoreChange.scn,
    AddThenRemoveHashtags.scn,
    AddThenRemoveReviewer.scn,
    AddThenRemoveTopics.scn,
    ChangePrivateState.scn,
    DeleteVote.scn,
    MarkChangeWIP.scn,
    PostComment.scn,
    SubmitChangeScn.scn,
    CreateTag.scn,
    DeleteTag.scn
  )

  val scenarios =
    if (testConfig.restRunAnonymousUser)
      authenticatedScenarios
    else authenticatedScenarios

  require(httpProtocol.isDefined, "GERRIT_HTTP_URL must be defined to run REST-API simulation")

  setUp(
    scenarios.map(
      _.inject(rampConcurrentUsers(1) to testConfig.numUsers during testConfig.duration)
        .pauses(normalPausesWithStdDevDuration(FiniteDuration(5, "seconds")))
    )
  ).protocols(httpProtocol)
}
