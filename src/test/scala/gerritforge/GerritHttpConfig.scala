package gerritforge

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object GerritHttpConfig {

  val httpConfig = ConfigSource.default.at("http").loadOrThrow[GerritHttpConfig]
}

case class GerritHttpConfig(
    username: String,
    password: String
)
