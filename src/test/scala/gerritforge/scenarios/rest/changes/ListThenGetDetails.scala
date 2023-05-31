package gerritforge.scenarios.rest.changes

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import java.net.HttpURLConnection.{HTTP_NO_CONTENT, HTTP_OK}

object ListThenGetDetails extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
  setupCookies("List and Get Change Details")
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("get change details")
          .get("#{changeUrl}")
          .headers(restApiHeader)
          .resources(
            http("check account capabilities")
              .get("/accounts/self/capabilities")
              .check(status.in(Seq(HTTP_OK, HTTP_NO_CONTENT))),
            http("fetch comments")
              .get("/changes/#{id}/comments?enable-context=true&context-padding=3"),
            http("fetch ported comments")
              .get("/changes/#{id}/revisions/current/ported_comments/"),
            http("fetch robot-comments")
              .get("/changes/#{id}/robotcomments"),
            http("get change details")
              .get(
                "/changes/#{id}/detail?o=LABELS&o=CURRENT_ACTIONS&o=ALL_REVISIONS&o=SUBMITTABLE"
              ),
            http("get draft comments")
              .get("/changes/#{id}/drafts")
              .check(status.in(Seq(HTTP_OK, HTTP_NO_CONTENT))),
            http("get ported drafts comments")
              .get("/changes/#{id}/revisions/current/ported_drafts/"),
            http("get download commands")
              .get("/changes/#{id}/edit/?download-commands=true")
              .check(status.in(Seq(HTTP_OK, HTTP_NO_CONTENT))),
            http("get project config")
              .get(s"/projects/${testConfig.encodedProject}/config"),
            http("get available actions")
              .get("/changes/#{id}/revisions/#{revision}/actions"),
            http("get list of reviewed files")
              .get("/changes/#{id}/revisions/#{revision}/files?reviewed")
              .check(status.in(Seq(HTTP_OK, HTTP_NO_CONTENT))),
            http("get files")
              .get("/changes/#{id}/revisions/1/files"),
            http("check if change is mergeable")
              .get("/changes/#{id}/revisions/current/mergeable"),
            http("get related changes")
              .get("/changes/#{id}/revisions/#{revision}/related"),
            http("get cherry picks")
              .get(
                s"/changes/?O=a&q=project%3A${testConfig.encodedProject}%20change%3A#{changeId}%20-change%3A#{changeNumber}%20-is%3Aabandoned"
              ),
            http("get conflicting changes")
              .get("/changes/?O=a&q=status%3Aopen%20conflicts%3A#{changeNumber}"),
            http("check for other changes submittable together")
              .get("/changes/#{id}/submitted_together?o=NON_VISIBLE_CHANGES")
          )
      )
}
