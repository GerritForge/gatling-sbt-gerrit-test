//package gerritforge
//
//import gerritforge.GerritTestConfig._
//import gerritforge.scenarios.rest.tags.CreateTag.httpProtocol
//import io.gatling.core.Predef._
//
//import scala.concurrent.duration.FiniteDuration
//
//class GerritRestSimulation extends SimulationBase {
//
//  val scenarios =
//    if (testConfig.restRunAnonymousUser)
//      allRestScenarios
//    else authenticatedScenarios
//
//  require(httpProtocol.isDefined, "GERRIT_HTTP_URL must be defined to run REST-API simulation")
//
//  val pauseStdDevSecs = 5
//  setUp(
//    scenarios.toList.map(
//      _.scn
//        .inject(rampConcurrentUsers(1) to testConfig.numUsers during testConfig.duration)
//        .pauses(normalPausesWithStdDevDuration(FiniteDuration(pauseStdDevSecs, "seconds")))
//    )
//  ).protocols(httpProtocol)
//}
