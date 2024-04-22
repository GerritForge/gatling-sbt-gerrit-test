package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import gerritforge.scenarios.git.backend.GitServer
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

class CloneCommand(val gitServer: GitServer, val url: String) extends GitScenarioBase {

  override def scn: ScenarioBuilder =
    scenario(s"Clone Command over $protocol")
      .feed(gitServer.refSpecFeeder)
      .feed(userIdFeeder.circular)
      .doIf { session =>
        !alreadyFedUsers.contains(session("userId").as[String])
      } {
        exec { session =>
          alreadyFedUsers = session("userId").as[String] :: alreadyFedUsers
          session
        }.exec(
          new GitRequestBuilder(
            //We push a new refspec as the subsequent clone needs the ref to exist.
            GitRequestSession(
              "push",
              s"${gitServer.baseHttpUrl(url)}/${testConfig.project}${gitServer.httpUrlSuffix}",
              s"#{refSpec}",
              requestName = s"Push to setup Clone over $protocol"
            )
          )
        )
      }
      .pause(pauseDuration, pauseStdDev)
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "clone",
            s"${gitServer.baseHttpUrl(url)}/${testConfig.project}${gitServer.httpUrlSuffix}",
            "#{refSpec}",
            ignoreFailureRegexps = List(".*want.+not valid.*"),
            requestName = s"Clone over $protocol"
          )
        )
      )
}
