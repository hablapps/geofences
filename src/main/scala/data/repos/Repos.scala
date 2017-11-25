package org.hablapps.geofences
package data
package repos

import services._, ViewData._, java.sql.Timestamp

trait NetworkRepo[P[_]]{

  val GeofenceRepo: GeofenceRepo[P]
  val DeviceRepo: DeviceRepo[P]
  val WhereInRepo: WhereInRepo[P]

  // Init & Dispose

  def init: P[Unit]

  def dispose: P[Unit]
}

trait GeofenceRepo[P[_]]{
  def addGeofence(region: Region): P[GID]
  def removeGeofence(gid: GID): P[Unit]
  def getGeofence(gid: GID): P[Option[Geofence]]
  def getAllGeofences(): P[List[Geofence]]
  def region(gid: GID): P[Option[Region]]
}

trait DeviceRepo[P[_]]{
  def at(did: DID, pos: Position, time: Timestamp): P[Unit]
  def removeDevice(did: DID): P[Unit]
  def getDevice(did: DID): P[Option[Device]]
  def getPos(did: DID): P[Option[Position]]
  def getTime(did: DID): P[Option[Timestamp]]
}

trait WhereInRepo[P[_]]{
  def enter(gid: GID, did: DID, time: Timestamp): P[Unit]
  def exit(gid: GID, did: DID): P[Unit]
  def isIn(gid: GID, did: DID): P[Boolean]
  def getTime(gid: GID, did: DID): P[Option[Timestamp]]
}
