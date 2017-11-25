package org.hablapps.geofences
package data
package repos

import scalaz._, Scalaz._

import services._, ViewData._, java.sql.Timestamp

import doobie.imports._
import org.postgresql.geometric._
import doobie.postgres.imports._
import doobie.postgres.pgistypes._

import data.doobieImpl.Schemas._

object NetworkRepoDoobie extends NetworkRepo[ConnectionIO]{

  val GeofenceRepo = GeofenceRepoDoobie
  val DeviceRepo = DeviceRepoDoobie
  val WhereInRepo = WhereInRepoDoobie

  def init: ConnectionIO[Unit] = setUpTables
  def dispose: ConnectionIO[Unit] = dropTables
}

object GeofenceRepoDoobie extends GeofenceRepo[ConnectionIO]{

  def addGeofence(region: Region): ConnectionIO[GID] =
    Geofences.add(region).withUniqueGeneratedKeys("gid")

  def removeGeofence(gid: GID): ConnectionIO[Unit] =
    Geofences.remove(gid).run.flatMap{ count =>
      if (count == 0) doobie.free.connection.fail(System.Error.nonExisting(gid))
      else ().point[ConnectionIO]
    }

  def getGeofence(gid: GID): ConnectionIO[Option[Geofence]] =
    Geofences.get(gid).option

  def getAllGeofences(): ConnectionIO[List[Geofence]] =
    Geofences.getAll.list

  def region(gid: GID): ConnectionIO[Option[Region]] =
    Geofences.getRegion(gid).option
}

object DeviceRepoDoobie extends DeviceRepo[ConnectionIO]{

  def at(did: DID, pos: Position, time: Timestamp): ConnectionIO[Unit] =
    Devices.at(did,pos,time).run.as(())

  def removeDevice(did: DID): ConnectionIO[Unit] =
    Devices.remove(did).run.as(())

  def getDevice(did: DID): ConnectionIO[Option[Device]] =
    Devices.get(did).option

  def getPos(did: DID): ConnectionIO[Option[Position]] =
    Devices.getPos(did).option

  def getTime(did: DID): ConnectionIO[Option[Timestamp]] =
    Devices.getTime(did).option
}

object WhereInRepoDoobie extends WhereInRepo[ConnectionIO]{

  def enter(gid: GID, did: DID, time: Timestamp): ConnectionIO[Unit] =
    WhereIn.enter(gid,did,time).run.as(())

  def exit(gid: GID, did: DID): ConnectionIO[Unit] =
    WhereIn.exit(gid,did).run.as(())

  def isIn(gid: GID, did: DID): ConnectionIO[Boolean] =
    WhereIn.isIn(gid,did).option.map(_.isDefined)

  def getTime(gid: GID, did: DID): ConnectionIO[Option[Timestamp]] =
    WhereIn.getTime(gid,did).option
}

