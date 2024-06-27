package gerritforge.scenarios.oauth

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import org.jsoup._

object InvalidCredentialsLogin extends OauthScenarioBase {

  final val OAUTH_INVALID_CREDENTIALS_MSG = "Invalid username or password"

  override def scnTitle: String = "A user with non-valid credentials cannot log into Gerrit"

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
          .body(StringBody(getLoginCredentials(hasValidCredentials = false)))
          .check(
            status.is(200),
            bodyString.find.transform(_.contains(OAUTH_INVALID_CREDENTIALS_MSG)).is(true)
          )
      )
      .pause(pauseDuration, pauseStdDev)
}
