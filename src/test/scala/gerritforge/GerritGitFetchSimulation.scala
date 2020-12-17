package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritGitScenario._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import GerritTestConfig.testConfig
import java.net.InetAddress

import scala.concurrent.duration._

class GerritGitFetchSimulation extends Simulation {

  val hostname = InetAddress.getLocalHost.getHostName
  val gitProtocol = GitProtocol()

  val feeder = csv(s"${testConfig.dataDirectory}/gerrit-git-fetch-simulation.csv")

  val gitHttpScenario = GerritGitScenario(testConfig.httpUrl + "/a")

  val gitCloneAndPushSSH = scenario("Git clone and fetch to Gerrit")
    .feed(feeder.circular)
    .exec(gitHttpScenario.fetchPatchSetCommand)

  setUp(
    gitCloneAndPushSSH.inject(
      rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration)
    )
  ).protocols(gitProtocol)
}
