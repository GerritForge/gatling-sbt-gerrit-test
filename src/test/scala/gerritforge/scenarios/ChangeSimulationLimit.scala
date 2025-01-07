package gerritforge.scenarios

import gerritforge.config.SimulationConfig.simulationConfig

import java.util.concurrent.atomic.AtomicInteger

object ChangeSimulationLimit {
  private val maxNumberOfChanges = simulationConfig.maxNumChanges.getOrElse(Int.MaxValue)
  private val counter            = new AtomicInteger()

  def canCreateNewChange: Boolean =
    (counter.get() < maxNumberOfChanges) &&
      (counter.incrementAndGet() < maxNumberOfChanges)
}
