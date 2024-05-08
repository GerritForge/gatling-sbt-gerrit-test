package gerritforge.config

import pureconfig._
import pureconfig.generic.auto._

object GerritTestConfig {
  val gerritTestConfig = ConfigSource.default.at("gerrit").loadOrThrow[GerritTestConfig]
}

case class GerritTestConfig(
    accountCookie: Option[String],
    xsrfToken: Option[String],
    userAgent: String,
    restRunAnonymousUser: Boolean,
    reviewerAccount: Int
)
