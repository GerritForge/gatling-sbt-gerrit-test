package gerritforge.scenarios.oauth

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import org.jsoup._

object SuccessfulLogin extends OauthScenarioBase {

  override def scnTitle: String = "A user with valid credentials logs into Gerrit"

  override def scnActions: ChainBuilder =
    exec(browseToGerritLogin)
      .exec(setOauthRedirect())
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
            header("Location").saveAs("final_url")
          )
      )
      .pause(pauseDuration, pauseStdDev)

}
