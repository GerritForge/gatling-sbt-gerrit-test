package gerritforge

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object HttpConfig {

  val httpConfig = ConfigSource.default.at("http").loadOrThrow[HttpConfig]
}

final case class HttpConfig(
    username: String,
    password: String
)
