package gerritforge.scenarios.oauth

import gerritforge.config.HttpConfig.httpConfig
import gerritforge.config.OauthConfig.oauthConfig
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.ScenarioBase
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.util.Random

trait OauthScenarioBase extends ScenarioBase {

  def browseToGerritLogin: HttpRequestBuilder = {
    http("Gerrit /login redirects to oauth provider")
      .get(s"${simulationConfig.httpUrl.get}/login/%2Fq%2Fstatus%3Aopen%2B-is%3Awip")
      .check(
        status.is(302),
        header("Location").saveAs("redirect_url")
      )
  }

  def browseToOauthLogin: HttpRequestBuilder =
    http("Oauth provider ask for login")
      .get("#{oauthLoginUrl}")
      .check(
        status.is(200),
        bodyString.saveAs("htmlform"),
        header("set-cookie").findAll.saveAs("cookies")
      )

  def getLoginCredentials(isValidUser: Boolean = true, hasValidCredentials: Boolean = true): String = {
    val usr = if(isValidUser) httpConfig.username else Random.alphanumeric.take(4).mkString
    val pwd = if(hasValidCredentials) httpConfig.password else Random.alphanumeric.take(4).mkString
    s"username=$usr&password=$pwd&credentialId="
  }

  def setOauthRedirect(): ChainBuilder = {
    exec { session =>
      val firstRedirect: String = session.attributes.getOrElse("redirect_url", "").asInstanceOf[String]
      session.set("oauthLoginUrl", firstRedirect.replace(oauthConfig.localUrl, oauthConfig.url))
    }
  }
}
