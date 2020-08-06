package gerritforge

import java.net.URL

import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.FiniteDuration

object GerritTestConfig {
  val testConfig = pureconfig.loadConfig[GerritTestConfig]("gerrit") match {
    case Right(config) => config
    case Left(error)   => throw new Exception(error.toList.mkString(","))
  }
}

case class GerritTestConfig(
    accountCookie: Option[String],
    httpUrl: String,
    sshUrl: String,
    project: String,
    userAgent: String,
    numUsers: Int,
    duration: FiniteDuration
) {
  lazy val domain = new URL(httpUrl).getHost.split('.').drop(1).mkString(".")
  lazy val cookie = accountCookie.map(cookie => Cookie("GerritAccount", cookie).withDomain(domain))
}
