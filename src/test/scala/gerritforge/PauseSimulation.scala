package gerritforge

import io.gatling.core.Predef.normalPausesWithStdDevDuration

import scala.concurrent.duration.FiniteDuration

trait PauseSimulation {
  def scenarioName: String = this.getClass.getSimpleName.dropRight(1) //To drop final `$`

  lazy val pauseDuration: FiniteDuration =
    FiniteDuration(sys.env.getOrElse(s"${scenarioName}_PAUSE", "0").toLong, "seconds")
  lazy val stdDevDuration =
    FiniteDuration(sys.env.getOrElse(s"${scenarioName}_STDDEV_PAUSE", "0").toLong, "seconds")
  lazy val pauseStdDev = normalPausesWithStdDevDuration(stdDevDuration)
  println(
    s"$scenarioName: sleeping for ${pauseDuration.toMillis} ms with a ${stdDevDuration.toMillis}ms stdandard deviation"
  )
}
