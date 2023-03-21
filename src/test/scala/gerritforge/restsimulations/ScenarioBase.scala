package gerritforge.restsimulations

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

trait ScenarioBase {

  val XSS_LEN = 5

  val scns: List[ScenarioBuilder]

  def setupAuthenticatedSession(scnTitle: String): ScenarioBuilder = {
    testConfig.accountCookie match {
      case Some(cookie) =>
        scenario(scnTitle)
          .exec(addCookie(Cookie("GerritAccount", cookie)))
      case None => throw new Exception("Requires authentication")
    }
  }
}
