package gerritforge

import gerritforge.restscenarios.changes._
//import gerritforge.restscenarios.tags.{CreateTag, DeleteTag}
import io.gatling.core.scenario.Simulation

trait SimulationBase extends Simulation {

  val authenticatedScenarios = List(
    AbandonThenRestoreChange,
    AddThenRemoveHashtags,
    AddThenRemoveReviewer,
    AddThenRemoveTopics,
    ChangePrivateState,
    DeleteVote
//    MarkChangeWIP,
//    PostComment,
//    SubmitChangeScn,
//    CreateTag,
//    DeleteTag,
//    AddPatchset
  )

  val anonymousScenarios = List(
    ListThenGetDetails
  )

  val allScenarios: Seq[PauseSimulation] = authenticatedScenarios ++ anonymousScenarios

  val hashtagLoop = allScenarios.map(_.simulationName).to(LazyList).lazyAppendedAll(allScenarios)
}
