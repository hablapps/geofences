package org.hablapps.geofences
package services
package http4sImpl
package test

import cats._, cats.implicits._
import cats.effect.IO
import org.http4s._

import services._, ViewData._, java.sql.Timestamp
import org.hablapps.puretest._, scalatestImpl._

class AllHttp4sSpecs extends services.test.scalatest.AllSpecs[IO](
  AllHttp4sSpecs.HError,
  AllHttp4sSpecs.RError,
  AllHttp4sSpecs.Tester,
  new Http4sSystemClient(Uri.uri("http://127.0.0.1:8080")))(
  AllHttp4sSpecs.ScalazMonadIO)

object AllHttp4sSpecs{

  val TestSystem = new Http4sSystemClient(Uri.uri("http://127.0.0.1:8080"))

  implicit val ScalazMonadIO = new scalaz.Monad[IO]{
    def point[A](a: => A) = Monad[IO].pure(a)
    def bind[A,B](p: IO[A])(f: A => IO[B]) = Monad[IO].flatMap(p)(f)
  }

  val HError = new HandleError[IO,System.Error]{
    def handleError[A](fa: IO[A])(f: System.Error => IO[A]): IO[A] =
      fa.attempt flatMap {
        _.fold({
          case serror: System.Error => f(serror)
          case other => IO.raiseError(other)
        },_.pure[IO])
      }
  }

  // should be provided by puretest
  case class PuretestErrorException[E](error: PuretestError[E])
  extends RuntimeException(error.toString)

  val RError = new RaiseError[IO, PuretestError[System.Error]] {
    def raiseError[A](e: PuretestError[System.Error]): IO[A] =
      IO.raiseError(PuretestErrorException(e))
  }

  def tester(S: System[IO]) = new Tester[IO,PuretestError[System.Error]]{
    def apply[X](t: IO[X]): Either[PuretestError[System.Error],X] =
      (S.run(t).attempt map {
          case Left(perror: PuretestErrorException[System.Error]@unchecked) => Left(perror.error)
          case Left(t) => throw t // should be captured in PuretestError
          case Right(other) => Right(other)
        }).unsafeRunSync()
  }

  val Tester = tester(TestSystem)
}


