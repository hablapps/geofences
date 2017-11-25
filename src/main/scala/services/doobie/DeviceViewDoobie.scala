package org.hablapps.geofences
package services
package doobieImpl

import scalaz._, Scalaz._
import doobie.imports._
import ViewData._, java.sql.Timestamp
import data.doobieImpl.Schemas._

object DoobieDeviceView extends DeviceView[ConnectionIO]{
  import MetaImplicits._

  def whatHappened(did: DID, pos: Position, time: Timestamp): ConnectionIO[List[GeoEvent]] =
    enterOrExitFrom(did,pos).list.map{
      _.map{
        case (gid,did,enter) =>
          if (enter) Enter(gid,did,time)
          else Exit(gid,did,time)
      }
    }

  def whatHappened2(did: DID, pos: Position, time: Timestamp): ConnectionIO[List[GeoEvent]] =
    Geofences.getAll.list.flatMap{
      _.traverseU{ geofence => for {
          wasIn <- WhereIn.isIn(geofence.gid,did).option.map(_.isDefined)
          isIn = Geofence.isIn(geofence)(pos)
        } yield if (wasIn && !isIn) List(Exit(geofence.gid,did,time))
                else if (!wasIn && isIn) List(Enter(geofence.gid,did,time))
                else List()
      }.map(_.flatten)
    }

  def at(did: DID, pos: Position, time: Timestamp): ConnectionIO[List[GeoEvent]] =
    whatHappened(did,pos,time) >>! { events =>
      events.traverse{
        case Exit(gid,did,_) => WhereIn.exit(gid,did).run
        case Enter(gid,did,t) => WhereIn.enter(gid,did,t).run
      } >>
      Devices.at(did,pos,time).run
    }

  def get(did: DID): ConnectionIO[Option[Device]] =
    Devices.get(did).option


}