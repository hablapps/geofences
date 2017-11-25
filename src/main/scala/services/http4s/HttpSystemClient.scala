package org.hablapps.geofences
package services
package http4sImpl

import cats.effect.IO
import cats._, cats.implicits._

import org.http4s._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._

class Http4sSystemClient(root: Uri) extends System[IO]{

  val httpClient = PooledHttp1Client[IO]()

  def init: IO[Unit] = {
    val req = POST(root / "init")
    httpClient.expect(req)(jsonOf[IO, Unit])
  }

  def dispose: IO[Unit] = {
    val req = POST(root / "dispose")
    httpClient.expect(req)(jsonOf[IO, Unit])
  }

  val AdminView = new HttpAdminClient(root, httpClient)
  lazy val DeviceView = new HttpDeviceClient(root, httpClient)
}
