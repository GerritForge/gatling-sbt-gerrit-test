package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

class CreateChangeCommand(val url: String, scenarioHashtags: Seq[String]) extends GitScenarioBase {

  val hashtagLoop = scenarioHashtags.to(LazyList).lazyAppendedAll(scenarioHashtags)
  override val refSpecFeeder: IndexedSeq[Map[String, String]] =
    (1 to testConfig.numUsers) map { _ =>
      Map("refSpec" -> "refs/for/master")
    }

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
            // We only do a "git pull" once to setup the client environment.
            // All the changes created will be chained.
            GitRequestSession(
              "pull",
              s"$url/${testConfig.project}",
              MasterRef,
              userId = "#{userId}",
              requestName = s"Pull to setup Push over $protocol",
              ignoreFailureRegexps = List(".*want.+not valid.*"),
              repoDirOverride = s"/tmp/$protocol-#{userId}"
            )
          )
        )
      }
      .foreach(hashtagLoop, "hashtagId") {
        exec(
          new GitRequestBuilder(
            GitRequestSession(
              "push",
              s"$url/${testConfig.project}",
              "HEAD:#{refSpec}",
              computeChangeId = true,
              pushOptions = s"hashtag=#{hashtagId},hashtag=#{userId}",
              userId = "#{userId}",
              requestName = s"Push new change over $protocol",
              repoDirOverride = s"/tmp/$protocol-#{userId}"
            )
          )
        ).pause(pauseDuration, pauseStdDev)
      }
}
