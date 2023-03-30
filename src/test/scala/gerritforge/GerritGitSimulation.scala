package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.git.CreateChange
import io.gatling.core.Predef._

class GerritGitSimulation extends SimulationBase {

  val gitProtocol = GitProtocol()

  val scenarios = (testConfig.sshUrl ++ testConfig.httpUrl)
    .flatMap(
      url =>
        List(
//          Clone(url).scn,
          CreateChange(url, authenticatedScenarios.map(_.scenarioName)).scn
          //Push(url).scn TODO IS THIS WANTED?
          //TODO Add forcePushScenario
        )
    )
    .toList

  setUp(
    scenarios.map(
      _.inject(
        rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration)
      )
    )
  ).protocols(gitProtocol)
}
