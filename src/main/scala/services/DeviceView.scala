package org.hablapps.geofences
package services

import ViewData._, java.sql.Timestamp

trait DeviceView[P[_]]{

  def at(did: DID, pos: Position, time: Timestamp): P[List[GeoEvent]]

  def get(did: DID): P[Option[Device]]
}
