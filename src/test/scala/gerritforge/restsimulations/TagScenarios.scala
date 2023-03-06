package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.GatlingRestUtils._
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.generic.auto._

import scala.util.Random

object TagScenarios extends ScenarioBase {

  case class TagDetail(ref: String, revision: String)

  val numTags = 500

  val randomNumTags = new Random

  val randomTagNumbers = new Random

  val createTag = {
    setupAuthenticatedSession("Create a new Tag")
      .foreach(
        Range(1, randomNumTags.nextInt(TagScenarios.numTags))
          .map(s"${System.nanoTime}-" + _)
          .toSeq,
        "tagNum"
      ) {
        exec(
          http("create tag")
            .put(s"/projects/${testConfig.project}/tags/tag-#{tagNum}")
            .headers(postApiHeader(testConfig.xsrfToken))
            .body(StringBody("""{"revision":"HEAD"}"""))
            .asJson
        )
      }
  }

  val deleteTags = {
    setupAuthenticatedSession("List and remove a Tag")
      .exec(
        http("list tags")
          .get(s"/projects/${testConfig.project}/tags/?n=${numTags}&S=0")
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
      .doIf(session => !session("tagDetails").as[List[TagDetail]].isEmpty) {
        exec { session =>
          val tags    = session("tagDetails").as[List[TagDetail]]
          val numTags = randomNumTags.nextInt(tags.size)
          val randomTagRefs = Random
            .shuffle(tags)
            .drop(numTags)
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
