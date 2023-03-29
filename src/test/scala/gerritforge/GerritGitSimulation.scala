package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.git.{Clone, CreateChange, Push}
import io.gatling.core.Predef._

class GerritGitSimulation extends SimulationBase {

  val scenarios = (testConfig.sshUrl ++ testConfig.httpUrl)
    .flatMap(
      url =>
        List(
          Clone(url).scn,
          CreateChange(url, authenticatedScenarios.map(_.scenarioName)).scn,
          Push(url).scn
        )
    )
    .toList

  setUp(
    scenarios.map(
      _.inject(
        rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration)
      )
    )
  ).protocols(GitProtocol)
}
