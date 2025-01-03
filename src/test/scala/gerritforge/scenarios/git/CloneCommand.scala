package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GitRequestSession
import com.github.barbasa.gatling.git.GitRequestSession.MasterRef
import com.github.barbasa.gatling.git.request.builder.GitRequestBuilder
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.git.backend.GitServer
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

class CloneCommand(val gitServer: GitServer, val url: String) extends GitScenarioBase {

  override def scnActions: ChainBuilder =
    feed(userIdFeeder.circular)
      .exec(
        new GitRequestBuilder(
          GitRequestSession(
            "clone",
            gitServer.gitUrl(url, simulationConfig.repository),
            MasterRef,
            ignoreFailureRegexps = List(".*want.+not valid.*"),
            requestName = s"Clone over $protocol",
            deleteWorkdirOnExit = true,
            failOnDeleteErrors = false
          )
        )
      )
      .pause(pauseDuration, pauseType)

  override def scnTitle: String = s"Clone Command over $protocol"
}
