package gerritforge.config

import gerritforge.scenarios.git.backend.{BitBucket, Gerrit}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import scala.concurrent.duration.FiniteDuration
import gerritforge.EncodeUtils.encode
import HttpConfig.httpConfig

object SimulationConfig {
  val simulationConfig = ConfigSource.default.at("simulation").loadOrThrow[SimulationConfig]
}

case class SimulationConfig(
    httpUrl: Option[String],
    sshUrl: Option[String],
    project: String,
    numUsers: Int,
    duration: FiniteDuration,
    backend: String
) {
  val encodedProject = encode(project)
  val gitBackend = backend.toLowerCase() match {
    case "bitbucket" => BitBucket(httpConfig.username, httpConfig.password, encodedProject)
    case _           => Gerrit(encodedProject, numUsers)
  }
}