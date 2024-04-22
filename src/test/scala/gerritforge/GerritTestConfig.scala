package gerritforge

import pureconfig._
import pureconfig.generic.auto._
import EncodeUtils.encode
import gerritforge.GerritHttpConfig.httpConfig
import gerritforge.scenarios.git.backend.{BitBucket, Gerrit}

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
    restRunAnonymousUser: Boolean,
    reviewerAccount: Int,
    backend: String
) {
  val encodedProject = encode(project)
  val gitBackend = backend.toLowerCase() match {
    case "bitbucket" => new BitBucket(httpConfig.username, httpConfig.password, encodedProject)
    case "gerrit"    => Gerrit
  }
}
