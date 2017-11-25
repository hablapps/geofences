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
import java.sql.Timestamp
import ViewData._

class HttpDeviceClient(root: Uri, httpClient: Client[IO]) extends DeviceView[IO]{

  def at(did: DID, pos: Position, time: Timestamp): IO[List[GeoEvent]] = {
    val req = POST(root / "devices", (did,pos,time).asJson)
    httpClient.expect(req)(jsonOf[IO, List[GeoEvent]])
  }

  def get(did: DID): IO[Option[Device]] = {
    val req = GET(root / "devices" / did.toString)
    httpClient.expect(req)(jsonOf[IO, Option[Device]])
  }
}
