package gerritforge

import java.net.HttpURLConnection

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import HttpURLConnection._

import gerritforge.ChangesListScenario.{Cookie, addCookie, http}
import io.gatling.http.HttpDsl

import scala.util.Random

object ProjectGCScenario extends HttpDsl with GerritRestApi {

  def runGc(project: String, authCookie: String, xsrfCookie: String) =
    exec(addCookie(Cookie("GerritAccount", authCookie)))
      .exec(http("home page").get("/"))
      .exec(http("Run GC")
        .post(s"/projects/$project/gc")
        .headers(postApiHeader(Some(xsrfCookie)))
        .body(StringBody("""{"show_progress": false, "async": true}"""))
        .asJson)
}
