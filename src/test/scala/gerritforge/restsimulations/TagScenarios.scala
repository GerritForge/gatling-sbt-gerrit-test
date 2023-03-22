package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.GatlingRestUtils._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.generic.auto._

import java.util.UUID
import scala.util.Random

object TagScenarios extends ScenarioBase {

  case class TagDetail(ref: String, revision: String)

  val numTagGroups       = 500
  val tagsToDeleteAtOnce = 150

  val randomNumTags = new Random

  val randomTagNumbers = new Random

  /*
  We tag groupIds to 3 digits, so that groupId 1 becomes 001, so on so forth.
  This is to avoid that when querying for groupId 1 we also select group 10,11,100,[...]
   */
  def tagGroupIds = {
    def padWithLeadingZeros(num: Int) = f"$num%03d"
    (1 to numTagGroups).map(tagGroup => Map("tagGroupId" -> padWithLeadingZeros(tagGroup))).circular
  }

  val createTag =
    setupAuthenticatedSession("Create a new Tag")
      .feed(tagGroupIds)
      .feed(
        Iterator.continually(Map("tagId" -> s"${System.currentTimeMillis()}-${UUID.randomUUID()}"))
      )
      .exec(
        http("create tag")
          .put(s"/projects/${testConfig.project}/tags/tag-#{tagId}-#{tagGroupId}")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{"revision":"HEAD"}"""))
          .asJson
      )

  val deleteTags = {
    setupAuthenticatedSession("List and remove a Tag")
      .feed(tagGroupIds)
      .exec(
        http("list tags")
          .get(s"/projects/${testConfig.project}/tags/?n=$tagsToDeleteAtOnce&m=-#{tagGroupId}")
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
            .post(s"/projects/${testConfig.project}/tags:delete")
            .headers(postApiHeader(testConfig.xsrfToken))
            .body(StringBody("""{"tags": #{tagNames}}"""))
            .asJson
        )
      }
  }

  override val scns: List[ScenarioBuilder] = List(createTag, deleteTags)
}
