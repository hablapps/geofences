package org.hablapps.geofences
package services

import ViewData._, java.sql.Timestamp

trait AdminView[P[_]]{

  def add(reg: Region): P[GID]

  def remove(gid: GID): P[Unit]

  def getAll(): P[List[Geofence]]

  def get(gid: GID): P[Option[Geofence]]
}

object AdminView{

  sealed abstract class Error
  case class NonExisting(gid: GID) extends Error

}
