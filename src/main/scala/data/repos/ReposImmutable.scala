package org.hablapps.geofences
package data
package repos

import scalaz._, Scalaz._

import services._, services.ViewData._, java.sql.Timestamp

import data.immutable._
import puretest.RaiseError

class NetworkRepoImmutable[P[_]](implicit
  M: MonadState[P,Network],
  RE: RaiseError[P,System.Error])
extends NetworkRepo[P]{
  import M._

  val GeofenceRepo = new GeofenceRepoImmutable[P]
  val DeviceRepo = new DeviceRepoImmutable[P]
  val WhereInRepo = new WhereInRepoImmutable[P]

  def init: P[Unit] = put(Network(1,Map(),Map(),Map()))
  def dispose: P[Unit] = ().point[P]
}

object NetworkRepoImmutable{
  def apply[P[_]: MonadState[?[_],Network]: RaiseError[?[_],System.Error]] = new NetworkRepoImmutable[P]
}

class GeofenceRepoImmutable[P[_]](implicit
  M: MonadState[P,Network],
  RE: RaiseError[P,System.Error])
extends GeofenceRepo[P]{
  import M._

  def addGeofence(region: Region): P[GID] =
    gets(_.gid) >>! { gid =>
      modify{
        case Network(num, geofences, devices, whereIn) =>
          Network(num+1, geofences + (num -> Geofence(num, region)), devices, whereIn)
      }
    }

  def removeGeofence(gid: GID): P[Unit] =
    getGeofence(gid) >>= {
      _.fold(RE.raiseError[Unit](System.Error.nonExisting(gid))){
        _ => modify{
          state => state.copy(geofences = state.geofences - gid)
        }
      }
    }

  def getGeofence(gid: GID): P[Option[Geofence]] =
    gets(_.geofences.get(gid))

  def getAllGeofences(): P[List[Geofence]] =
    gets(_.geofences.values.toList)

  def region(gid: GID): P[Option[Region]] =
    getGeofence(gid) map (_ map (_.region))
}

class DeviceRepoImmutable[P[_]](implicit
  M: MonadState[P,Network])
extends DeviceRepo[P]{
  import M._

  def at(did: DID, pos: Position, time: Timestamp): P[Unit] =
    modify{
      case Network(gid,geofences,devices, whereIn) =>
        val updatedWhereIn = geofences.foldLeft(whereIn){
          case (wIn, (gid,geofence)) =>
            val isIn = Geofence.isIn(geofence)(pos)
            val wasIn = wIn.contains((gid,did))
            if (!wasIn && isIn) wIn + ((gid,did)->time)
            else if (wasIn && !isIn) wIn - ((gid,did))
            else wIn
        }
        Network(gid,geofences,devices+(did -> Device(did,pos,time)), updatedWhereIn)
    }

  def removeDevice(did: DID): P[Unit] =
    modify{
      case Network(gid,geofences,devices, whereIn) =>
        Network(gid,geofences,devices-did, whereIn)
    }

  def getDevice(did: DID): P[Option[Device]] =
    gets(_.devices.get(did))

  def getPos(did: DID): P[Option[Position]] =
    getDevice(did) map (_ map (_.pos))

  def getTime(did: DID): P[Option[Timestamp]] =
    getDevice(did) map (_ map (_.time))
}

class WhereInRepoImmutable[P[_]](implicit
  M: MonadState[P,Network])
extends WhereInRepo[P]{
  import M._

  // Dummie impl., done through `at` command
  def enter(gid: GID, did: DID, time: Timestamp): P[Unit] =
    ().point[P]

  def exit(gid: GID, did: DID): P[Unit] =
    ().point[P]

  def isIn(gid: GID, did: DID): P[Boolean] =
    gets{ _.whereIn.contains((gid,did)) }

  def getTime(gid: GID, did: DID): P[Option[Timestamp]] =
    MonadState[P,Network].gets{ _.whereIn.get((gid,did)) }
}

