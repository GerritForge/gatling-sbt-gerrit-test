package gerritforge.scenarios.rest.changes

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

object PostComment extends ChangeScenarioBase {

  override val scn: ScenarioBuilder =
    setupCookies("Post Comment")
      .feed(userIdFeeder.circular)
      .exec(listChangesWithHashtags(List(scenarioName, "#{userId}")))
      .exec(pickRandomChange)
      .pause(pauseDuration, pauseStdDev)
      .exec(
        authenticatedChangesPostRequest(
          "Post Comment",
          "/revisions/1/review",
          """{"drafts":"PUBLISH_ALL_REVISIONS","labels":{"Code-Review":0},
            |"comments":{"/PATCHSET_LEVEL":[{"message":"some message","unresolved":false}]},
            |"reviewers":[],"ignore_automatic_attention_set_rules":true,"add_to_attention_set":[]}""".stripMargin
        )
      )
      .pause(pauseDuration, pauseStdDev)
}
