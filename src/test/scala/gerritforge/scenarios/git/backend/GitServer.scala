package gerritforge.scenarios.git.backend

import com.github.barbasa.gatling.git.GatlingGitConfiguration
import io.gatling.core.pause.PauseType
import io.gatling.core.structure.ChainBuilder

import scala.concurrent.duration.FiniteDuration

trait GitServer {

  def createChange(
      origin: String,
      refSpec: String,
      userId: String,
      protocol: String,
      pushOptions: Option[String] = Option.empty,
      sleep: Option[(FiniteDuration, PauseType)] = Option.empty
  )(
      implicit conf: GatlingGitConfiguration
  ): ChainBuilder

  def baseUrl(url: String): String
  val httpUrlSuffix: String                = ""
  def gitUrl(url: String, project: String) = s"${baseUrl(url)}/$project$httpUrlSuffix"

  val refSpecFeeder: Iterator[Map[String, String]]
}
