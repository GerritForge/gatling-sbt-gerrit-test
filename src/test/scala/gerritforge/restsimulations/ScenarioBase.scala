package gerritforge.restsimulations

import gerritforge.ChangeDetail
import gerritforge.ChangesListScenario.randomNumber
import gerritforge.GerritTestConfig.testConfig
import gerritforge.restsimulations.GatlingRestUtils.firstOpenChangeDetails
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

trait ScenarioBase {
  val scn: ScenarioBuilder

  def setupAuthenticatedSession(scnTitle: String): ScenarioBuilder = {
    testConfig.accountCookie match {
      case Some(cookie) =>
        scenario(scnTitle)
          .exec(addCookie(Cookie("GerritAccount", cookie)))
          .exec(firstOpenChangeDetails(testConfig.project))
          .doIf(session => session("changeDetails").as[List[ChangeDetail]].nonEmpty) {
            exec { session =>
              val changes: Seq[ChangeDetail] = session("changeDetails").as[List[ChangeDetail]]
              val change                     = changes(randomNumber.nextInt(changes.size))
              session.set(
                "changeNumber",
                change._number
              )
            }
          }
          .pause(4 seconds)
      case None => throw new Exception("Requires authentication")
    }
  }
}
