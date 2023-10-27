package gerritforge.scenarios.rest.tags

import gerritforge.GerritTestConfig.testConfig
import gerritforge.scenarios.rest.RestScenarioBase
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.net.HttpURLConnection.HTTP_NO_CONTENT

object CreateAndDeleteMultipleTags extends RestScenarioBase {

  private def currentTimestamp: Long = System.currentTimeMillis()
  private val tagsToDeleteAtOnce = 3

  case class TagDetail(ref: String, revision: String)

  override val scn: ScenarioBuilder =
    setupAuthenticatedSession("Create and Delete Multiple Tags")
      .feed(userIdFeeder.circular)
      .exec { session =>
        val userId = session("userId").as[String]
        val tagId = s"scnmultipletags-$userId-$currentTimestamp"
        session.set("tagId", tagId)
      }
      .exec(
        http("create multiple tags")
          .put(s"/projects/${testConfig.encodedProject}/tags/#{tagId}")
          .headers(addApiHeaders(testConfig.xsrfToken))
          .body(StringBody("""{"revision":"HEAD"}"""))
          .asJson
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("list tags")
          .get(
            s"/projects/${testConfig.encodedProject}/tags/?m=scnmultipletags-#{userId}-"
          )
          .headers(addApiHeaders(testConfig.xsrfToken))
          .check(
            bodyString
              .transform(_.drop(XSS_LEN))
              .transform(decode[List[TagDetail]](_))
              .transform {
                case Right(tagDetailList) => tagDetailList
                case Left(decodingError) => throw decodingError
              }
              .saveAs("tagDetails")
          )
      )
      .doIf(session => session("tagDetails").as[List[TagDetail]].size >= tagsToDeleteAtOnce) {
        exec { session =>
          val tagsForUser =
            session("tagDetails")
              .as[List[TagDetail]]
              .map(_.ref)
              .map(_.stripPrefix("refs/tags/"))
              .asJson
          session.set("tagNames", tagsForUser)
        }.exec(
            http("delete multiple tags at once")
              .post(s"/projects/${testConfig.encodedProject}/tags:delete")
              .headers(addApiHeaders(testConfig.xsrfToken))
              .body(StringBody(s"""{"tags": #{tagNames}}"""))
              .asJson
              .check(status.is(HTTP_NO_CONTENT))
        )
        .pause(pauseDuration, pauseStdDev)
      }
}
