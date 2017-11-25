package org.hablapps.geofences
package services
package http4sImpl

import cats._
import cats.effect._
import cats.implicits._
import fs2.{Scheduler, Stream}
import io.circe.Json
import org.http4s._
import org.http4s.MediaType._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.server._
import scala.concurrent._
import scala.concurrent.duration._
import io.circe.generic.auto._
import io.circe.syntax._
import org.hablapps.puretest.HandleError

class HttpSystemService[F[_]](
  system: System[F])(implicit
  E: Effect[F],
  HE: HandleError[F,System.Error]
) extends ToHttpResponse[F,System.Error] {

  def service(implicit scheduler: Scheduler,
      executionContext: ExecutionContext = ExecutionContext.global): HttpService[F] =
    Router[F](
      "/" -> rootHttpService,
      "/admin" -> (new HttpAdminService(system.AdminView)).service,
      "/devices" -> (new HttpDeviceService(system.DeviceView)).service
    )

  val rootHttpService = HttpService[F]{
    case POST -> Root / "init" =>
      toHttpResponse(system.init)
    case POST -> Root / "dispose" =>
      toHttpResponse(system.dispose)
  }
}

object HttpNetworkService{

  import org.http4s.server.blaze._
  import org.http4s.server.blaze.BlazeBuilder
  import org.http4s.util.{ExitCode, StreamApp}

  class App[F[_]](
    system: System[F])(implicit
    E: Effect[F],
    HE: HandleError[F,System.Error]
  ) extends StreamApp[F] {

    def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] =
      Scheduler(corePoolSize = 2).flatMap{ implicit scheduler =>
        BlazeBuilder[F]
          .bindHttp(8080)
          .mountService((new HttpSystemService(system)(E,HE)).service, "/")
          .serve
      }
  }
}
