package com.abdullah.todo.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling")

  val scn = scenario("Basic Simulation")
    .exec(
      http("health_check")
        .get("/actuator/health")
        .check(status.is(200))
    )

  setUp(
    scn.inject(atOnceUsers(10))
  ).protocols(httpProtocol)
}
