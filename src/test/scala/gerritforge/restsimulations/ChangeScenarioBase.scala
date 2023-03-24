package gerritforge.restsimulations

import gerritforge.EncodeUtils.encode
import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.circe.parser._
import io.circe.generic.auto._

import java.util.Calendar

trait ChangeScenarioBase extends ScenarioBase {

  case class ChangeDetail(
      _number: Int,
      project: String,
      current_revision: String,
      change_id: String
  ) {
    lazy val url = s"/c/$project/+/${_number}/"
  }
  def createChange =
    http("Create Change")
      .post("/changes/")
      .headers(postApiHeader(testConfig.xsrfToken))
      .body(StringBody(s"""{"project":"${testConfig.encodedProject}",
           |"branch":"master",
           |"subject":"Test commit subject - ${Calendar.getInstance().getTime}"}""".stripMargin))

  def listChanges =
    http("changes list and get first change")
      .get(s"/q/status:open+project:${testConfig.encodedProject}")
      .headers(restApiHeader)
      .resources(
        http("get list of changes")
          .get(
            s"/changes/?n=500&q=status%3Aopen+project:${testConfig.encodedProject}&o=CURRENT_REVISION"
          )
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
      )

  def pickRandomChange =
    doIf(session => session("changeDetails").as[List[ChangeDetail]].nonEmpty) {
      exec { session =>
        val changes = session("changeDetails").as[List[ChangeDetail]]
        val change  = changes(randomNumber.nextInt(changes.size))
        session
          .set("id", s"${encode(change.project)}~${change._number}")
          .set("changeUrl", change.url)
          .set("changeNum", change._number)
          .set("changeId", change.change_id)
          .set("revision", encode(change.current_revision))
      }
    }

  def authenticatedChangesPostRequest(title: String, url: String, body: String = "{}") =
    http(title)
      .post(s"/changes/${testConfig.encodedProject}~#{changeNumber}$url")
      .headers(postApiHeader(testConfig.xsrfToken))
      .body(StringBody(body))
}
