package gerritforge.scenarios

import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.Predef.scenario
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}

trait ScenarioBase extends PauseScenarioSettings {
  def scnTitle: String
  def scn: ScenarioBuilder = scenario(scnTitle).exec(scnActions)

  def scnActions: ChainBuilder

  var alreadyFedUsers: List[String] = List.empty
  val userIdFeeder =
    (simulationConfig.usersOffset until simulationConfig.usersOffset + simulationConfig.numUsers)
      .map(userId => Map("userId" -> s"user-$userId"))
}
