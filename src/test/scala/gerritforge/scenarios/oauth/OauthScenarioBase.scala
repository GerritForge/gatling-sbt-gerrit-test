package gerritforge.scenarios.oauth

import gerritforge.config.HttpConfig.httpConfig
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.ScenarioBase
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

trait OauthScenarioBase extends ScenarioBase {

  final val GERRIT_ACCOUNT_COOKIE_NAME = "GerritAccount"
  final val INVALID_PWD                = "Invalid"

  def browseToGerritLogin: HttpRequestBuilder = {
    http("Gerrit /login redirects to oauth provider")
      .get(s"${simulationConfig.httpUrl.get}/login/%2Fq%2Fstatus%3Aopen%2B-is%3Awip")
      .check(
        status.is(302),
        header("Location").saveAs("redirectToOauthProviderUrl")
      )
  }

  def browseToOauthLogin: HttpRequestBuilder =
    http("Oauth provider ask for login")
      .get("#{redirectToOauthProviderUrl}")
      .check(
        status.is(200),
        bodyString.saveAs("htmlform"),
        header("set-cookie").findAll.saveAs("cookies")
      )

  def getLoginCredentials(
      hasValidCredentials: Boolean = true
  ): String = {
    val pwd = if (hasValidCredentials) httpConfig.password else INVALID_PWD
    s"username=${httpConfig.username}&password=$pwd&credentialId="
  }
}
