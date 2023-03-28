package gerritforge

import io.gatling.core.Predef.normalPausesWithStdDevDuration

import scala.concurrent.duration.FiniteDuration

trait PauseSimulation {
  def simulationName: String

  lazy val pauseDuration: FiniteDuration = {
    println(s"simulation name is:$simulationName")
    FiniteDuration(sys.env.getOrElse(s"${simulationName}_PAUSE", "0").toLong, "sec")
  }
  lazy val stdDevDuration =
    FiniteDuration(sys.env.getOrElse(s"${simulationName}_STDDEV_PAUSE", "0").toLong, "seconds")
  lazy val pauseStdDev = normalPausesWithStdDevDuration(stdDevDuration)
  println(
    s"$simulationName: sleeping for ${pauseDuration.toMillis} ms with a ${stdDevDuration.toMillis}ms stdandard deviation"
  )
}
