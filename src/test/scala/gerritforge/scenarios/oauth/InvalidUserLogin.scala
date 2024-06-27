package gerritforge.scenarios.oauth

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import org.jsoup._

object InvalidUserLogin extends OauthScenarioBase {

  override def scnTitle: String = "An invalid user cannot log into Gerrit"

  override def scnActions: ChainBuilder =
    exec(browseToGerritLogin)
      .exec(setOauthRedirect())
      .exec(browseToOauthLogin)
      .exec { session =>
        session.set("oauthCookies", session.attributes.getOrElse("cookies", "").asInstanceOf[List[String]].head)
      }
      .exec { session =>
        session.set("action", Jsoup.parse(session.attributes.getOrElse("htmlform", "").asInstanceOf[String]).select("form").first().attributes().get("action"))
      }
      .exec(
        http(scnTitle)
          .post("#{action}")
          .headers(Map("Content-Type" -> "application/x-www-form-urlencoded",
            "Cookie" -> "#{oauthCookies}"))
          .body(StringBody(getLoginCredentials(isValidUser = false)))
          .check(
            status.is(200),
          )
      )
      .pause(pauseDuration, pauseStdDev)
}
