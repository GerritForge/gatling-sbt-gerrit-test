package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritGitScenario._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation

import scala.concurrent.duration._

class GerritGitSimulation extends Simulation {

  val gitProtocol = GitProtocol()
  val numUsers    = 2
  val feeder = (1 to numUsers) map { idx =>
    Map("refSpec" -> s"branch-$idx")
  }

  val gitClone = scenario("Git clone from Gerrit").exec(cloneCommand)
  val gitPush = scenario("Git push to Gerrit")
    .feed(feeder.circular)
    .exec(cloneCommand)
    .exec(pushCommand)

  setUp(
    gitPush.inject(constantConcurrentUsers(numUsers) during (10 seconds))
  ).protocols(gitProtocol)
}
