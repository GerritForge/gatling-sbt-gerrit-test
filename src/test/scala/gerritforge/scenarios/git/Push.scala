package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

case class Push(url: String) extends GitScenarioBase {

  override def scn: ScenarioBuilder =
    scenario(s"Git Push Command over $protocol")
      .feed(refSpecFeeder.circular)
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "push",
            s"$url/${testConfig.encodedProject}",
            "#{refSpec}",
            force = "#{force}",
            ignoreFailureRegexps = List(".*no common ancestry.*")
          )
        )
      )
}