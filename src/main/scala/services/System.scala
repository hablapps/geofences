package org.hablapps.geofences
package services

import scalaz._, Scalaz._
import ViewData._, java.sql.Timestamp

trait System[P[_]]{
  val AdminView: AdminView[P]
  val DeviceView: DeviceView[P]

  def init: P[Unit]
  def dispose: P[Unit]

  def init(g: Region*)(at: (DID,Position,Timestamp)*)(implicit M: Monad[P]): P[List[GID]] =
    g.toList.traverse(AdminView.add) >>! { _ =>
      at.toList.traverse{
        case (did,pos,time) => DeviceView.at(did,pos,time)
      }
    }

  def run[T](p: => P[T])(implicit M: Monad[P]): P[T] =
    init >> p >>! { _ => dispose}
}

object System{

  sealed abstract class Error extends Throwable
  case class AdminError(error: AdminView.Error) extends Error

  object Error{
    def nonExisting(gid: GID): Error =
      AdminError(AdminView.NonExisting(gid))
  }

  // Boilerplate
  def fromNatTrans[P[_],Q[_]](nat: Q ~> P)(implicit Q: System[Q]) = new System[P]{

    val AdminView = new AdminView[P]{
      def add(reg: Region): P[GID] = nat(Q.AdminView.add(reg))
      def remove(gid: GID): P[Unit] = nat(Q.AdminView.remove(gid))
      def getAll(): P[List[Geofence]] = nat(Q.AdminView.getAll())
      def get(gid: GID): P[Option[Geofence]] = nat(Q.AdminView.get(gid))
    }

    val DeviceView = new DeviceView[P]{
      def at(did: DID, pos: Position, time: Timestamp): P[List[GeoEvent]] = nat(Q.DeviceView.at(did,pos,time))
      def get(did: DID): P[Option[Device]] = nat(Q.DeviceView.get(did))
    }

    def init: P[Unit] = nat(Q.init)
    def dispose: P[Unit] = nat(Q.dispose)
  }

}
