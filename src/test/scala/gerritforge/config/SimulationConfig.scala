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

final case class SimulationConfig(
    httpUrl: Option[String],
    sshUrl: Option[String],
    repository: String,
    numUsers: Int,
    duration: FiniteDuration,
    backend: String
) {
  val encodedProject = encode(repository)
  val gitBackend = backend.toLowerCase() match {
<<<<<<< HEAD
    case "bitbucket" => BitBucket(httpConfig.username, httpConfig.password, encodedProject)
    case "gerrit"    => Gerrit(encodedProject, numUsers)
    case _           => throw new Exception("Unsupported backend")
=======
    case "bitbucket" =>
      println("creating bitbucket")
      BitBucket(httpConfig.username, httpConfig.password, encodedProject)
    case _           =>
      println("creating gerrit")
      Gerrit(encodedProject, numUsers)
>>>>>>> 59ecf42 (Make BitBucket simulation configurable)
  }
}
