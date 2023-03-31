package gerritforge.scenarios

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.structure.ScenarioBuilder

trait ScenarioBase extends PauseScenarioSettings {
  def scn: ScenarioBuilder

  val userIdFeeder = (1 to testConfig.numUsers).map(userId => Map("userId" -> s"user-$userId"))
}
