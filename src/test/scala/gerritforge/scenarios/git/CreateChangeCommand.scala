package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

class CreateChangeCommand(val url: String, scenarioHashtags: Seq[String]) extends GitScenarioBase {

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
              requestName = s"Create branch over $protocol"
            )
          )
        )
      }
      .pause(pauseDuration, pauseStdDev)
      .foreach(hashtagLoop, "hashtagId") {
        exec(
          new GitRequestBuilder(
            GitRequestSession(
              "push",
              s"$url/${testConfig.project}",
              "HEAD:refs/for/#{refSpec}-#{userId}",
              computeChangeId = true,
              pushOptions = s"hashtag=#{hashtagId},hashtag=#{userId}",
              userId = "#{userId}",
              requestName = s"Push to new branch over $protocol"
            )
          )
        ).pause(pauseDuration, pauseStdDev)
      }
}
