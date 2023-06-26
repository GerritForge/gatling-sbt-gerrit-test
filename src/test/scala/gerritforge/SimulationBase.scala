package gerritforge

import gerritforge.scenarios.ScenarioBase
import gerritforge.scenarios.rest.changes._
import gerritforge.scenarios.rest.tags._
import io.gatling.core.scenario.Simulation

trait SimulationBase extends Simulation {

  val authenticatedScenarios = List(
//    AbandonThenRestoreChange,
//    AddThenRemoveHashtags,
//    AddThenRemoveReviewer,
//    AddThenRemoveTopics,
//    ChangePrivateState,
//    DeleteVote,
//    MarkChangeWIP,
//    PostComment,
    SubmitChange
//    CreateTag,
//    DeleteTag,
//    AddPatchset
  )

  val anonymousScenarios = List(
    ListThenGetDetails
  )

  val allRestScenarios: Seq[ScenarioBase] = authenticatedScenarios// ++ anonymousScenarios
}
