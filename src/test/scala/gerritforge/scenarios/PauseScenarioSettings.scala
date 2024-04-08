package gerritforge.scenarios

import scala.util.Random
import scala.concurrent.duration._

trait PauseScenarioSettings {
  def scenarioName: String = {
    this.getClass.getSimpleName match {
      case simpleName if simpleName.endsWith("$") => simpleName.dropRight(1)
      case simpleName                             => simpleName
    }
  }

  lazy val random: Random = {
    val seed: Option[Long] = sys.env.get("STDDEV_SEED").map(_.toLong)
    seed.fold(new Random())(new Random(_))
  }

  lazy val pauseDuration: FiniteDuration =
    FiniteDuration(sys.env.getOrElse(s"${scenarioName}_PAUSE", "0").toLong, "seconds")
  lazy val stdDevDuration: Long = sys.env.getOrElse(s"${scenarioName}_STDDEV_PAUSE", "0").toLong
  println(
    s"$scenarioName: sleeping for ${pauseDuration.toMillis} ms with a ${stdDevDuration / 1000}ms stdandard deviation"
  )
  lazy val pauseStdDev: FiniteDuration = (random.nextGaussian() * stdDevDuration).seconds
}
