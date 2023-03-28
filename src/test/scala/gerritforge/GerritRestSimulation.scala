//package gerritforge
//
//import gerritforge.GerritTestConfig._
//import gerritforge.restscenarios.tags.CreateTag.httpProtocol
//import io.gatling.core.Predef._
//
//import scala.concurrent.duration.FiniteDuration
//
//class GerritRestSimulation extends SimulationBase {
//
//  val scenarios =
//    if (testConfig.restRunAnonymousUser)
//      anonymousScenarios.map(_.scn) ++ authenticatedScenarios.map(_.scn)
//    else authenticatedScenarios.map(_.scn)
//
//  require(httpProtocol.isDefined, "GERRIT_HTTP_URL must be defined to run REST-API simulation")
//
//  setUp(
//    scenarios.map(
//      _.inject(rampConcurrentUsers(1) to testConfig.numUsers during testConfig.duration)
//        .pauses(normalPausesWithStdDevDuration(FiniteDuration(5, "seconds")))
//    )
//  ).protocols(httpProtocol)
//}
