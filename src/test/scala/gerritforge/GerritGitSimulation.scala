package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.git.{CloneCommand, CreateChangeCommand}
import io.gatling.core.Predef._

class GerritGitSimulation extends SimulationBase {

  val scenarios =
    (testConfig.sshUrl.filterNot(_.isEmpty) ++ testConfig.httpUrl.filterNot(_.isEmpty))
      .flatMap(
        url =>
          List(
            CloneCommand(url).scn,
            CreateChangeCommand(url, authenticatedScenarios.map(_.scenarioName)).scn
          )
      )
      .toList

  setUp(
    scenarios.map(
      _.inject(
        rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration)
      )
    )
  ).protocols(GitProtocol).maxDuration(testConfig.duration)
}
