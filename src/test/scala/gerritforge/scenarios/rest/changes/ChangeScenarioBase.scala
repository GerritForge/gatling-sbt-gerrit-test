package gerritforge.scenarios.rest.changes

import gerritforge.EncodeUtils.encode
import gerritforge.config.GerritTestConfig.gerritTestConfig
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.rest.RestScenarioBase
import gerritforge.scenarios.rest.changes.ChangeScenarioBase.ChangeDetail
import io.circe.generic.auto._
import io.circe.parser._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.Calendar
import scala.util.Random

trait ChangeScenarioBase extends RestScenarioBase {

  def createChange =
    http("Create Change")
      .post("/changes/")
      .headers(addApiHeaders(gerritTestConfig.xsrfToken))
      .body(StringBody(s"""{"project":"${simulationConfig.project}",
           |"branch":"master",
           |"subject":"Test commit subject - ${Calendar.getInstance().getTime}"}""".stripMargin))

  def listChanges(filters: Option[String] = None) = {
    http("changes list and get first change")
      .get(
        s"/changes/?n=500&q=status%3Aopen+project:${simulationConfig.encodedProject}${filters.getOrElse("")}&o=CURRENT_REVISION"
      )
      .headers(restApiHeader)
      .check(
        bodyString
          .transform(_.drop(XSS_LEN))
          .transform(decode[List[ChangeDetail]](_))
          .transform {
            case Right(changeDetailList) => changeDetailList
            case Left(decodingError)     => throw decodingError
          }
          .saveAs("changeDetails")
      )
  }

  def listChangesWithHashtags(hashtags: List[String]) = {
    val hashtagQuery = hashtags.map(h => s"hashtag:$h").mkString("+")
    listChanges(Some(s"+$hashtagQuery"))
  }

  def pickRandomChange =
    doIf(session => session("changeDetails").as[List[ChangeDetail]].nonEmpty) {
      exec { session =>
        val changes = session("changeDetails").as[List[ChangeDetail]]
        val change  = changes(Random.nextInt(changes.size))
        session.setAll(
          "id"           -> s"${encode(change.project)}~${change._number}",
          "changeUrl"    -> change.url,
          "changeNumber" -> change._number,
          "changeId"     -> change.change_id,
          "revision"     -> encode(change.current_revision)
        )
      }
    }

  def authenticatedChangesPostRequest(title: String, url: String, body: String = "{}") =
    http(title)
      .post(s"/changes/${simulationConfig.encodedProject}~#{changeNumber}$url")
      .headers(addApiHeaders(gerritTestConfig.xsrfToken))
      .body(StringBody(body))
}

object ChangeScenarioBase {
  final case class ChangeDetail(
      _number: Int,
      project: String,
      current_revision: String,
      change_id: String
  ) {
    lazy val url = s"/c/$project/+/${_number}/"
  }
}
