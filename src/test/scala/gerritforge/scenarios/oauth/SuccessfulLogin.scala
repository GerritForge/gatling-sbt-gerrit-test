package gerritforge.scenarios.oauth

import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import org.jsoup._

object SuccessfulLogin extends OauthScenarioBase {

  override def scnTitle: String = "A user with valid credentials logs into Gerrit"

  override def scnActions: ChainBuilder =
    exec(browseToGerritLogin)
      .exec(browseToOauthLogin)
      .exec { session =>
        session.set(
          "oauthCookies",
          session.attributes.getOrElse("cookies", "").asInstanceOf[List[String]].head
        )
      }
      .exec { session =>
        session.set(
          "action",
          Jsoup
            .parse(session.attributes.getOrElse("htmlform", "").asInstanceOf[String])
            .select("form")
            .first()
            .attributes()
            .get("action")
        )
      }
      .exec(
        http(scnTitle)
          .post("#{action}")
          .headers(
            Map(
              "Content-Type" -> "application/x-www-form-urlencoded",
              "Cookie"       -> "#{oauthCookies}"
            )
          )
          .body(StringBody(getLoginCredentials()))
          .check(
            status.is(302),
            headerRegex("Location", s"^((.*)/oauth).*"),
            header("Location").saveAs("redirectToGerritUrl")
          )
      )
      .exec(
        http("A valid user is redirected to Gerrit and is logged in")
          .get("#{redirectToGerritUrl}")
          .check(
            status.is(302),
            headerRegex("Set-Cookie", s"$GERRIT_ACCOUNT_COOKIE_NAME=([^;]*)").exists
          )
      )
      .pause(pauseDuration, pauseStdDev)
}
