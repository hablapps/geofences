package org.hablapps.geofences
package services
package doobieImpl

import doobie.imports._
import data.doobieImpl.Schemas._

object DoobieSystem extends System[ConnectionIO]{
  def init = setUpTables
  def dispose = dropTables

  val AdminView = DoobieAdminView
  val DeviceView = DoobieDeviceView
}
