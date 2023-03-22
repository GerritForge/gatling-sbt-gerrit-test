package gerritforge

import java.net.{HttpURLConnection, URLEncoder}

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import io.circe.parser._
import io.circe.generic.auto._
import HttpURLConnection._
import java.nio.charset.StandardCharsets

import scala.util.Random

case class ChangeOwner(_account_id: Int, name: String)

case class ChangeDetail(
    _number: Int,
    project: String,
    change_id: String,
    owner: ChangeOwner,
    current_revision: String
) {
  lazy val url = s"/c/${project}/+/${_number}/"
}

object ChangesListScenario {

  val XSS_LEN  = 5
  val encoding = StandardCharsets.UTF_8.toString()

  val restApiHeader = Map(
    "Accept"                    -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "Pragma"                    -> "no-cache",
    "Upgrade-Insecure-Requests" -> "1"
  )

  def postApiHeader(xsrfCookie: Option[String]) = {
    val headers: Map[String, String] = restApiHeader + ("Content-Type" -> "application/json")
    xsrfCookie.fold(headers)(c => headers + ("x-gerrit-auth" -> c))
  }

  val randomNumber = new Random

  def listChanges(
      projectName: String,
      authCookie: Option[String] = None,
      xsrfCookie: Option[String] = None
  ) = {
    val checkStatus =
      status.in(authCookie.fold(Seq(HTTP_FORBIDDEN))(_ => Seq(HTTP_OK, HTTP_NO_CONTENT)))

    val listChanges = http("changes list and get first change")
      .get(s"/q/status:open+project:${projectName}")
      .headers(restApiHeader)
      .resources(
        http("get server version")
          .get("/config/server/version"),
        http("get server info")
          .get("/config/server/info"),
        http("get list of changes")
          .get(
            s"/changes/?O=81&S=0&n=500&q=status%3Aopen+project:${projectName}&o=CURRENT_REVISION"
          )
          .check(
            bodyString
              .transform(_.drop(XSS_LEN))
              .transform(decode[List[ChangeDetail]](_))
              .transform {
                case Right(changeDetailList) => changeDetailList
                case Left(decodingError)     => throw decodingError
              }
              .saveAs("changeDetails")
          )
      )

    val getChangeDetails = http("get change details")
      .get("#{changeUrl}")
      .headers(restApiHeader)
      .resources(
        http("check account capabilities")
          .get("/accounts/self/capabilities")
          .check(checkStatus),
        http("fetch comments")
          .get("/changes/#{id}/comments?enable-context=true&context-padding=3"),
        http("fetch ported comments")
          .get("/changes/#{id}/revisions/current/ported_comments/"),
        http("fetch robot-comments")
          .get("/changes/#{id}/robotcomments"),
        http("get change details")
          .get("/changes/#{id}/detail?o=LABELS&o=CURRENT_ACTIONS&o=ALL_REVISIONS&o=SUBMITTABLE"),
        http("get draft comments")
          .get("/changes/#{id}/drafts")
          .check(checkStatus),
        http("get ported drafts comments")
          .get("/changes/#{id}/revisions/current/ported_drafts/"),
        http("get download commands")
          .get("/changes/#{id}/edit/?download-commands=true")
          .check(checkStatus),
        http("get project config")
          .get("/projects/#{project}/config"),
        http("get available actions")
          .get("/changes/#{id}/revisions/#{revision}/actions"),
        http("get list of reviewed files")
          .get("/changes/#{id}/revisions/#{revision}/files?reviewed")
          .check(checkStatus),
        http("get files")
          .get("/changes/#{id}/revisions/1/files"),
        http("check if change is mergeable")
          .get("/changes/#{id}/revisions/current/mergeable"),
        http("get related changes")
          .get("/changes/#{id}/revisions/#{revision}/related"),
        http("get cherry picks")
          .get(
            "/changes/?O=a&q=project%3A#{project}%20change%3A#{changeId}%20-change%3A#{changeNum}%20-is%3Aabandoned"
          ),
        http("get conflicting changes")
          .get("/changes/?O=a&q=status%3Aopen%20conflicts%3A#{changeNum}"),
        http("check for other changes submittable together")
          .get("/changes/#{id}/submitted_together?o=NON_VISIBLE_CHANGES")
      )

    val postComments = {
      http("Post comments with score")
        .post("/changes/#{project}~#{changeNum}/revisions/#{revision}/review")
        .headers(postApiHeader(xsrfCookie))
        .body(
          StringBody(
            """{"drafts":"PUBLISH_ALL_REVISIONS","labels":{"Code-Review":#{reviewScore}},"comments":{"/PATCHSET_LEVEL":[{"message":"#{reviewMessage}","unresolved":false}]},"reviewers":[],"ignore_automatic_attention_set_rules":true,"add_to_attention_set":[],"remove_from_attention_set":[{"user":#{ownerId},"reason":"#{ownerName} replied on the change"}]}"""
          )
        )
        .asJson
    }

    val httpHead = http("head").head("/")

    authCookie
      .fold(exec(flushSessionCookies)) { auth =>
        exec(addCookie(Cookie("GerritAccount", auth)))
          .exec(http("home page").get("/"))
      }
      .exec(listChanges)
      .doIf(session => !session("changeDetails").as[List[ChangeDetail]].isEmpty) {
        exec { session =>
          val changes = session("changeDetails").as[List[ChangeDetail]]
          val change  = changes(randomNumber.nextInt(changes.size))
          session
            .set("changeUrl", change.url)
            .set("id", s"${encode(change.project)}~${change._number}")
            .set("changeNum", change._number)
            .set("changeId", change.change_id)
            .set("project", encode(change.project))
            .set("ownerId", change.owner._account_id)
            .set("ownerName", encode(change.owner.name))
            .set("revision", encode(change.current_revision))
        }.pause(2 seconds)
          .exec(getChangeDetails)
          .exec(authCookie.fold(httpHead)(_ => postComments))
      }
  }

  def encode(value: String): String = {
    URLEncoder.encode(value, encoding)
  }
}