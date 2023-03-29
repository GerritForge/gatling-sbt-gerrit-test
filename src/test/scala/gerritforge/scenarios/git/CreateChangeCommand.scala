package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.GerritTestConfig._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

case class CreateChangeCommand(url: String) extends GitScenarioBase {

  override def scn: ScenarioBuilder =
    scenario(s"Create Change Command over $protocol")
      .feed(feeder.circular)
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "push",
            s"$url/${testConfig.encodedProject}",
            "HEAD:refs/for/#{refSpec}",
            force = true,
            computeChangeId = true,
            ignoreFailureRegexps = List(".*no common ancestry.*"),
            pushOptions = List("t=my-test")
          )
        )
      )
}
