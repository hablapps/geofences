package org.hablapps
package geofences
package services
package repos

import scalaz._, Scalaz._
import ViewData._, java.sql.Timestamp
import geofences.data.repos._

case class DeviceViewRepos[P[_]: Monad](repos: NetworkRepo[P]) extends DeviceView[P]{

  def geoChange(did: DID, pos: Position, time: Timestamp): Geofence => P[List[GeoEvent]] = 
    geofence => for {
      wasIn <- repos.WhereInRepo.isIn(geofence.gid,did)
      isIn = Geofence.isIn(geofence)(pos)
    } yield if (wasIn && !isIn) List(Exit(geofence.gid,did,time))
            else if (!wasIn && isIn) List(Enter(geofence.gid,did,time))
            else List()

  def whatWillHappen(did: DID, pos: Position, time: Timestamp): P[List[GeoEvent]] =
    repos.GeofenceRepo.getAllGeofences().flatMap{
      _.traverseU(geoChange(did,pos,time))
    }.map(_.flatten)

  def at(did: DID, pos: Position, time: Timestamp): P[List[GeoEvent]] =
    whatWillHappen(did,pos,time) >>! { events =>
      events.traverse{
        case Exit(gid,did,_) => repos.WhereInRepo.exit(gid,did)
        case Enter(gid,did,t) => repos.WhereInRepo.enter(gid,did,t)
      } >>
      repos.DeviceRepo.at(did,pos,time)
    }

  def get(did: DID): P[Option[Device]] = 
    (repos.DeviceRepo.getPos(did) |@| repos.DeviceRepo.getTime(did)){
      (pos: Option[Position], time: Option[Timestamp]) => 
        (pos |@| time)(Device(did,_,_))
    }
}
