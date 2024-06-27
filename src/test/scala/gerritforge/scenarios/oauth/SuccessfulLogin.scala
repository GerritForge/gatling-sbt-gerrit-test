package gerritforge.scenarios.oauth

import gerritforge.scenarios.oauth.OauthScenarioBase._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import org.jsoup._


object SuccessfulLogin extends OauthScenarioBase {

  override val scn: ScenarioBuilder =
    scenario("User logs into gerrit using oauth")
      .exec(
        browseToGerritLogin
      )
      .exec { session =>
        val firstred : String = session.attributes.getOrElse("redirect_url", "").asInstanceOf[String]
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
        http("A user with valid credentials logs into Gerrit")
          .post("#{action}")
          .headers(Map("Content-Type" -> "application/x-www-form-urlencoded",
            "Cookie" -> "#{keycloackCookies}"))
          .body(StringBody(getLoginCredentials()))
          .check(
            status.is(302),
            headerRegex("Location", s"^($GERRIT_URL/oauth).*"),
            header("Location").saveAs("final_url")
          )
      )
      .pause(pauseDuration, pauseStdDev)
}
