package gerritforge.scenarios

import gerritforge.PauseSimulation
import io.gatling.core.structure.ScenarioBuilder

trait ScenarioBase extends PauseSimulation {
  def scn: ScenarioBuilder

}
