package gerritforge

import com.github.barbasa.gatling.git.GatlingGitConfiguration
import gerritforge.GerritRealLifeSimulation._
import gerritforge.SimulationUtil.httpProtocol
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.git.backend.Gerrit
import gerritforge.scenarios.git.{CloneCommand, CreateChangeCommand}
import gerritforge.scenarios.rest.changes.{AbandonThenRestoreChange, PostComment, SubmitChange}
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.closed.ClosedInjectionStep
import io.gatling.core.structure.ChainBuilder

import scala.concurrent.duration.FiniteDuration

// This simulation is based on the traffic extracted from the cue-lang project logs.
// By analyzing the logs we defined and reproduced the traffic shape (command
// types and their frequency) over a day.
class GerritRealLifeSimulation extends Simulation {

  def getScenarioProfile(duration: FiniteDuration): Seq[ClosedInjectionStep] = {
    Seq(
      constantConcurrentUsers(simulationConfig.numUsers / 2) during duration / 3,
      rampConcurrentUsers(simulationConfig.numUsers)
        .to(simulationConfig.numUsers) during duration / 3,
      constantConcurrentUsers(simulationConfig.numUsers / 2) during duration / 3
    )
  }

  setUp(
    scenario("Real Life Simulation")
      .randomSwitch(
        simulationConfig.cloneScnPct        -> cloneCommandActions,
        simulationConfig.postCommentScnPct  -> postCommentActions,
        simulationConfig.submitScnPct       -> submitActions,
        simulationConfig.abandonScnPct      -> abandonActions,
        simulationConfig.createChangeScnPct -> createChangeCommandActions
      )
      .inject(
        getScenarioProfile(simulationConfig.duration)
      )
  ).protocols(httpProtocol).maxDuration(simulationConfig.duration)
}

object GerritRealLifeSimulation {
  implicit val gitConfig: GatlingGitConfiguration = GatlingGitConfiguration()
  private val httpUrl: String                     = simulationConfig.httpUrl.get

  private val postCommentActions: ChainBuilder = PostComment(queryFilter = List.empty).scnActions
  private val cloneCommandActions = new CloneCommand(
    Gerrit,
    httpUrl
  ).scnActions
  private val submitActions: ChainBuilder  = SubmitChange.scnActions
  private val abandonActions: ChainBuilder = AbandonThenRestoreChange.scnActions
  private val createChangeCommandActions: ChainBuilder =
    new CreateChangeCommand(
      simulationConfig.gitBackend,
      httpUrl,
      Seq("AbandonThenRestoreChange", "PostComment")
    ).scnActions
}
