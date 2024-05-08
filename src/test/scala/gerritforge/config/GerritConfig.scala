package gerritforge.config

import pureconfig._
import pureconfig.generic.auto._

object GerritConfig {
  val gerritConfig = ConfigSource.default.at("gerrit").loadOrThrow[GerritConfig]
}

final case class GerritConfig(
    accountCookie: Option[String],
    xsrfToken: Option[String],
    restRunAnonymousUser: Boolean,
    reviewerAccount: Int
)
