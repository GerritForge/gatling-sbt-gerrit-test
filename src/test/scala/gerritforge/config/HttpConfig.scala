package gerritforge.config
import pureconfig.generic.auto._

import pureconfig.ConfigSource

object HttpConfig {

  val httpConfig = ConfigSource.default.at("http").loadOrThrow[HttpConfig]
}

final case class HttpConfig(
    username: String,
    password: String
)
