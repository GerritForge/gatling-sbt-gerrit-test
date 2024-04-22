package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.git.backend.GitServer
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

class CloneCommand(val gitServer: GitServer, val url: String) extends GitScenarioBase {

  override def scn: ScenarioBuilder =
    scenario(s"Clone Command over $protocol")
      .feed(userIdFeeder.circular)
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "clone",
            gitServer.gitUrl(url, simulationConfig.project),
            MasterRef,
            ignoreFailureRegexps = List(".*want.+not valid.*"),
            requestName = s"Clone over $protocol"
          )
        )
      )
}
