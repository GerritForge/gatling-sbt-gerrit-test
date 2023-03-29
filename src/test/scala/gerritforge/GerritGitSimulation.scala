package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.git.{CloneCommand, CreateChangeCommand, PushCommand}
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

class GerritGitSimulation extends Simulation {

  val gitProtocol = GitProtocol()

  val scenarios = (testConfig.sshUrl ++ testConfig.httpUrl)
    .flatMap(
      url =>
        List(
          CloneCommand(url).scn,
          CreateChangeCommand(url).scn,
          PushCommand(url).scn
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
