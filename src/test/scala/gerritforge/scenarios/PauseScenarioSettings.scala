package gerritforge.scenarios

import io.gatling.core.pause.NormalWithStdDevDuration

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
    sys.env.getOrElse(s"${scenarioName}_PAUSE", "0").toLong seconds
  lazy val stdDevDuration: FiniteDuration =
    sys.env.getOrElse(s"${scenarioName}_STDDEV_PAUSE", "0").toLong seconds

  println(
    s"$scenarioName: sleeping for ${pauseDuration.toMillis} ms with a ${stdDevDuration.toMillis}ms standard deviation"
  )
  lazy val pauseType = new NormalWithStdDevDuration(random.nextGaussian().toLong * stdDevDuration)
}
