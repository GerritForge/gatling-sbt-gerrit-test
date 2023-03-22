package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.GatlingRestUtils._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

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

  override val scns: List[ScenarioBuilder] =
    List(
      abandonAndRestoreChangeScn,
      submitChangeScn,
      makeChangeWipScn,
      addAndThenRemoveReviewerScn,
      deleteVoteScn,
      changePrivateStateScn,
      postCommentScn
    )
}
