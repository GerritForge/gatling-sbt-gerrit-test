package gerritforge.scenarios.oauth

import gerritforge.EncodeUtils.encode
import gerritforge.config.GerritConfig.gerritConfig
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.ScenarioBase
import gerritforge.scenarios.oauth.OauthScenarioBase._
import gerritforge.scenarios.rest.RestScenarioBase
import gerritforge.scenarios.rest.changes.ChangeScenarioBase.ChangeDetail
import io.circe.generic.auto._
import io.circe.parser._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.Calendar
import scala.util.Random

trait OauthScenarioBase extends ScenarioBase {

  def browseToGerritLogin =
    http("Gerrit /login redirects to oauth provider")
      .get(s"$GERRIT_DOCKER_INTERNAL/login/%2Fq%2Fstatus%3Aopen%2B-is%3Awip")
      .check(
        status.is(302),
        header("Location").saveAs("redirect_url")
      )

  def browseToOauthLogin =
    http("Oauth provider ask for login")
      .get("#{keycloack}")
      .check(
        status.is(200),
        bodyString.saveAs("htmlform"),
        header("set-cookie").findAll.saveAs("cookies")
      )

}

object OauthScenarioBase {
  final val GERRIT_DOCKER_INTERNAL = "http://host.docker.internal:8888"
  final val GERRIT_URL =  "http://localhost:8888"
  final val KEYCLOAK_DOCKER_INTERNAL = "http://host.docker.internal:8080"
  final val KEYCLOAK_URL = "http://keycloak:8080"
  final val VALID_USR = "foo"
  final val VALID_PWD = "secret"

  def getLoginCredentials(isValidUser: Boolean = true, hasValidCredentials: Boolean = true): String = {
    val usr = if(isValidUser) VALID_USR else Random.alphanumeric.take(4).mkString
    val pwd = if(hasValidCredentials) VALID_PWD else Random.alphanumeric.take(4).mkString
    s"username=$usr&password=$pwd&credentialId="
  }
}
