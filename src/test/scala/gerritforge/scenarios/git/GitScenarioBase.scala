package gerritforge.scenarios.git

import com.github.barbasa.gatling.git.GatlingGitConfiguration

import java.util.UUID
import gerritforge.scenarios.ScenarioBase

import java.net.InetAddress

trait GitScenarioBase extends ScenarioBase {

  val url: String
  val protocol = url.split(":").head

  val hostname           = InetAddress.getLocalHost.getHostName
  implicit val gitConfig = GatlingGitConfiguration()

  val feeder =
    Iterator.continually(Map("refSpec" -> UUID.randomUUID()))
}
