package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import gerritforge.scenarios.git.backend.GitServer
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

class CreateChangeCommand(val gitServer: GitServer, val url: String, scenarioHashtags: Seq[String])
    extends GitScenarioBase {

  val hashtagLoop = scenarioHashtags.to(LazyList).lazyAppendedAll(scenarioHashtags)

  override def scn: ScenarioBuilder =
    scenario(s"Create Change Command over $protocol")
      .feed(gitServer.refSpecFeeder)
      .feed(userIdFeeder.circular)
      .doIf { _ =>
        true
      } {
        exec { session =>
          alreadyFedUsers = session("userId").as[String] :: alreadyFedUsers
          session
        }.exec(
          new GitRequestBuilder(
            // We only do a "git pull" once to setup the client environment.
            // All the changes created will be chained.
            GitRequestSession(
//              s"${gitServer.baseHttpUrl(url)}/${testConfig.project}${gitServer.httpUrlSuffix}",
              "pull",
              s"$url/${testConfig.project}",
              MasterRef,
              userId = "#{userId}",
              requestName = s"Pull to setup Push over $protocol",
              ignoreFailureRegexps = List(".*want.+not valid.*"),
              repoDirOverride = "/tmp/#{userId}"
            )
          )
        )
      }
      .exec(
        gitServer
          .createChange(
            url,
            "#{refSpec}",
            "#{userId}",
            protocol,
            scenarioHashtags
          )
      )
      .pause(pauseDuration, pauseStdDev)
}
