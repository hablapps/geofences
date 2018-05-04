package org.hablapps.geofences
package services
package doobieImpl
package test

import scalaz._, Scalaz._
import doobie.imports._

import services._, ViewData._, java.sql.Timestamp
import data.doobieImpl.Schemas._

import puretest._, scalatestImpl._

class AllDoobieSpecs extends services.test.scalatest.AllSpecs[ConnectionIO](
  AllDoobieSpecs.HError,
  AllDoobieSpecs.RError,
  AllDoobieSpecs.Tester,
  DoobieSystem)

object AllDoobieSpecs{

  val HError = new HandleError[ConnectionIO,System.Error]{
    def handleError[A](fa: ConnectionIO[A])(
        f: System.Error => ConnectionIO[A]): ConnectionIO[A] =
      Catchable[ConnectionIO].attempt(fa) flatMap {
        _.fold({
          case serror: System.Error => f(serror)
          case other => doobie.free.connection.fail(other)
        },_.pure[ConnectionIO])
      }
  }

  // should be provided by puretest
  case class PuretestErrorException[E](error: PuretestError[E])
  extends RuntimeException(error.toString)

  val RError = new RaiseError[ConnectionIO,PuretestError[System.Error]]{
    def raiseError[A](e: PuretestError[System.Error]): ConnectionIO[A] =
      doobie.free.connection.fail(PuretestErrorException(e))
  }

  // TODO: From StateTester
  def tester(S: System[ConnectionIO]) = new Tester[ConnectionIO,PuretestError[System.Error]]{
    val xa = DriverManagerTransactor[IOLite](
      "org.postgresql.Driver", "jdbc:postgresql:testing", "postgres", "habla667$"
    )

    def apply[X](t: ConnectionIO[X]): Either[PuretestError[System.Error],X] =
      S.run(t)
        .transact[IOLite](xa)
        .attempt
        .map(_.leftMap{
          case perror: PuretestErrorException[System.Error]@unchecked => perror.error
          case error => throw error // should be wrapped in PuretestError
        })
        .unsafePerformIO
        .toEither
  }

  val Tester = tester(DoobieSystem)

}


