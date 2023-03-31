package gerritforge.scenarios

import gerritforge.GerritTestConfig.testConfig
import io.gatling.core.structure.ScenarioBuilder

trait ScenarioBase extends PauseScenarioSettings {
  def scn: ScenarioBuilder

  var alreadyFedUsers: List[String] = List.empty
  val userIdFeeder                  = (1 to testConfig.numUsers).map(userId => Map("userId" -> s"user-$userId"))
}
