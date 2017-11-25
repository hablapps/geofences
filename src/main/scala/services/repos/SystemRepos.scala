package org.hablapps
package geofences
package services
package repos

import scalaz._, Scalaz._
import geofences.data.repos._
import ViewData._, java.sql.Timestamp

class SystemRepos[P[_]: Monad](network: NetworkRepo[P]) extends System[P]{

  val AdminView = AdminViewRepos(network)
  val DeviceView = DeviceViewRepos(network)

  def init: P[Unit] = network.init
  def dispose: P[Unit] = network.dispose
}
