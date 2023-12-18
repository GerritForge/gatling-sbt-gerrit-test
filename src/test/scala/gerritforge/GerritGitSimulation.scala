package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritRestSimulation.allRestScenarios
import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.git.{CloneCommand, CreateChangeCommand}
import io.gatling.core.Predef._

class GerritGitSimulation extends Simulation {

  val maybeSshUrl = testConfig.sshUrl.filter(_.nonEmpty)
  val maybeHttpUrl = testConfig.httpUrl
    .filter(_.nonEmpty)
    .map(url => s"$url/a")

  val scenarios =
    (maybeSshUrl ++ maybeHttpUrl)
      .flatMap(
        url =>
          List(
            new CloneCommand(url).scn,
            new CreateChangeCommand(url, allRestScenarios.map(_.scenarioName)).scn
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
