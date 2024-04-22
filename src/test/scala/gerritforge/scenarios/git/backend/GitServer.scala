package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.GatlingGitConfiguration
import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.structure.ChainBuilder

trait GitServer {

  def createChange(
      origin: String,
      refSpec: String,
      userId: String,
      protocol: String,
      hashtags: Seq[String]
  )(
      implicit conf: GatlingGitConfiguration
  ): ChainBuilder

  def baseUrl(url: String): String
  val httpUrlSuffix: String = ""
  def gitUrl(url: String, project: String) = s"${baseUrl(url)}/$project$httpUrlSuffix"


  val refSpecFeeder: Iterator[Map[String, String]]
}
