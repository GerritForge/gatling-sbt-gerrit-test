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
              "pull",
              s"$url/${testConfig.project}",
              "refs/heads/master",
              ignoreFailureRegexps = List(".*want.+not valid.*"),
              repoDirOverride = "/tmp/user-#{userId}"
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
              "HEAD:refs/for/master",
              computeChangeId = true,
              pushOptions = s"hashtag=#{hashtagId},hashtag=#{userId}",
              userId = "#{userId}",
              requestName = "Push to new branch",
              repoDirOverride = "/tmp/user-#{userId}"
            )
          )
        ).pause(pauseDuration, pauseStdDev)
      }
}
