package org.hablapps.geofences

import services.http4sImpl._

import cats.effect.{Effect, IO}
import org.http4s._

import scalaz._, Scalaz._
import services.ViewData._, data.immutable._

import services._, services.repos._, data.repos._
import puretest.{RaiseError, HandleError}

object HttpNetworkServerVars extends HttpNetworkService.App[IO](
  IOSystem.apply)(
  implicitly,
  IOSystem.HandleSystemError,
  scala.concurrent.ExecutionContext.global
)

object IOSystem{

  // Handling system errors from IO programs

  object HandleSystemError extends HandleError[IO,System.Error]{
    def handleError[A](p: IO[A])(f: System.Error => IO[A]): IO[A] =
      Effect[IO].handleErrorWith(p){
        case serror: System.Error => f(serror)
        case other => IO.raiseError(other)
      }
  }

  // Underlying state-based programs

  type StateProgram[T] = StateT[Either[System.Error,?],Network,T]

  implicit val RE = new RaiseError[StateProgram, System.Error]{
    def raiseError[A](e: System.Error) =
      IndexedStateT(_ => Left(e): Either[System.Error, (Network,A)])
  }

  val stateRepoSystem: System[StateProgram] =
    new SystemRepos(new NetworkRepoImmutable[StateProgram])

  // IO programs

  object NatVarIO extends (StateProgram ~> IO){

    var current: Network = Network(1,Map(),Map(),Map())

    def apply[T](program: StateProgram[T]): IO[T] =
      program(current).fold(
        IO.raiseError,
        { case (newState,out) => current = newState; IO(out) }
      )
  }

  val apply: System[IO] =
    System.fromNatTrans(NatVarIO)(stateRepoSystem)
}
