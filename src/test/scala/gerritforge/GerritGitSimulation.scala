package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritGitScenario._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import GerritTestConfig.testConfig

import scala.concurrent.duration._

class GerritGitSimulation extends Simulation {

  val gitProtocol = GitProtocol()
  val feeder = (1 to testConfig.numUsers) map { idx =>
    Map("refSpec" -> s"branch-$idx", "force" -> true)
  }

  val gitSshScenario = GerritGitScenario(testConfig.sshUrl)
  val gitHttpScenario = GerritGitScenario(testConfig.httpUrl)

  val gitSshClone = scenario("Git/SSH clone from Gerrit")
    .feed(feeder.circular)
    .exec(gitSshScenario.cloneCommand)
  val gitSshPush = scenario("Git/SSH push to Gerrit")
    .feed(feeder.circular)
    .exec(gitSshScenario.pushCommand)

  setUp(
    gitSshClone.inject(rampConcurrentUsers (1) to testConfig.numUsers during (testConfig.duration)),
    gitSshPush.inject(rampConcurrentUsers (1) to testConfig.numUsers during (testConfig.duration)),
  ).protocols(gitProtocol)
}
