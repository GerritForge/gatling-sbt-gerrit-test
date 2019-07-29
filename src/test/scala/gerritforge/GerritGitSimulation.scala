package gerritforge

import io.gatling.core.scenario.Simulation
import GerritGitScenario._
import com.github.barbasa.gatling.git.protocol.GitProtocol
import io.gatling.core.Predef._
import scala.concurrent.duration._

class GerritGitSimulation  extends Simulation {

  val gitProtocol = GitProtocol()

  val gitClone = scenario("Git clone from Gerrit").exec(cloneCommand)
  val gitPush = scenario("Git push to Gerrit")
    .exec(cloneCommand)
    .exec(pushCommand)

  setUp(
    gitClone.inject(rampConcurrentUsers(1) to 10 during (2 minutes)),
    gitPush.inject(rampConcurrentUsers(1) to 10 during (2 minutes))
  ).protocols(gitProtocol)
}
