package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritRestSimulation.allRestScenarios
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.git.{CloneCommand, CreateChangeCommand}
import io.gatling.core.Predef._

class GitSimulation extends Simulation {

  val maybeSshUrl = simulationConfig.sshUrl.filter(_.nonEmpty)
  val maybeHttpUrl = simulationConfig.httpUrl
    .filter(_.nonEmpty)

  val scenarios =
    (maybeSshUrl ++ maybeHttpUrl)
      .flatMap(
        url =>
          List(
            new CloneCommand(simulationConfig.gitBackend, url).scn,
            new CreateChangeCommand(
              simulationConfig.gitBackend,
              url,
              allRestScenarios.map(_.scenarioName)
            ).scn
          )
      )
      .toList

  setUp(
    scenarios.map(
      _.inject(
        rampConcurrentUsers(1) to simulationConfig.numUsers during (simulationConfig.duration)
      )
    )
  ).protocols(GitProtocol).maxDuration(simulationConfig.duration)
}
