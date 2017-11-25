package org.hablapps.geofences
package services
package repos
package test

import org.hablapps.puretest._, scalatestImpl._
import doobie.imports.ConnectionIO

class AllReposDoobieSpecs extends services.test.scalatest.AllSpecs(
  services.doobieImpl.test.AllDoobieSpecs.HError,
  services.doobieImpl.test.AllDoobieSpecs.RError,
  services.doobieImpl.test.AllDoobieSpecs.tester(AllReposDoobieSpecs.System),
  AllReposDoobieSpecs.System)

object AllReposDoobieSpecs{
  val System = new SystemRepos(data.repos.NetworkRepoDoobie)
}