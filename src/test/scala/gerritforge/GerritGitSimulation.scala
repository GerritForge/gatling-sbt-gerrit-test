package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritGitScenario._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import GerritTestConfig.testConfig
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}

import java.net.InetAddress
import scala.concurrent.duration._

class GerritGitSimulation extends Simulation {

  val hostname    = InetAddress.getLocalHost.getHostName
  val gitProtocol = GitProtocol()
  val feeder = (1 to testConfig.numUsers) map { idx =>
    Map("refSpec" -> s"branch-$hostname-$idx", "force" -> true)
  }

  val gitSshScenario  = testConfig.sshUrl.map(GerritGitScenario)
  val gitHttpScenario = testConfig.httpUrl.map(_ + "/a").map(GerritGitScenario)

  val gitCloneAndPush: ScenarioBuilder = scenario("Git clone and push to Gerrit")
    .feed(feeder.circular)
    .doIf(gitSshScenario.isDefined) {
      exec(gitSshScenario.get.pushCommand)
        .exec(gitSshScenario.get.cloneCommand)
        .exec(gitSshScenario.get.createChangeCommand)
    }
    .doIf(gitHttpScenario.isDefined) {
      exec(gitHttpScenario.get.pushCommand)
        .exec(gitHttpScenario.get.cloneCommand)
        .exec(gitHttpScenario.get.createChangeCommand)
    }

  require(
    testConfig.httpUrl.orElse(testConfig.sshUrl).isDefined,
    "Either GERRIT_HTTP_URL or GERRIT_SSH_URL must be defined"
  )

  setUp(
    gitCloneAndPush.inject(
      rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration)
    )
  ).protocols(gitProtocol)
}
