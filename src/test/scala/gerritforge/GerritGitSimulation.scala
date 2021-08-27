package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritGitScenario._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import GerritTestConfig.testConfig
import io.gatling.core.structure.ChainBuilder

import java.net.InetAddress
import scala.concurrent.duration._

class GerritGitSimulation extends Simulation {

  val hostname    = InetAddress.getLocalHost.getHostName
  val gitProtocol = GitProtocol()
  val feeder = (1 to testConfig.numUsers) map { idx =>
    Map("refSpec" -> s"branch-$hostname-$idx", "force" -> true)
  }

  val gitSshScenario  = GerritGitScenario(testConfig.sshUrl)
  val gitHttpScenario = GerritGitScenario(testConfig.httpUrl.map(_ + "/a"))

  val gitCloneAndPush = scenario("Git clone and push to Gerrit")
    .feed(feeder.circular)
    .exec(
      ChainBuilder(
        gitSshScenario.pushCommand.toList ++
          gitHttpScenario.pushCommand.toList
      )
    )
    .exec(
      ChainBuilder(
        gitSshScenario.cloneCommand.toList ++
          gitHttpScenario.cloneCommand.toList
      )
    )
    .exec(
      ChainBuilder(
        gitSshScenario.createChangeCommand.toList ++
          gitHttpScenario.createChangeCommand.toList
      )
    )

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
