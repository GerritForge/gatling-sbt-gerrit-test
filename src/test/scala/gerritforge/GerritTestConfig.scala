package gerritforge

import pureconfig._
import pureconfig.generic.auto._
import EncodeUtils.encode

import scala.concurrent.duration.FiniteDuration

object GerritTestConfig {
  val testConfig = ConfigSource.default.at("gerrit").loadOrThrow[GerritTestConfig]
}

case class GerritTestConfig(
    accountCookie: Option[String],
    xsrfToken: Option[String],
    httpUrl: Option[String],
    sshUrl: Option[String],
    project: String,
    userAgent: String,
    numUsers: Int,
    duration: FiniteDuration,
    reviewerAccount: Int
) {
  val encodedProject = encode(project)
}
