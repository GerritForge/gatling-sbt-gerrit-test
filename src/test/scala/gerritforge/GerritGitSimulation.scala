package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder

import java.net.InetAddress

class GerritGitSimulation extends Simulation {

  val hostname    = InetAddress.getLocalHost.getHostName
  val gitProtocol = GitProtocol()
  val feeder = (1 to testConfig.numUsers) map { idx =>
    Map("refSpec" -> s"master", "force" -> false)
  }

  val gitSshScenario  = testConfig.sshUrl.map(GerritGitScenario)
  val gitHttpScenario = testConfig.httpUrl.map(_ + "/a").map(GerritGitScenario)

  val gitClone: ScenarioBuilder = scenario("Git clone")
    .feed(feeder.circular)
    .doIf(gitSshScenario.isDefined) {
      exec(gitSshScenario.get.cloneCommand)
    }
    .doIf(gitHttpScenario.isDefined) {
      exec(gitHttpScenario.get.cloneCommand)
    }

  require(
    testConfig.httpUrl.orElse(testConfig.sshUrl).isDefined,
    "Either GERRIT_HTTP_URL or GERRIT_SSH_URL must be defined"
  )

  setUp(
    gitClone.inject(
      atOnceUsers(testConfig.numUsers)
    )
  ).protocols(gitProtocol).maxDuration(testConfig.duration)
}
