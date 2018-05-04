package org.hablapps.geofences
package services

package object http4sImpl{

  import cats._
  import cats.effect._
  import cats.implicits._
  import cats.syntax._
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
  import io.circe._
  import io.circe.generic.auto._

  import puretest.HandleError

  trait ToHttpResponse[F[_],E] extends Http4sDsl[F]{
    def toHttpResponse[A: Encoder](
        f: F[A])(implicit
        HE: HandleError[F,E],
        Ef: Effect[F],
        EncE: Encoder[E]): F[Response[F]] =
      HE.handleError(
        f.flatMap(value => Ok(value.asJson))
      ){ e: E => BadRequest(e.asJson) }
  }

  // Borrowed from https://stackoverflow.com/questions/41431085/how-to-encode-decode-timestamp-for-json-in-circe
  import java.sql.Timestamp

  implicit val TimestampFormat : Encoder[Timestamp] with Decoder[Timestamp] =
    new Encoder[Timestamp] with Decoder[Timestamp] {
      override def apply(a: Timestamp): Json =
        Encoder.encodeLong.apply(a.getTime)
      override def apply(c: HCursor): Decoder.Result[Timestamp] =
        Decoder.decodeLong.map(s => new Timestamp(s)).apply(c)
    }

  import ViewData._

  trait NetworkEntityDecoders[F[_]]{
    implicit def tupleDecoder(implicit Ef: Effect[F]): EntityDecoder[F,(DID,Position,Timestamp)] =
      jsonOf[F, (DID,Position,Timestamp)]

    implicit def regionDecoder(implicit Ef: Effect[F]): EntityDecoder[F,Region] = jsonOf[F, Region]

    implicit def errorDecoder(implicit Ef: Effect[F]): EntityDecoder[F,System.Error] = jsonOf[F, System.Error]
  }

}