package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder

case class PostComment(queryFilter: List[String] = List("PostComment", "#{userId}"))
    extends ChangeScenarioBase {

  override def scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(queryFilter))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseType)
      .exec(
        authenticatedChangesPostRequest(
          "Post Comment",
          "/revisions/1/review",
          """{"drafts":"PUBLISH_ALL_REVISIONS","labels":{"Code-Review":0},
            |"comments":{"/PATCHSET_LEVEL":[{"message":"some message","unresolved":false}]},
            |"reviewers":[],"ignore_automatic_attention_set_rules":true,"add_to_attention_set":[]}""".stripMargin
        )
      )
      .pause(pauseDuration, pauseType)

  override def scnTitle: String = "Post Comment"
}
