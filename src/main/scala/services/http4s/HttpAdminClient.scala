package org.hablapps.geofences
package services
package http4sImpl

import cats.effect.IO

import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._

import io.circe.generic.auto._
import io.circe.syntax._

import ViewData._

class HttpAdminClient(root: Uri, httpClient: Client[IO]) extends AdminView[IO] with NetworkEntityDecoders[IO]{

  def add(reg: Region): IO[GID] = {
    val req = POST(root / "admin", reg.asJson)
    httpClient.expect(req)(jsonOf[IO, GID])
  }

  def remove(gid: GID): IO[Unit] = {
    val req = DELETE(root / "admin" / gid.toString)
    httpClient.fetch[Unit](req) { response =>
        response.status match {
          case Ok => response.as[Unit]
          case BadRequest =>
            response.as[System.Error] flatMap IO.raiseError
          case _ =>
            IO.raiseError(new RuntimeException(response.toString))
        }
      }
  }

  def getAll(): IO[List[Geofence]] = {
    val req = GET(root / "admin")
    httpClient.expect(req)(jsonOf[IO, List[Geofence]])
  }

  def get(gid: GID): IO[Option[Geofence]] = {
    val req = GET(root / "admin" / gid.toString)
    httpClient.expect(req)(jsonOf[IO, Option[Geofence]])
  }
}
