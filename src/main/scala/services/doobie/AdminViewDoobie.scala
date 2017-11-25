package org.hablapps.geofences
package services
package doobieImpl

import scalaz._, Scalaz._
import scala.util.Try
import doobie.imports._
import ViewData._, java.sql.Timestamp
import data.doobieImpl.Schemas._

object DoobieAdminView extends AdminView[ConnectionIO]{

  def add(reg: Region): ConnectionIO[GID] =
    Geofences.add(reg).withUniqueGeneratedKeys("gid")

  def remove(gid: GID): ConnectionIO[Unit] =
    Geofences.remove(gid).run.flatMap{ count =>
      if (count == 0) doobie.free.connection.fail(System.Error.nonExisting(gid))
      else ().point[ConnectionIO]
    }

  def getAll(): ConnectionIO[List[Geofence]] =
    Geofences.getAll.list

  def get(id: GID): ConnectionIO[Option[Geofence]] =
    Geofences.get(id).option

}
