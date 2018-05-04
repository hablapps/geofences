package org.hablapps.geofences
package services
package test
package scalatest
import scalaz._, Scalaz._

import puretest._

class AdminSpec[P[_]](
  val M: Monad[P],
  val HError: HandleError[P,System.Error],
  val RError: RaiseError[P,PuretestError[System.Error]],
  val Tester: Tester[P,PuretestError[System.Error]],
  val Sys: System[P]) extends scalatestImpl.FunSpec[P,System.Error] with services.test.AdminSpec[P]

class AtSpec[P[_]](
  val M: Monad[P],
  val HError: HandleError[P,System.Error],
  val RError: RaiseError[P,PuretestError[System.Error]],
  val Tester: Tester[P,PuretestError[System.Error]],
  val Sys: System[P]) extends scalatestImpl.FunSpec[P,System.Error] with services.test.AtSpec[P]

class AllSpecs[P[_]: Monad](
  HError: HandleError[P,System.Error],
  RError: RaiseError[P,PuretestError[System.Error]],
  Tester: Tester[P,PuretestError[System.Error]],
  Sys: System[P]) extends org.scalatest.Sequential(
  new AdminSpec[P](Monad[P],HError,RError,Tester,Sys),
  new AtSpec[P](Monad[P],HError,RError,Tester,Sys)
)
