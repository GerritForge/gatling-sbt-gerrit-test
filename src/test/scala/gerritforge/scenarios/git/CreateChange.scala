package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

case class CreateChange(url: String, scenarioHashtags: Seq[String]) extends GitScenarioBase {

  val hashtagLoop = scenarioHashtags.to(LazyList).lazyAppendedAll(scenarioHashtags)

  override def scn: ScenarioBuilder =
    scenario(s"Create Change Command over $protocol")
      .exec { branchToBeCreated(true) }
      .feed(refSpecFeeder)
      .feed(userIdFeeder.circular)
      .doIf("#{branchToBeCreated}") {
        exec { branchToBeCreated(false) }
          .exec(
            new GitRequestBuilder(
              GitRequestSession(
                "push",
                s"$url/${testConfig.encodedProject}",
                s"#{refSpec}-#{userId}"
              )
            )
          )
          .pause(pauseDuration, pauseStdDev)
      }
      .foreach(hashtagLoop, "hashtagId") {
        exec(
          new GitRequestBuilder(
            GitRequestSession(
              "push",
              s"$url/${testConfig.encodedProject}",
              "HEAD:refs/for/#{refSpec}-#{userId}",
              computeChangeId = true,
              pushOptions = s"hashtag=#{hashtagId},hashtag=#{userId}"
            )
          )
        ).pause(pauseDuration, pauseStdDev)
      }
}
