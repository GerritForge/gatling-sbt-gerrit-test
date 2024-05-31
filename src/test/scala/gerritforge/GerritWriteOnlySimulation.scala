package gerritforge

import com.github.barbasa.gatling.git.GatlingGitConfiguration
import gerritforge.GerritWriteOnlySimulation._
import gerritforge.SimulationUtil.httpProtocol
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.git.CreateChangeCommand
import gerritforge.scenarios.rest.changes.{PostComment, SubmitChange}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import scala.concurrent.duration.FiniteDuration
import io.gatling.core.controller.inject.closed.ClosedInjectionStep

class GerritWriteOnlySimulation extends Simulation {

  def getScenarioProfile(duration: FiniteDuration): Seq[ClosedInjectionStep] = {
    Seq(
      constantConcurrentUsers(simulationConfig.numUsers) during duration
    )
  }

  setUp(
    submitScenario
      .inject(
        getScenarioProfile(simulationConfig.duration)
      )
      .protocols(httpProtocol),
    createChangeCommandScenario
      .inject(
        getScenarioProfile(simulationConfig.duration)
      )
      .protocols(httpProtocol),
    postCommentScenario
      .inject(
        getScenarioProfile(simulationConfig.duration)
      )
      .protocols(httpProtocol)
  ).maxDuration(simulationConfig.duration)
}

object GerritWriteOnlySimulation {
  implicit val gitConfig: GatlingGitConfiguration = GatlingGitConfiguration()
  private val httpUrl: String                     = simulationConfig.httpUrl.get

  private val postCommentScenario: ScenarioBuilder = PostComment(List.empty).scn
  private val submitScenario: ScenarioBuilder      = SubmitChange.scn
  private val createChangeCommandScenario =
    new CreateChangeCommand(
      simulationConfig.gitBackend,
      httpUrl,
      Seq("CreateChangeCommand")
    ).scn
}
