package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

class CloneCommand(val url: String) extends GitScenarioBase {

  override def scn: ScenarioBuilder =
    scenario(s"Clone Command over $protocol")
      .feed(refSpecFeeder.circular)
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
              s"$url/${testConfig.project}",
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
            s"clone",
            s"$url/${testConfig.project}",
            "#{refSpec}",
            ignoreFailureRegexps = List(".*want.+not valid.*"),
            requestName = s"Clone over $protocol"
          )
        )
      )
}
