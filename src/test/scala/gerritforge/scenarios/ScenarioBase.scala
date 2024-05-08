package gerritforge.scenarios

import gerritforge.config.SimulationConfig.simulationConfig
import io.gatling.core.structure.ScenarioBuilder

trait ScenarioBase extends PauseScenarioSettings {
  def scn: ScenarioBuilder

  var alreadyFedUsers: List[String] = List.empty
  val userIdFeeder =
    (1 to simulationConfig.numUsers).map(userId => Map("userId" -> s"user-$userId"))
}
