package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

case class Clone(url: String) extends GitScenarioBase {
  override def scn: ScenarioBuilder =
    scenario(s"Clone Command over $protocol")
      .feed(refSpecFeeder)
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "clone",
            s"$url/${testConfig.encodedProject}",
            "#{refSpec}",
            ignoreFailureRegexps = List(".*want.+not valid.*")
          )
        )
      )
}
