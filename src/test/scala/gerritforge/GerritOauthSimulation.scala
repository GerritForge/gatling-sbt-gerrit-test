package gerritforge

import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.oauth.{InvalidCredentialsLogin, SuccessfulLogin}
import io.gatling.core.Predef._
import io.gatling.http.Predef.http

import scala.concurrent.duration.FiniteDuration

class GerritOauthSimulation extends Simulation {

  private val scenarios = {
    List(
      SuccessfulLogin,
      InvalidCredentialsLogin
    )
  }

  private val protocol = http
    .baseUrl(simulationConfig.httpUrl.get)
    .disableFollowRedirect

  val pauseStdDevSecs = 5
  setUp(
    scenarios.map(
      _.scn
        .inject(
          rampConcurrentUsers(1) to simulationConfig.numUsers during simulationConfig.duration
        )
        .pauses(normalPausesWithStdDevDuration(FiniteDuration(pauseStdDevSecs, "seconds")))
        .protocols(protocol)
    )
  ).protocols(protocol).maxDuration(simulationConfig.duration)
}
