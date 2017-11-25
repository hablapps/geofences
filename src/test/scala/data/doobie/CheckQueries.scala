package org.hablapps.geofences
package data
package doobieImpl
package test

import scalaz._, Scalaz._
import doobie.imports._, doobie.scalatest.imports._
import org.scalatest._

import services._, ViewData._, java.sql.Timestamp
import data.doobieImpl.Schemas, Schemas._

class CheckQueries extends FunSpec with Matchers with IOLiteChecker with BeforeAndAfterAll{

  val transactor = DriverManagerTransactor[IOLite](
    "org.postgresql.Driver", "jdbc:postgresql:testing", "postgres", "habla667$"
  )

  override def beforeAll {
    Schemas.setUpTables.transact[IOLite](transactor).unsafePerformIO
  }

  // Check At table queries

  describe("Check 'at' table queries"){
    it("should type check"){
      check(Devices.at(1,(1,1),new Timestamp(1)))
    }
  }

  // Check WhereIn table queries

  describe("Check 'wherein' table queries"){
    it("should type check"){

      check(WhereIn.enter(1,1,new Timestamp(1)))
      check(WhereIn.exit(1,1))
    }
  }

  // Check Geofences table queries

  describe("Check 'geofences' table queries"){
    it("should type check"){
      check(Geofences.createTable)
      check(Geofences.dropTable)
      check(Geofences.add(Region((0,0),1)))
      check(Geofences.add(Geofence(1,Region((0,0),1))))
      check(Geofences.getAll)
    }
  }

  // Check GeoEvents table queries

  override def afterAll {
    Schemas.dropTables.transact[IOLite](transactor).unsafePerformIO
  }

}