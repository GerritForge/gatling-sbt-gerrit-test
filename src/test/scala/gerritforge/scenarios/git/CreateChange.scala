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
      .feed(feeder.circular)
      .feed(userIdFeeder)
      .foreach(hashtagLoop, "hashtagId") {
        exec(
          new GitRequestBuilder(
            GitRequestSession(
              "push",
              s"$url/${testConfig.encodedProject}",
              "#{refSpec}"
            )
          )
        ).pause(pauseDuration, pauseStdDev)
          .exec(
            new GitRequestBuilder(
              GitRequestSession(
                "push",
                s"$url/${testConfig.encodedProject}",
                "HEAD:refs/for/#{refSpec}",
                computeChangeId = true,
                pushOptions = "t=#{hashtagId},t=#{userId}"
              )
            )
          )
          .pause(pauseDuration, pauseStdDev)
      }

}
