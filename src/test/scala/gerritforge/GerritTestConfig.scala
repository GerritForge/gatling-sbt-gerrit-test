package gerritforge

import java.net.URL

import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object GerritTestConfig {
  val testConfig = pureconfig.loadConfig[GerritTestConfig]("gerrit") match {
    case Right(config) => config
    case Left(error) => throw new Exception(error.toList.mkString(","))
  }
}

case class GerritTestConfig(accountCookie: String, url: String, userAgent: String)
{
  lazy val domain = new URL(url).getHost.split('.').drop(1).mkString(".")

  lazy val cookie = Cookie("GerritAccount", accountCookie).withDomain(domain)
}
