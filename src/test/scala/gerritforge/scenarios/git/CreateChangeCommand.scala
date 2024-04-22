package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
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
            GitRequestSession(
              "push",
              s"$url/${testConfig.project}",
              s"#{refSpec}-#{userId}",
              userId = "#{userId}",
              requestName = "Create branch"
            )
          )
        )
      }
      .pause(pauseDuration, pauseStdDev)
      .foreach(hashtagLoop, "hashtagId") {
        exec(
          gitServer.createChange(s"$url/${testConfig.project}", "#{refSpec}-#{userId}", "#{userId}")
        ).pause(pauseDuration, pauseStdDev)
      }
}
