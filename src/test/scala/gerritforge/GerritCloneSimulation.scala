package gerritforge

import java.net.InetAddress

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

class GerritCloneSimulation extends Simulation {

  private val hostname = InetAddress.getLocalHost.getHostName
  private val gitProtocol = GitProtocol()
  private val feeder = (1 to testConfig.numUsers) map { idx =>
    Map("refSpec" -> s"branch-$hostname-$idx")
  }

  private val gitSshScenario  = GerritGitScenario(testConfig.sshUrl)
  private val gitHttpScenario = GerritGitScenario(testConfig.httpUrl + "/a")

  private val gitClone = scenario("Git clones from Gerrit")
    .feed(feeder.circular)
    .exec(gitSshScenario.cloneCommand)
    .exec(gitHttpScenario.cloneCommand)

  setUp(
    gitClone.inject(
      rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration)
    )
  ).protocols(gitProtocol)
}
