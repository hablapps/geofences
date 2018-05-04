package org.hablapps
package geofences
package data
package doobieImpl

import scalaz._, Scalaz._
import doobie.imports._
import org.postgresql.geometric._
import doobie.postgres.imports._
import doobie.postgres.pgistypes._

import geofences.services.ViewData._, java.sql.Timestamp

// psql -c 'create database geofences;' -U postgres
// psql -c '\i geofences-schemas.sql' -d geofences -U postgres

object Schemas{

  val dropTables: ConnectionIO[Unit] =
    Geofences.dropTable.run *>
    Devices.dropTable.run *>
    WhereIn.dropTable.run.as(())

  val setUpTables: ConnectionIO[Unit] =
    dropTables *>
    Geofences.createTable.run *>
    Devices.createTable.run *>
    WhereIn.createTable.run.as(())

  // Parameters

  object MetaImplicits{
    implicit val RegionType: Meta[Region] =
      Meta[PGcircle].xmap(
        { case circle => Region((circle.center.x,circle.center.y),circle.radius) },
        { case Region((x,y),r) => new PGcircle(new PGpoint(x,y),r) })

    implicit val PositionType: Meta[Position] =
      Meta[PGpoint].xmap(
        { case point => (point.x, point.y) },
        { case (x,y) => new PGpoint(x,y) }
      )
  }

  import MetaImplicits._

  // Geofence table

  object Geofences{
    val createTable: Update0 = sql"""
      CREATE TABLE IF NOT EXISTS geofences (
        gid SERIAL PRIMARY KEY,
        region CIRCLE NOT NULL
    );""".update


    val dropTable: Update0 = sql"""
      DROP TABLE IF EXISTS geofences CASCADE
    """.update

    def add: Geofence => Update0 = {
      case Geofence(gid, circle) => sql"""
        INSERT INTO geofences VALUES ($gid,$circle)
      """.update
    }

    def add(region: Region): Update0 = sql"""
      INSERT INTO geofences (region) VALUES ($region)
    """.update

    def insertOrUpdate: Geofence => Update0 = {
      case Geofence(gid, circle) => sql"""
        INSERT INTO geofences VALUES ($gid,$circle)
        ON CONFLICT (gid) DO
        UPDATE SET
          region = excluded.region;
      """.update
    }

    def remove(gid: GID): Update0 =
      sql"DELETE FROM geofences WHERE gid = $gid".update

    def getAll: Query0[Geofence] = sql"""
      SELECT * from geofences
    """.query[Geofence]

    def get(gid: GID): Query0[Geofence] = sql"""
      SELECT * from geofences WHERE gid = $gid
    """.query[Geofence]

    def getRegion(gid: GID): Query0[Region] = sql"""
      SELECT region FROM geofences WHERE gid = $gid".query[Region].option
    """.query[Region]
  }

  // WhereIn table

  object WhereIn{
    val createTable: Update0 = sql"""
      CREATE TABLE IF NOT EXISTS wherein (
          gid integer NOT NULL,
          did integer NOT NULL,
          time timestamp NOT NULL,
          PRIMARY KEY (gid,did)
      );""".update

    val dropTable: Update0 = sql"""
      DROP TABLE IF EXISTS wherein
    """.update

    def enter(gid: GID, did: DID, time: Timestamp): Update0 =
      sql"INSERT INTO wherein VALUES ($gid,$did,$time)".update

    def exit(gid: GID, did: DID): Update0 =
      sql"DELETE FROM wherein WHERE gid = $gid and did = $did".update

    def in(did: DID): Query0[GID] =
      sql"SELECT gid FROM wherein WHERE did = $did".query

    def getTime(gid: GID, did: DID): Query0[Timestamp] =
      sql"SELECT time FROM wherein WHERE gid = $gid and did = $did".query

    def isIn(gid: Int, did: Int): Query0[(GID,DID)] = sql"""
      SELECT gid, did FROM wherein
        WHERE gid=$gid AND did=$did
    """.query

    /** Additional for stateless */

    def insertOrUpdate: ((GID,DID),Timestamp) => Update0 = {
      case ((gid,did), time) => sql"""
        INSERT INTO wherein VALUES ($gid,$did,$time)
        ON CONFLICT (gid,did) DO
        UPDATE SET
          time = excluded.time;
      """.update
    }

    def getAll: Query0[((GID,DID),Timestamp)] = sql"""
      SELECT * from wherein
    """.query[((GID,DID),Timestamp)]

    def getIndices: Query0[(GID,DID)] = sql"""
      SELECT gid, did from wherein
    """.query[(GID,DID)]


  }

  // Devices table

  object Devices{
    val createTable: Update0 = sql"""
      CREATE TABLE IF NOT EXISTS devices (
          did integer NOT NULL PRIMARY KEY,
          position point NOT NULL,
          time timestamp NOT NULL
      );""".update

    val dropTable: Update0 = sql"""
      DROP TABLE IF EXISTS devices
    """.update

    def at(did: DID, pos: Position, time: Timestamp): Update0 = sql"""
      INSERT INTO devices VALUES ($did,$pos,$time)
      ON CONFLICT (did) DO
      UPDATE SET
        position = excluded.position,
        time = excluded.time ;
    """.update

    def insertOrUpdate(device: Device): Update0 =
      at(device.did, device.pos, device.time)

    def get(did: DID): Query0[Device] = sql"""
      SELECT * from devices WHERE did = $did
    """.query[Device]

    def getPos(did: DID): Query0[Position] = sql"""
      SELECT position from devices WHERE did = $did
    """.query[Position]

    def getTime(did: DID): Query0[Timestamp] = sql"""
      SELECT time from devices WHERE did = $did
    """.query[Timestamp]

    def getAll: Query0[Device] = sql"""
      SELECT * from devices
    """.query[Device]

    def remove(did: DID): Update0 =
      sql"DELETE FROM devices WHERE did = $did".update
  }

  // New position for DID

  def enterOrExitFrom(did: DID, pos: Position): Query0[(GID,DID,Boolean)] =
    sql"""
      select gid, did, isin from
        (select gid, did, isin, (time is not null) as wasin from
          (select gid, $did as did, $pos <@ region as isin from geofences) as foo1 left join
           wherein using (gid,did)) foo2
      where (isin and not wasIn) or (not isin and wasIn);
    """.query[(GID,DID,Boolean)]

}