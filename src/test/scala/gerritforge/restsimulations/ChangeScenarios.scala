package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import java.net.HttpURLConnection.{HTTP_NO_CONTENT, HTTP_OK}

object ChangeScenarios extends ChangeScenarioBase {

  val abandonAndRestoreChangeScn: ScenarioBuilder =
    setupAuthenticatedSession("Abandon and then Restore Change")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        authenticatedChangesPostRequest(
          "abandon change",
          "/abandon"
        )
      )
      .exec(
        authenticatedChangesPostRequest(
          "restore change",
          "/restore"
        )
      )

  val submitChangeScn: ScenarioBuilder =
    setupAuthenticatedSession("Submit Change")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        authenticatedChangesPostRequest(
          "Approve Change",
          "/revisions/1/review",
          """{"labels":{"Code-Review":2}}"""
        )
      )
      .exec(
        authenticatedChangesPostRequest(
          "Submit Change",
          "/revisions/1/submit"
        )
      )

  val makeChangeWipScn: ScenarioBuilder =
    setupAuthenticatedSession("Make change WIP")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        authenticatedChangesPostRequest(
          "Mark Change as WIP",
          "/wip"
        )
      )
      .exec(
        authenticatedChangesPostRequest(
          "Mark Change as Ready",
          "/ready"
        )
      )

  val addAndThenRemoveReviewerScn =
    setupAuthenticatedSession("Add and Remove Reviewer")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        authenticatedChangesPostRequest(
          "Add Reviewer",
          "/reviewers",
          s"""{"reviewer":${testConfig.reviewerAccountId}}"""
        )
      )
      .exec(
        authenticatedChangesPostRequest(
          "Remove Reviewer",
          s"/reviewers/${testConfig.reviewerAccountId}/delete",
          """{"notify": "NONE"}"""
        )
      )

  val deleteVoteScn =
    setupAuthenticatedSession("Delete Vote")
      .exec(createChange.check(regex("_number\":(\\d+),").saveAs("changeNumber")))
      .exec(
        authenticatedChangesPostRequest(
          "Vote On Change",
          "/revisions/1/review",
          """{"labels":{"Code-Review":-1}}""".stripMargin
        )
      )
      .exec(
        authenticatedChangesPostRequest(
          "Remove Vote for Label",
          "/reviewers/self/votes/Code-Review/delete",
          """{"notify": "NONE"}"""
        )
      )

  val changePrivateStateScn = setupAuthenticatedSession("Change Private State")
    .exec(createChange.check(regex("_number\":(\\d+),").saveAs("changeNumber")))
    .exec(
      authenticatedChangesPostRequest(
        "Mark Private",
        "/private",
        s"""{"message":"Marking change #{changeNumber} as private for testing purposes"}"""
      )
    )
    .exec(
      authenticatedChangesPostRequest(
        "UnMark Private",
        "/private.delete"
      )
    )

  val postCommentScn =
    setupAuthenticatedSession("Post Comment")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        authenticatedChangesPostRequest(
          "Post Comment",
          "/revisions/1/review",
          """{"drafts":"PUBLISH_ALL_REVISIONS","labels":{"Code-Review":0},
            |"comments":{"/PATCHSET_LEVEL":[{"message":"some message","unresolved":false}]},
            |"reviewers":[],"ignore_automatic_attention_set_rules":true,"add_to_attention_set":[]}""".stripMargin
        )
      )

  val addThenRemoveHashtags =
    setupAuthenticatedSession("Add then Remove Hashtags")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        authenticatedChangesPostRequest(
          "Add Hashtag",
          "/hashtags",
          """{"add":["test", "test1", "test2"]}"""
        )
      )
      .exec(
        authenticatedChangesPostRequest(
          "Remove Hastag",
          "/hashtags",
          """{"remove":["test"]}"""
        )
      )

  val addThenRemoveTopics =
    setupAuthenticatedSession("Add then Remove Topics")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        http("Add Topic")
          .put(s"/changes/${testConfig.encodedProject}~#{changeNumber}/topic")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(
            StringBody("""{"topic":"testTopic"}""")
          )
      )
      .exec(
        http("Remove Topic")
          .delete(s"/changes/${testConfig.encodedProject}~#{changeNumber}/topic")
          .headers(postApiHeader(testConfig.xsrfToken, contentType = None))
      )

  val listThenGetDetailsScn =
    setupAuthenticatedSession("List and Get Change Details")
      .exec(listChanges)
      .exec(pickRandomChange)
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
                s"/changes/?O=a&q=project%3A${testConfig.encodedProject}%20change%3A#{changeId}%20-change%3A#{changeNum}%20-is%3Aabandoned"
              ),
            http("get conflicting changes")
              .get("/changes/?O=a&q=status%3Aopen%20conflicts%3A#{changeNum}"),
            http("check for other changes submittable together")
              .get("/changes/#{id}/submitted_together?o=NON_VISIBLE_CHANGES")
          )
      )

  val listChangesScn = scenario("Anonymous User Listing Changes").exec(listThenGetDetailsScn)

  override val scns: List[ScenarioBuilder] =
    List(
      abandonAndRestoreChangeScn,
      submitChangeScn,
      makeChangeWipScn,
      addAndThenRemoveReviewerScn,
      deleteVoteScn,
      changePrivateStateScn,
      postCommentScn,
      addThenRemoveHashtags,
      addThenRemoveTopics
    )
}
