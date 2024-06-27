package gerritforge

import gerritforge.GerritOauthSimulation.oauthScenarios
import gerritforge.SimulationUtil.httpProtocol
import gerritforge.config.SimulationConfig.simulationConfig
import gerritforge.scenarios.oauth.{InvalidCredentialsLogin, InvalidUserLogin, SuccessfulLogin}
import gerritforge.scenarios.rest.changes._
import gerritforge.scenarios.rest.tags.{CreateAndDeleteMultipleTags, CreateAndDeleteTag}
import io.gatling.core.Predef._
import io.gatling.http.Predef.http

import scala.concurrent.duration.FiniteDuration

class GerritOauthSimulation extends Simulation {

  val scenarios = oauthScenarios

  val protocol = http
    .baseUrl("http://host.docker.internal:8888")
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

object GerritOauthSimulation {
  val oauthScenarios = List(
    SuccessfulLogin,
    InvalidCredentialsLogin,
    InvalidUserLogin
  )
}
