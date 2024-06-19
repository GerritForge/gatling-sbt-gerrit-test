package gerritforge.scenarios.rest.tags

import gerritforge.config.GerritConfig.gerritConfig
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.rest.RestScenarioBase
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import java.net.HttpURLConnection.HTTP_NO_CONTENT

object CreateAndDeleteMultipleTags extends RestScenarioBase {

  private def currentTimestamp: Long = System.currentTimeMillis()
  private val tagsToDeleteAtOnce =
    sys.env.get("NUMBER_OF_TAGS_TO_DELETE_AT_ONCE").map(_.toInt).getOrElse(3)

  case class TagDetail(ref: String, revision: String)

  override val scnActions: ChainBuilder =
    exec(setupAuthenticatedSession)
      .feed(userIdFeeder.circular)
      .exec { session =>
        val userId = session("userId").as[String]
        val tagId  = s"scnmultipletags-$userId-$currentTimestamp"
        session.set("tagId", tagId)
      }
      .exec(
        http("create multiple tags")
          .put(s"/projects/${simulationConfig.encodedProject}/tags/#{tagId}")
          .headers(addApiHeaders(gerritConfig.xsrfToken))
          .body(StringBody("""{"revision":"HEAD"}"""))
          .asJson
      )
      .pause(pauseDuration, pauseStdDev)
      .exec(
        http("list tags")
          .get(
            s"/projects/${simulationConfig.encodedProject}/tags/?m=scnmultipletags-#{userId}-"
          )
          .headers(addApiHeaders(gerritConfig.xsrfToken))
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
              .post(s"/projects/${simulationConfig.encodedProject}/tags:delete")
              .headers(addApiHeaders(gerritConfig.xsrfToken))
              .body(StringBody(s"""{"tags": #{tagNames}}"""))
              .asJson
              .check(status.is(HTTP_NO_CONTENT))
          )
          .pause(pauseDuration, pauseStdDev)
      }

  override def scnTitle: String = "Create and Delete Multiple Tags"
}
