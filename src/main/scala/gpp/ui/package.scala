package gpp

import cats.effect.IO
import japgolly.scalajs.react.{ Callback, CallbackTo }

package ui {
  trait RunCB[F[_]] {
    def runInCB(f: F[Unit]): Callback
  }
}

package object ui {
  implicit object RunIO extends RunCB[IO] {
    def runInCB(f: IO[Unit]): Callback =
      Callback(f.unsafeRunAsyncAndForget())
  }

  implicit object RunCB extends RunCB[CallbackTo] {
    def runInCB(f: CallbackTo[Unit]): Callback = f
  }
}
