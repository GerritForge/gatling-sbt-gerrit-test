package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.git.{Clone, CreateChange}
import io.gatling.core.Predef._

class GerritGitSimulation extends SimulationBase {

  val gitProtocol = GitProtocol()

  val scenarios = (testConfig.sshUrl ++ testConfig.httpUrl)
    .flatMap(
      url =>
        List(
          Clone(url).scn,
//          ForcePushBranch(url).scn, TODO IS THIS WANTED?
          CreateChange(url, allRestScenarios.map(_.scenarioName)).scn
//          PushNewBranch(url).scn TODO IS THIS WANTED?
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
