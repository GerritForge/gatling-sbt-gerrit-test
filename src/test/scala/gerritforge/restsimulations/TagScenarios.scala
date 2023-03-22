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

  val numTags = 500

  val randomNumTags = new Random

  val randomTagNumbers = new Random

  val createTag = {
    setupAuthenticatedSession("Create a new Tag")
      .feed(Iterator.continually(Map("tagId" -> UUID.randomUUID())))
      .exec(
        http("create tag")
          .put(s"/projects/${testConfig.project}/tags/tag-#{tagId}")
          .headers(postApiHeader(testConfig.xsrfToken))
          .body(StringBody("""{"revision":"HEAD"}"""))
          .asJson
      )
  }

  val deleteTags = {
    setupAuthenticatedSession("List and remove a Tag")
      .feed((1 to numTags).map(i => Map("tagGroupId" -> i)).circular)
      .exec(
        http("list tags")
          .get(s"/projects/${testConfig.project}/tags/?n=150&m=-#{tagGroupId}")
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
          val tags = session("tagDetails").as[List[TagDetail]]
          val randomTagRefs = tags
            .map(_.ref)
            .map(_.drop("refs/tags/".length))
          val tagNames = randomTagRefs.asJson
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
