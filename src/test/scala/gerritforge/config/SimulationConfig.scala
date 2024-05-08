package gerritforge.config

import gerritforge.scenarios.git.backend.Gerrit
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import scala.concurrent.duration.FiniteDuration
import gerritforge.EncodeUtils.encode

object SimulationConfig {
  val simulationConfig = ConfigSource.default.at("simulation").loadOrThrow[SimulationConfig]
}

final case class SimulationConfig(
    httpUrl: Option[String],
    sshUrl: Option[String],
    project: String,
    numUsers: Int,
    duration: FiniteDuration
) {
  val encodedProject = encode(project)
  val gitBackend     = new Gerrit
}
