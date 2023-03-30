package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GatlingGitConfiguration
import gerritforge.GerritTestConfig.testConfig

import java.util.UUID
import gerritforge.scenarios.ScenarioBase
import io.gatling.core.Predef.Session

import java.net.InetAddress

trait GitScenarioBase extends ScenarioBase {

  val url: String
  val protocol = url.split(":").head

  val hostname           = InetAddress.getLocalHost.getHostName
  implicit val gitConfig = GatlingGitConfiguration()

  val refSpecFeeder =
    (1 to testConfig.numUsers) map { idx =>
      Map("refSpec" -> s"branch-$hostname-$idx-$protocol-$scenarioName-${UUID.randomUUID()}")
    //RandomUUID is needed to ensure unique-ness across different runs.
    }

  def branchToBeCreated(is: Boolean): Session => Session = { session =>
    val newSession = session.set("branchToBeCreated", is)
    newSession
  }
}
