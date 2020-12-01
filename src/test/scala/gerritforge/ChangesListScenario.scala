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

import scala.util.Random

case class ChangeDetail(_number: Int, project: String, change_id: String) {
  lazy val url = s"/c/${project}/+/${_number}/"
}

object ChangesListScenario extends GerritRestApi {

  val randomNumber = new Random

  def listChanges(authCookie: Option[String] = None, xsrfCookie: Option[String] = None) = {
    val checkStatus = status.in(authCookie.fold(Seq(HTTP_FORBIDDEN))(_ => Seq(HTTP_OK, HTTP_NO_CONTENT)))

    val listChanges = http("changes list and get first change")
      .get("/q/status:open")
      .headers(restApiHeader)
      .resources(
        http("get server version")
          .get("/config/server/version"),
        http("get server info")
          .get("/config/server/info"),
        http("get list of changes")
          .get("/changes/?O=81&S=0&n=500&q=status%3Aopen")
          .check(
            bodyString.transform(_.drop(XSS_LEN))
              .transform(decode[List[ChangeDetail]](_))
              .transform(_.right.get)
              .transform(changes => changes(randomNumber.nextInt(changes.size)))
              .saveAs("changeDetail")))

    val getChangeDetails = http("get change details")
      .get("${changeUrl}")
      .headers(restApiHeader)
      .resources(
        http("check account capabilities")
          .get("/accounts/self/capabilities")
          .check(checkStatus),
        http("fetch comments")
          .get("/changes/${id}/comments"),
        http("fetch robot-comments")
          .get("/changes/${id}/robotcomments"),
        http("get change details")
          .get("/changes/${id}/detail?O=916314"),
        http("get draft comments")
          .get("/changes/${id}/drafts")
          .check(checkStatus),
        http("get download commands")
          .get("/changes/${id}/edit/?download-commands=true")
          .check(checkStatus),
        http("get project config")
          .get("/projects/${project}/config"),
        http("get available actions")
          .get("/changes/${id}/revisions/1/actions"),
        http("get list of reviewed files")
          .get("/changes/${id}/revisions/1/files?reviewed")
          .check(checkStatus),
        http("check if change is mergeable")
          .get("/changes/${id}/revisions/current/mergeable"),
        http("get related changes")
          .get("/changes/${id}/revisions/1/related"),
        http("get cherry picks")
          .get("/changes/?O=a&q=project%3A${project}%20change%3A${changeId}%20-change%3A${changeNum}%20-is%3Aabandoned"),
        http("get conflicting changes")
          .get("/changes/?O=a&q=status%3Aopen%20conflicts%3A${changeNum}"),
        http("check for other changes submittable together")
          .get("/changes/${id}/submitted_together?o=NON_VISIBLE_CHANGES"))

    val postComments = {
      http("Post comments with score")
        .post("/changes/${project}~${changeNum}/revisions/1/review")
        .headers(postApiHeader(xsrfCookie))
        .body(StringBody("""{"drafts":"PUBLISH_ALL_REVISIONS","labels":{"Code-Review":${reviewScore}},"message":"${reviewMessage}","reviewers":[]}"""))
        .asJson
    }

    val httpHead = http("head").head("/")

    authCookie
      .fold(exec(flushSessionCookies)) { auth =>
        exec(addCookie(Cookie("GerritAccount", auth)))
          .exec(http("home page").get("/"))
      }
      .exec(listChanges)
      .exec { session =>
        val change = session("changeDetail").as[ChangeDetail]
        session
          .set("changeUrl", change.url)
          .set("id", s"${change.project}~${change._number}")
          .set("changeNum", change._number)
          .set("changeId", change.change_id)
          .set("project", change.project)
      }
      .pause(2 seconds)
      .exec(getChangeDetails)
      .exec(authCookie.fold(httpHead)(_ => postComments))
  }
}
