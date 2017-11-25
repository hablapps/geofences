package org.hablapps.geofences
package services
package repos
package test

import scalaz._, Scalaz._

import services._, ViewData._, java.sql.Timestamp

import org.scalatest._
import org.hablapps.puretest._

import data.immutable._
import data.repos.NetworkRepoImmutable

import AllReposImmutableSpecs.Program

class AllReposImmutableSpecs extends services.test.scalatest.AllSpecs(
  HandleError[Program,System.Error],
  RaiseError[Program,PuretestError[System.Error]],
  StateTester[Program,Network,PuretestError[System.Error]].apply(Network(1,Map(),Map(),Map())),
  new SystemRepos[Program](new NetworkRepoImmutable[Program]))

object AllReposImmutableSpecs{
  type Program[T] = StateT[PuretestError[System.Error] \/ ?,Network,T]
}
