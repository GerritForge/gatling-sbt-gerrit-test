package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

case class CreateChange(url: String) extends GitScenarioBase {

  override def scn: ScenarioBuilder =
    scenario(s"Create Change Command over $protocol")
      .feed(feeder)
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "push",
            s"$url/${testConfig.encodedProject}",
            "#{refSpec}"
          )
        )
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "push",
            s"$url/${testConfig.encodedProject}",
            "HEAD:refs/for/#{refSpec}",
            computeChangeId = true
          )
        )
      )
      .pause(pauseDuration, pauseStdDev)

}
