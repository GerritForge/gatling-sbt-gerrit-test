package gerritforge

import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.protocol.GitProtocol
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritRealLifeSimulation._
import gerritforge.GerritTestConfig.testConfig
import gerritforge.SimulationUtil.httpProtocol
import gerritforge.scenarios.git.{CloneCommand, CreateChangeCommand}
import gerritforge.scenarios.rest.changes.{AbandonThenRestoreChange, PostComment, SubmitChange}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration.FiniteDuration
import io.gatling.core.Predef.normalPausesWithStdDevDuration
import io.gatling.core.controller.inject.closed.ClosedInjectionStep

import scala.concurrent.duration._

// This simulation is based on the traffic extracted from the cue-lang project logs.
// By analyzing the logs we defined and reproduced the traffic shape (command
// types and their frequency) over a day.
class GerritRealLifeSimulation extends Simulation {

  def getScenarioProfile(duration: FiniteDuration): Seq[ClosedInjectionStep] = {
    Seq(
      constantConcurrentUsers(testConfig.numUsers / 2) during duration / 3,
      rampConcurrentUsers(testConfig.numUsers)
        .to(testConfig.numUsers) during duration / 3,
      constantConcurrentUsers(testConfig.numUsers / 2) during duration / 3
    )
  }

  setUp(
    httpCloneScenario
      .inject(
        getScenarioProfile(httpCloneDuration)
      )
      .protocols(GitProtocol),
    receivePackScenario.inject(
      getScenarioProfile(receivePackDuration)
    ),
    submitScenario
      .inject(
        getScenarioProfile(submitDuration)
      )
      .protocols(httpProtocol),
    createChangeCommandScenario
      .inject(
        getScenarioProfile(abandonDuration + postCommentDuration)
      )
      .protocols(httpProtocol)
      .andThen(
        abandonScenario
          .inject(
            getScenarioProfile(abandonDuration)
          )
          .protocols(httpProtocol),
        postCommentScenario
          .inject(
            getScenarioProfile(postCommentDuration)
          )
          .protocols(httpProtocol)
      )
  ).maxDuration(testConfig.duration)
}

object GerritRealLifeSimulation {
  implicit val gitConfig: GatlingGitConfiguration = GatlingGitConfiguration()
  private val httpUrl: String                     = testConfig.httpUrl.get

  private val refSpecFeederMaster =
    (1 to testConfig.numUsers) map { _ =>
      Map("refSpec" -> MasterRef)
    }

  private val pauseLength = 3 seconds
  private val pauseStdDev = normalPausesWithStdDevDuration(1 second)

  private val httpCloneScenario: ScenarioBuilder = scenario(s"Clone Command over HTTP")
    .feed(refSpecFeederMaster.circular)
    .pause(
      pauseLength,
      pauseStdDev
    )
    .exec(
      new GitRequestBuilder(
        GitRequestSession(
          "clone",
          s"$httpUrl/${testConfig.project}",
          s"#{refSpec}",
          ignoreFailureRegexps = List(".*want.+not valid.*")
        )
      )
    )

  private val postCommentScenario: ScenarioBuilder = PostComment.scn
  private val receivePackScenario                  = new CloneCommand(httpUrl).scn
  private val submitScenario: ScenarioBuilder      = SubmitChange.scn
  private val abandonScenario: ScenarioBuilder     = AbandonThenRestoreChange.scn
  private val createChangeCommandScenario =
    new CreateChangeCommand(
      testConfig.gitBackend,
      httpUrl,
      Seq("AbandonThenRestoreChange", "PostComment")
    ).scn

  // Define the distribution of the traffic generated by each scenario in the simulation by assigning
  // a percentage of the whole simulation duration
  private val scenariosPct = Map(
    httpCloneScenario   -> 82,
    postCommentScenario -> 11,
    receivePackScenario -> 4,
    submitScenario      -> 2,
    abandonScenario     -> 1
  )

  private val httpCloneDuration   = testConfig.duration * scenariosPct(httpCloneScenario) / 100
  private val postCommentDuration = testConfig.duration * scenariosPct(postCommentScenario) / 100
  private val receivePackDuration = testConfig.duration * scenariosPct(receivePackScenario) / 100
  private val submitDuration      = testConfig.duration * scenariosPct(submitScenario) / 100
  private val abandonDuration     = testConfig.duration * scenariosPct(abandonScenario) / 100
}
