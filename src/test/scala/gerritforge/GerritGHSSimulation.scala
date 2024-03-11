package gerritforge

import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.protocol.GitProtocol
import com.github.barbasa.gatling.git.{GatlingGitConfiguration, GitRequestSession}
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritGHSSimulation.{httpProtocol, postCommentDuration, postCommentScenario}
import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.ScenarioBase
import gerritforge.scenarios.rest.changes.{PostComment, SubmitChange}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import io.gatling.core.Predef.normalPausesWithStdDevDuration
import io.gatling.http.Predef.http
import io.gatling.http.protocol.HttpProtocol

class GerritGHSSimulation extends Simulation {
  implicit val gitConfig      = GatlingGitConfiguration()
  private val httpUrl: String = testConfig.httpUrl.get

  val refSpecFeeder =
    (1 to testConfig.numUsers) map { _ =>
      Map("refSpec" -> MasterRef)
    }

  private val pauseLength = FiniteDuration(3, TimeUnit.SECONDS)
  private val pauseStdDev = normalPausesWithStdDevDuration(FiniteDuration(1, TimeUnit.SECONDS))

  private val httpClonePct = 0.82
  private val httpCloneDuration =
    FiniteDuration((testConfig.duration * httpClonePct).toSeconds, TimeUnit.SECONDS)
  private val httpClone: ScenarioBuilder = scenario(s"Clone Command over HTTP")
    .feed(refSpecFeeder.circular)
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

  setUp(
    httpClone
      .inject(
        constantConcurrentUsers(testConfig.numUsers) during httpCloneDuration
      )
      .protocols(GitProtocol),
    postCommentScenario.scn
      .inject(
        constantConcurrentUsers(testConfig.numUsers) during postCommentDuration
      )
      .protocols(httpProtocol)
  ).maxDuration(testConfig.duration)
}

object GerritGHSSimulation {
  val postCommentScenario    = PostComment
  private val postCommentPct = 0.2
  private val postCommentDuration =
    FiniteDuration((testConfig.duration * postCommentPct).toSeconds, TimeUnit.SECONDS)

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
