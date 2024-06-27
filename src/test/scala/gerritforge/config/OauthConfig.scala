package gerritforge.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object OauthConfig {
  val oauthConfig: OauthConfig = ConfigSource.default.at("oauth").loadOrThrow[OauthConfig]
}

final case class OauthConfig(
    url: String,
    localUrl: String,
    gerritRedirectUrl: String
)
