package gerritforge

import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.protocol.GitProtocol
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritGHSSimulation._
import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.git.{CloneCommand, CreateChangeCommand}
import gerritforge.scenarios.rest.changes.{AbandonThenRestoreChange, PostComment, SubmitChange}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import io.gatling.core.Predef.normalPausesWithStdDevDuration
import io.gatling.http.Predef.http
import io.gatling.http.protocol.HttpProtocol

import java.net.InetAddress
import java.util.UUID

class GerritGHSSimulation extends Simulation {

  setUp(
    httpClone
      .inject(
        constantConcurrentUsers(testConfig.numUsers) during httpCloneDuration
      )
      .protocols(GitProtocol),
    receivePackScenario.scn.inject(
      constantConcurrentUsers(testConfig.numUsers) during receivePackDuration
    ),
    postCommentScenario
      .inject(
        constantConcurrentUsers(testConfig.numUsers) during postCommentDuration
      )
      .protocols(httpProtocol),
    submitScenario
      .inject(
        constantConcurrentUsers(testConfig.numUsers) during submitDuration
      )
      .protocols(httpProtocol),
    createChangeCommandScenario
      .inject(
        constantConcurrentUsers(testConfig.numUsers) during abandonDuration
      )
      .protocols(httpProtocol)
      .andThen(
        abandonScenario
          .inject(
            constantConcurrentUsers(testConfig.numUsers) during abandonDuration
          )
          .protocols(httpProtocol)
      )
  ).maxDuration(testConfig.duration)
}

object GerritGHSSimulation {
  implicit val gitConfig: GatlingGitConfiguration = GatlingGitConfiguration()
  val httpUrl: String                             = testConfig.httpUrl.get

  val hostname = InetAddress.getLocalHost.getHostName
  val refSpecFeederMaster =
    (1 to testConfig.numUsers) map { _ =>
      Map("refSpec" -> MasterRef)
    }

  val refSpecFeederMultipleRefs =
    (1 to testConfig.numUsers) map { idx =>
      Map("refSpec" -> s"branch-$hostname-$idx-receive-pack-${UUID.randomUUID()}")
    }

  private val httpClonePct   = 0.82
  private val postCommentPct = 0.11
  private val receivePackPct = 0.04
  private val submitPct      = 0.02
  private val abandonPct     = 0.01

  private val httpCloneDuration =
    FiniteDuration((testConfig.duration * httpClonePct).toSeconds, TimeUnit.SECONDS)
  private val httpClone: ScenarioBuilder = scenario(s"Clone Command over HTTP")
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

  val pauseLength = FiniteDuration(3, TimeUnit.SECONDS)
  val pauseStdDev = normalPausesWithStdDevDuration(FiniteDuration(1, TimeUnit.SECONDS))

  val postCommentScenario: ScenarioBuilder = PostComment.scn

  private val postCommentDuration =
    FiniteDuration((testConfig.duration * postCommentPct).toSeconds, TimeUnit.SECONDS)

  private val receivePackDuration =
    FiniteDuration((testConfig.duration * receivePackPct).toSeconds, TimeUnit.SECONDS)
  val receivePackScenario = new CloneCommand(httpUrl)

  private val submitDuration =
    FiniteDuration((testConfig.duration * submitPct).toSeconds, TimeUnit.SECONDS)
  val submitScenario: ScenarioBuilder = SubmitChange.scn

  private val abandonDuration =
    FiniteDuration((testConfig.duration * abandonPct).toSeconds, TimeUnit.SECONDS)

  val abandonScenario: ScenarioBuilder = AbandonThenRestoreChange.scn

  val createChangeCommandScenario =
    new CreateChangeCommand(httpUrl, Seq("AbandonThenRestoreChange")).scn

  val httpProtocol: HttpProtocol = testConfig.httpUrl
    .map(
      url =>
        http
          .baseUrl(url)
          .inferHtmlResources(
            AllowList(),
            DenyList(""".*\.js""", """.*\.css""", """.*\.ico""", """.*\.woff2""", """.*\.png""")
          )
          .acceptHeader("*/*")
          .acceptEncodingHeader("gzip, deflate")
          .acceptLanguageHeader("en-GB,en;q=0.5")
          .userAgentHeader("gatling-test")
    )
    .getOrElse(
      throw new IllegalArgumentException(
        "GERRIT_HTTP_URL must be defined to run REST-API simulation"
      )
    )
}
