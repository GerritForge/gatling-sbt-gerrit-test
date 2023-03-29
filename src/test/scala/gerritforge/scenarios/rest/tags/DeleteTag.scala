package gerritforge.scenarios.rest.tags

import gerritforge.GerritTestConfig.testConfig
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

object DeleteTag extends TagScenarioBase {

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("List and remove a Tag")
      .feed(tagGroupIds)
      .exec(
        http("list tags")
          .get(
            s"/projects/${testConfig.encodedProject}/tags/?n=$tagsToDeleteAtOnce&m=-#{tagGroupId}"
          )
          .headers(postApiHeader(testConfig.xsrfToken))
          .check(
            bodyString
              .transform(_.drop(XSS_LEN))
              .transform(decode[List[TagDetail]](_))
              .transform {
                case Right(tagDetailList) => tagDetailList
                case Left(decodingError)  => throw decodingError
              }
              .saveAs("tagDetails")
          )
      )
      .pause(pauseDuration, pauseStdDev)
      .doIf(session => session("tagDetails").as[List[TagDetail]].nonEmpty) {
        exec { session =>
          val tagNames =
            session("tagDetails")
              .as[List[TagDetail]]
              .map(_.ref)
              .map(_.drop("refs/tags/".length))
              .asJson
          session.set("tagNames", tagNames)
        }.exec(
            http("delete tag")
              .post(s"/projects/${testConfig.encodedProject}/tags:delete")
              .headers(postApiHeader(testConfig.xsrfToken))
              .body(StringBody("""{"tags": #{tagNames}}"""))
              .asJson
          )
          .pause(pauseDuration, pauseStdDev)
      }
}
