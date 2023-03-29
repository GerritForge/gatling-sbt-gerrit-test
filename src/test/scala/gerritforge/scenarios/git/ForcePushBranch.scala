package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

case class ForcePushBranch(url: String) extends GitScenarioBase {

  override def scn: ScenarioBuilder =
    scenario(s"Git Force Push Command over $protocol")
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "push",
            s"$url/${testConfig.encodedProject}",
            "aBranch",
            force = true
          )
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
