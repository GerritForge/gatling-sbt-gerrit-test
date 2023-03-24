package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.GatlingRestUtils._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import java.net.HttpURLConnection.{HTTP_NO_CONTENT, HTTP_OK}

object ChangeScenarios extends ScenarioBase {

  val abandonAndRestoreChangeScn: ScenarioBuilder =
    setupAuthenticatedSession("Abandon and then Restore Change")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        http("abandon change")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/abandon")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )
      .exec(
        http("restore change")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/restore")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )

  val submitChangeScn: ScenarioBuilder = {
    setupAuthenticatedSession("Submit Change")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("newChangeNumber"))
      )
      .exec(
        http("Approve Change")
          .post(s"/changes/${testConfig.project}~#{newChangeNumber}/revisions/1/review")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{"labels":{"Code-Review":2}}"""))
          .asJson
      )
      .exec(
        http("Submit Change")
          .post(s"/changes/${testConfig.project}~#{newChangeNumber}/revisions/1/submit")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("{}"))
      )
  }

  val makeChangeWipScn: ScenarioBuilder =
    setupAuthenticatedSession("Make change WIP")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("newChangeNumber"))
      )
      .exec(
        http("Mark Change as WIP")
          .post(s"/changes/${testConfig.project}~#{newChangeNumber}/wip")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{}"""))
      )
      .exec(
        http("Make Change as Ready")
          .post(s"/changes/${testConfig.project}~#{newChangeNumber}/ready")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{}"""))
      )

  val addAndThenRemoveReviewerScn =
    setupAuthenticatedSession("Add and Remove Reviewer")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeToReview"))
      )
      .exec(
        http("Add Reviewer")
          .post(
            s"/changes/${testConfig.project}~#{changeToReview}/reviewers"
          )
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(
            StringBody(
              s"""{"reviewer":${testConfig.reviewerAccountId}}"""
            )
          )
      )
      .exec(
        http("Remove Reviewer")
          .post(
            s"/changes/${testConfig.project}~#{changeToReview}/reviewers/${testConfig.reviewerAccountId}/delete"
          )
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{"notify": "NONE"}"""))
      )

  val deleteVoteScn = setupAuthenticatedSession("Delete Vote")
    .exec(createChange.check(regex("_number\":(\\d+),").saveAs("changeToVote")))
    .exec(
      http("Vote On Change")
        .post(
          s"/changes/${testConfig.project}~#{changeToVote}/revisions/1/review"
        )
        .headers(postApiHeader(testConfig.xsrfToken))
        .body(
          StringBody(
            """{"labels":{"Code-Review":-1}}""".stripMargin
          )
        )
    )
    .exec(
      http("Remove Vote for Label")
        .post(
          s"/changes/${testConfig.project}~#{changeToVote}/reviewers/self/votes/Code-Review/delete"
        )
        .headers(postApiHeader(testConfig.xsrfToken))
        .body(StringBody("""{"notify": "NONE"}"""))
    )

  val changePrivateStateScn = setupAuthenticatedSession("Change Private State")
    .exec(createChange.check(regex("_number\":(\\d+),").saveAs("changeToMarkPrivate")))
    .exec(
      http("Mark Private")
        .post(s"/changes/${testConfig.project}~#{changeToMarkPrivate}/private")
        .headers(postApiHeader(testConfig.xsrfToken))
        .body(
          StringBody(
            s"""{"message":"Marking change #{changeToMarkPrivate} as private for testing purposes"}"""
          )
        )
    )
    .exec(
      http("UnMark Private")
        .post(s"/changes/${testConfig.project}~#{changeToMarkPrivate}/private.delete")
        .headers(postApiHeader(testConfig.xsrfToken))
        .body(StringBody("{}"))
    )

  val postCommentScn = setupAuthenticatedSession("Post Comment")
    .exec(
      createChange
        .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
    )
    .exec(
      http("Post Comment")
        .post(
          s"/changes/${testConfig.project}~#{changeNumber}/revisions/1/review"
        )
        .headers(postApiHeader(testConfig.xsrfToken))
        .body(
          StringBody(
            """{"drafts":"PUBLISH_ALL_REVISIONS","labels":{"Code-Review":0},
              |"comments":{"/PATCHSET_LEVEL":[{"message":"some message","unresolved":false}]},
              |"reviewers":[],"ignore_automatic_attention_set_rules":true,"add_to_attention_set":[]}""".stripMargin
          )
        )
    )

  val addThenRemoveHashtags = {
    setupAuthenticatedSession("Add then Remove Hashtags")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        http("Add Hastag")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/hashtags")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(
            StringBody("""{"add":["test", "test1", "test2"]}""")
          )
      )
      .exec(
        http("Remove Hastag")
          .post(s"/changes/${testConfig.project}~#{changeNumber}/hashtags")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(
            StringBody("""{"remove":["test"]}""")
          )
      )
  }

  val addThenRemoveTopics = {
    setupAuthenticatedSession("Add then Remove Topics")
      .exec(
        createChange
          .check(regex("_number\":(\\d+),").saveAs("changeNumber"))
      )
      .exec(
        http("Add Topic")
          .put(s"/changes/${testConfig.project}~#{changeNumber}/topic")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(
            StringBody("""{"topic":"testTopic"}""")
          )
      )
      .exec(
        http("Remove Topic")
          .delete(s"/changes/${testConfig.project}~#{changeNumber}/topic")
          .headers(postApiHeader(testConfig.xsrfToken, contentType = None))
      )
  }

  val listThenGetDetailsScn = {
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
              .get("/projects/#{project}/config"),
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
                "/changes/?O=a&q=project%3A#{project}%20change%3A#{changeId}%20-change%3A#{changeNum}%20-is%3Aabandoned"
              ),
            http("get conflicting changes")
              .get("/changes/?O=a&q=status%3Aopen%20conflicts%3A#{changeNum}"),
            http("check for other changes submittable together")
              .get("/changes/#{id}/submitted_together?o=NON_VISIBLE_CHANGES")
          )
      )
  }

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
      addThenRemoveTopics,
      listThenGetDetailsScn
    )
}
