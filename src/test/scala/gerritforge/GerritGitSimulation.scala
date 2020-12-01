package gerritforge

import com.github.barbasa.gatling.git.protocol.GitProtocol
import gerritforge.GerritGitScenario._
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import GerritTestConfig.testConfig
import java.net.InetAddress

import gerritforge.ChangesListScenario.listChanges
import gerritforge.ProjectGCScenario.runGc
import io.gatling.http
import io.gatling.http.HttpDsl

import scala.concurrent.duration._

class GerritGitSimulation extends Simulation with HttpDsl {

  def httpProtocol = http
    .baseUrl(testConfig.httpUrl)
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("gatling-test")

  val hostname = InetAddress.getLocalHost.getHostName
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

  val projectGC = scenario("Run Project GC").exec(runGc(testConfig.project, testConfig.accountCookie.get, testConfig.xsrfToken.get))

  setUp(
    gitCloneAndPush.inject(
      rampConcurrentUsers(1) to testConfig.numUsers during (testConfig.duration)
    ).protocols(gitProtocol),
    projectGC.inject(constantConcurrentUsers(1) during (testConfig.duration)).protocols(httpProtocol)
  )
}
