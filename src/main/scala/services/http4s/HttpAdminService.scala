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
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator
import scala.concurrent._
import scala.concurrent.duration._
import io.circe.syntax._
import io.circe.generic.auto._

import ViewData._
import puretest.HandleError

class HttpAdminService[F[_]: Effect](AdminView: AdminView[F])(implicit
  HE: HandleError[F,System.Error]
) extends ToHttpResponse[F,System.Error] with NetworkEntityDecoders[F]{

  val service = HttpService[F]{

    case GET -> Root / IntVar(gid) =>
      toHttpResponse(AdminView.get(gid))

    case GET -> Root =>
      toHttpResponse(AdminView.getAll)

    case req@ (POST -> Root) =>
      toHttpResponse(req.as[ViewData.Region] >>= AdminView.add)

    case DELETE -> Root / IntVar(gid) =>
      toHttpResponse(AdminView.remove(gid))
  }
}
