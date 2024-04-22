package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GatlingGitConfiguration
//import gerritforge.GerritTestConfig.testConfig

//import java.util.UUID
import gerritforge.scenarios.ScenarioBase

import java.net.InetAddress
import scala.util.Random

trait GitScenarioBase extends ScenarioBase {

  val url: String
  val protocol = url.split(":").head

  val hostname           = InetAddress.getLocalHost.getHostName
  implicit val gitConfig = GatlingGitConfiguration()

  val refSpecFeeder =
    Iterator.continually(
      Map("refSpec" -> s"branch-$hostname-$scenarioName-${Random.nextInt(999999)}")
    )
}
