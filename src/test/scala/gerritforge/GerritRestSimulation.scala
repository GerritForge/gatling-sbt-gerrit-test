package gerritforge

import gerritforge.GerritRestSimulation.{allRestScenarios, authenticatedScenarios}
import gerritforge.GerritTestConfig._
import gerritforge.scenarios.ScenarioBase
import gerritforge.scenarios.rest.changes._
import gerritforge.scenarios.rest.tags.{CreateAndDeleteMultipleTags, CreateAndDeleteTag}
import io.gatling.core.Predef._
import io.gatling.http.Predef.http
import io.gatling.http.protocol.HttpProtocol

import scala.concurrent.duration.FiniteDuration

class GerritRestSimulation extends Simulation {

  val scenarios =
    if (testConfig.restRunAnonymousUser)
      allRestScenarios
    else authenticatedScenarios

  val httpProtocol: HttpProtocol = testConfig.httpUrl
    .map(
      url =>
        http
          .baseUrl(url)
          .inferHtmlResources(
            AllowList(),
            DenyList(""".*\.js""", """.*\.css""", """.*\.ico""", """.*\.woff2""", """.*\.png""")
          )
          .acceptHeader("*/*")
          .acceptEncodingHeader("gzip, deflate")
          .acceptLanguageHeader("en-GB,en;q=0.5")
          .userAgentHeader("gatling-test")
    )
    .getOrElse(
      throw new IllegalArgumentException(
        "GERRIT_HTTP_URL must be defined to run REST-API simulation"
      )
    )

  val pauseStdDevSecs = 5
  setUp(
    scenarios.toList.map(
      _.scn
        .inject(rampConcurrentUsers(1) to testConfig.numUsers during testConfig.duration)
        .pauses(normalPausesWithStdDevDuration(FiniteDuration(pauseStdDevSecs, "seconds")))
    )
  ).protocols(httpProtocol).maxDuration(testConfig.duration)
}

object GerritRestSimulation {
  val authenticatedScenarios = List(
    AbandonThenRestoreChange,
    AddThenRemoveHashtags,
    AddThenRemoveReviewer,
    AddThenRemoveTopics,
    ChangePrivateState,
    DeleteVote,
    MarkChangeWIP,
    PostComment,
    SubmitChange,
    CreateAndDeleteMultipleTags,
    CreateAndDeleteTag,
    AddPatchset
  )

  val anonymousScenarios = List(
    ListThenGetDetails
  )

  val allRestScenarios: Seq[ScenarioBase] = authenticatedScenarios ++ anonymousScenarios
}
