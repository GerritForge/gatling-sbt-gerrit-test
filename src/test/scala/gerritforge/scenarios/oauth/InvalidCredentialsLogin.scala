package gerritforge.scenarios.oauth

import gerritforge.scenarios.ScenarioBase
import gerritforge.scenarios.oauth.OauthScenarioBase.{KEYCLOAK_DOCKER_INTERNAL, KEYCLOAK_URL, getLoginCredentials}
import gerritforge.scenarios.oauth.SuccessfulLogin.{browseToGerritLogin, browseToOauthLogin}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import org.jsoup._


object InvalidCredentialsLogin extends ScenarioBase {

  override val scn: ScenarioBuilder =
    scenario("User with invalid credentials")
      .exec(
        browseToGerritLogin
      )
      .exec { session =>
        val firstred: String = session.attributes.getOrElse("redirect_url", "").asInstanceOf[String]
        session.set("keycloack", firstred.replace(KEYCLOAK_URL, KEYCLOAK_DOCKER_INTERNAL))
      }
      .exec(
        browseToOauthLogin
      )
      .exec { session =>
        session.set("keycloackCookies", session.attributes.getOrElse("cookies", "").asInstanceOf[List[String]].head)
      }
      .exec { session =>
        session.set("action", Jsoup.parse(session.attributes.getOrElse("htmlform", "").asInstanceOf[String]).select("form").first().attributes().get("action"))
      }
      .exec(
        http("A user with non-valid credentials cannot log into Gerrit")
          .post("#{action}")
          .headers(Map("Content-Type" -> "application/x-www-form-urlencoded",
            "Cookie" -> "#{keycloackCookies}"))
          .body(StringBody(getLoginCredentials(hasValidCredentials = false)))
          .check(
            status.is(200),
          )
      )
      .pause(pauseDuration, pauseStdDev)
}
