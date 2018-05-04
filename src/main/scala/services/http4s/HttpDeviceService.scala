package org.hablapps.geofences
package services
package http4sImpl


import java.sql.Timestamp

import cats._
import cats.effect._
import cats.implicits._
import fs2.{Scheduler, Stream}
import io.circe._
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
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

import ViewData._
import puretest.HandleError


class HttpDeviceService[F[_]: Effect](DeviceView: DeviceView[F])(implicit
  HE: HandleError[F,System.Error]
) extends ToHttpResponse[F,System.Error] with NetworkEntityDecoders[F]{

  val service = HttpService[F]{

    case GET -> Root / IntVar(did) =>
      toHttpResponse(DeviceView.get(did))

    case req@ (POST -> Root) =>
      toHttpResponse(req.as[(DID,Position,Timestamp)] >>= {
        case (did,pos,time) => DeviceView.at(did,pos,time)
      })
  }
}
