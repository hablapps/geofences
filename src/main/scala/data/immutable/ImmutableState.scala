package org.hablapps.geofences
package data
package immutable

import services.ViewData._, java.sql.Timestamp

case class Network(
  gid: Int,
  geofences: Map[GID,Geofence],
  devices: Map[DID,Device],
  whereIn: Map[(GID,DID),Timestamp]
)
