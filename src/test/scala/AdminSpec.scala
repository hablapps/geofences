package org.hablapps.geofences
package services
package test

import scalaz._, Scalaz._
import services._, ViewData._, java.sql.Timestamp

import puretest._

trait AdminSpec[P[_]] extends FunSpec[P]{
  implicit val Sys: System[P]
  implicit val M: Monad[P]
  implicit val HError: HandleError[P,System.Error]
  implicit val RError: RaiseError[P,PuretestError[System.Error]]
  import Sys.{init, AdminView}, AdminView._

  Describe("Add service"){

    It("should work from empty state"){
      for {
        _ <- init()()
        1 <- add(Region((0,0),2))
        2 <- add(Region((0,0),2))
        Some(Geofence(1,Region((0,0),2))) <- get(1)
        Some(Geofence(2,Region((0,0),2))) <- get(2)
      } yield ()
    }

    It("should work from non-empty state"){
      init(Region((0,0),1))() >>
      (add(Region((0,0),2)) shouldBe 2) >>
      (getAll() shouldBe
        List(Geofence(1,Region((0,0),1)),Geofence(2,Region((0,0),2))))
    }

  }

  Describe("Remove service"){

    It("should remove existing region"){
      init(Region((0,0),1))() >>
      remove(1) *>
      (get(1) shouldBe None)
    }

    It("should attempt to remove non-existing region and return system error"){
      (init()() >> remove(1)) shouldFailWith[System.Error]
        System.Error.nonExisting(1)
    }
  }
}
