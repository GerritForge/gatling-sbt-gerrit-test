package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritGitScenario._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import GerritTestConfig.testConfig
import java.net.InetAddress

import scala.concurrent.duration._

class GerritGitSimulation extends Simulation {

  val hostname    = InetAddress.getLocalHost.getHostName
  val gitProtocol = GitProtocol()
  val feeder = (1 to testConfig.numUsers) map { idx =>
    Map("refSpec" -> s"branch-$hostname-$idx", "force" -> true)
  }

  val gitSshScenario  = GerritGitScenario(testConfig.sshUrl)
  val gitHttpScenario = GerritGitScenario(testConfig.httpUrl + "/a")

  val gitCloneAndPush = scenario("Git clone and push to Gerrit")
    .feed(feeder.circular)
    .exec(gitSshScenario.cloneCommand)
    .exec(gitSshScenario.pushCommand)
    .exec(gitSshScenario.createChangeCommand)
    .exec(gitHttpScenario.cloneCommand)
    .exec(gitHttpScenario.pushCommand)
    .exec(gitHttpScenario.createChangeCommand)

  setUp(
    gitCloneAndPush.inject(
      rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration)
    )
  ).protocols(gitProtocol)
}
