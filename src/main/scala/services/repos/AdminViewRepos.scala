package org.hablapps
package geofences
package services
package repos

import ViewData._, java.sql.Timestamp
import geofences.data.repos._

case class AdminViewRepos[P[_]](repos: NetworkRepo[P]) extends AdminView[P]{
  
  def add(reg: Region): P[GID] = 
    repos.GeofenceRepo.addGeofence(reg)

  def remove(gid: GID): P[Unit] = 
    repos.GeofenceRepo.removeGeofence(gid)

  def getAll(): P[List[Geofence]] = 
    repos.GeofenceRepo.getAllGeofences()

  def get(gid: GID): P[Option[Geofence]] = 
    repos.GeofenceRepo.getGeofence(gid)
}
