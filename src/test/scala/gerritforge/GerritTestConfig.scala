package gerritforge

import java.net.URL

import pureconfig._
import pureconfig.generic.auto._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.FiniteDuration

object GerritTestConfig {
  val testConfig = ConfigSource.default.at("gerrit").load[GerritTestConfig] match {
    case Right(config) => config
    case Left(error)   => throw new Exception(error.toList.mkString(","))
  }
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
    stickyCookie: Option[String]
)
