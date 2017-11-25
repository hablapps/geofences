package org.hablapps.geofences
package services

import java.sql.Timestamp

object ViewData{

  /**
   * Geofences
   */

  type GID = Int // geofence id
  type Position = (Double, Double)
  case class Region(pos: Position, radius: Double)

  case class Geofence(gid: Int, region: Region)

  object Geofence{
    def isIn(reg: Geofence): Position => Boolean = {
      case (x2,y2) => reg match {
        case Geofence(_, Region((x, y),r)) =>
          (Math.abs(x - x2) <= r) && (Math.abs(y - y2) <= r)
      }
    }
  }

  /**
   * Devices
   */

  type DID = Int

  case class Device(did: Int, pos: Position, time: Timestamp)

  /**
   * Geofence events
   */

  sealed abstract class GeoEvent
  case class Enter(gid: GID, did: DID, time: Timestamp) extends GeoEvent
  case class Exit(gid: GID, did: DID, time: Timestamp)  extends GeoEvent

}