package org.hablapps.geofences
package services
package test

import scalaz._, Scalaz._
import puretest._
import services._, ViewData._, java.sql.Timestamp

trait AtSpec[P[_]] extends FunSpec[P]{
  implicit val Sys: System[P]
  implicit val M: Monad[P]
  implicit val HError: HandleError[P,System.Error]
  implicit val RError: RaiseError[P,PuretestError[System.Error]]
  import Sys._

  Describe("At service"){

    It("should update position"){
      DeviceView.at(1,(0,0),new Timestamp(1)) >>
      (DeviceView.get(1) shouldBe Some(Device(1,(0,0),new Timestamp(1)))) >>
      DeviceView.at(1,(0,1),new Timestamp(2)) >>
      (DeviceView.get(1) shouldBe Some(Device(1,(0,1),new Timestamp(2))))
    }

    It("should enter if it wasn't in before"){
      init(Region((0,0),2))() >> {
        val atOutput = List(Enter(1,1,new Timestamp(1)))

        (DeviceView.at(1,(0,0),new Timestamp(1)) shouldBe atOutput) >>
        (DeviceView.get(1) shouldBe Some(Device(1,(0,0),new Timestamp(1)))) >>
        (DeviceView.at(1,(0,0),new Timestamp(1)) shouldBe List()) >>
        (DeviceView.get(1) shouldBe Some(Device(1,(0,0),new Timestamp(1))))
      }
    }

    It("should exit"){
      init(Region((0,0),2))((1,(0,0),new Timestamp(0))) >> {
        val atOutput = List(Exit(1,1,new Timestamp(1)))

        (DeviceView.at(1,(0,5),new Timestamp(1)) shouldBe atOutput) >>
        (DeviceView.get(1) shouldBe Some(Device(1,(0,5),new Timestamp(1)))) >>
        (DeviceView.at(1,(0,5),new Timestamp(1)) shouldBe List())
      }
    }

    It("should not enter/exit if it still stays in"){
      init(Region((0,0),2))((1,(0,0),new Timestamp(0))) >> {
        val atOutput = List()

        (DeviceView.at(1,(0,0),new Timestamp(1)) shouldBe atOutput) >>
        (DeviceView.get(1) shouldBe Some(Device(1,(0,0),new Timestamp(1))))
      }
    }

    It("should not enter/exit if it still stays out"){
      init(Region((0,0),2))() >> {
        val atOutput = List()

        (DeviceView.at(1,(0,5),new Timestamp(1)) shouldBe atOutput) >>
        (DeviceView.get(1) shouldBe Some(Device(1,(0,5),new Timestamp(1))))
      }
    }

    It("should enter simultaneously in several geofences"){
      init(Region((0,0),2),Region((2,0),2))() >> {
        val atOutput = Set[GeoEvent](
          Enter(1,1,new Timestamp(1)),
          Enter(2,1,new Timestamp(1)))

        (DeviceView.at(1,(1,0),new Timestamp(1)).map(_.toSet[GeoEvent]) shouldBe atOutput) >>
        (DeviceView.at(1,(1,0),new Timestamp(1)).map(_.toSet[GeoEvent]) shouldBe Set())
      }
    }

    It("should exit simultaneously from several geofences"){
      init(Region((0,0),2),Region((2,0),2))((1,(0,0),new Timestamp(0))) >> {
        val atOutput = Set[GeoEvent](
          Exit(1,1,new Timestamp(1)),
          Exit(2,1,new Timestamp(1)))

        (DeviceView.at(1,(5,0),new Timestamp(1)).map(_.toSet[GeoEvent]) shouldBe atOutput) >>
        (DeviceView.at(1,(5,0),new Timestamp(1)).map(_.toSet[GeoEvent]) shouldBe Set())
      }
    }

    It("should enter/exit simultaneously"){
      init(Region((0,0),2),Region((2,0),2))((1,(-1,0),new Timestamp(0))) >> {
        val atOutput = Set[GeoEvent](
          Exit(1,1,new Timestamp(1)),
          Enter(2,1,new Timestamp(1)))

        (DeviceView.at(1,(3,0),new Timestamp(1)).map(_.toSet[GeoEvent]) shouldBe atOutput) >>
        (DeviceView.at(1,(3,0),new Timestamp(1)).map(_.toSet[GeoEvent]) shouldBe Set())
      }
    }
  }
}
