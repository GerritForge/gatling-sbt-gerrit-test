package gerritforge

import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.oauth.{InvalidCredentialsLogin, SuccessfulLogin}
import io.gatling.core.Predef._
import io.gatling.http.Predef.http

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

  setUp(
    scenarios.map(
      _.scn
        .inject(
          rampConcurrentUsers(1) to simulationConfig.numUsers during simulationConfig.duration
        )
        .protocols(protocol)
    )
  ).protocols(protocol).maxDuration(simulationConfig.duration)
}
