package gerritforge.config

import pureconfig._
import pureconfig.generic.auto._

object GerritConfig {
  val gerritConfig = ConfigSource.default.at("gerrit").loadOrThrow[GerritConfig]
}

case class GerritConfig(
    accountCookie: Option[String],
    xsrfToken: Option[String],
    userAgent: String,
    restRunAnonymousUser: Boolean,
    reviewerAccount: Int
)