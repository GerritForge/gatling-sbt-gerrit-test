package gerritforge

import gerritforge.GerritRestSimulation.{allRestScenarios, authenticatedScenarios}
import gerritforge.GerritTestConfig._
import gerritforge.SimulationUtil.httpProtocol
import gerritforge.scenarios.ScenarioBase
import gerritforge.scenarios.rest.changes._
import gerritforge.scenarios.rest.tags.{CreateAndDeleteMultipleTags, CreateAndDeleteTag}
import io.gatling.core.Predef._

import scala.concurrent.duration.FiniteDuration

class GerritRestSimulation extends Simulation {

  val scenarios =
    if (testConfig.restRunAnonymousUser)
      allRestScenarios
    else authenticatedScenarios

  val pauseStdDevSecs = 5
  setUp(
    scenarios.toList.map(
      _.scn
        .inject(rampConcurrentUsers(1) to testConfig.numUsers during testConfig.duration)
        .pauses(normalPausesWithStdDevDuration(FiniteDuration(pauseStdDevSecs, "seconds")))
    )
  ).protocols(httpProtocol).maxDuration(testConfig.duration)
}

object GerritRestSimulation {
  val authenticatedScenarios = List(
    AbandonThenRestoreChange,
    AddThenRemoveHashtags,
    AddThenRemoveReviewer,
    AddThenRemoveTopics,
    ChangePrivateState,
    DeleteVote,
    MarkChangeWIP,
    PostComment,
    SubmitChange,
    CreateAndDeleteMultipleTags,
    CreateAndDeleteTag,
    AddPatchset
  )

  val anonymousScenarios = List(
    ListThenGetDetails
  )

  val allRestScenarios: Seq[ScenarioBase] = authenticatedScenarios ++ anonymousScenarios
}
