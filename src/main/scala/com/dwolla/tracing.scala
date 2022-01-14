package com.dwolla

import cats._
import cats.data.Kleisli
import cats.effect.{Trace => _, _}
import cats.tagless.aop._
import cats.tagless.syntax.all._
import natchez._
import natchez.http4s.NatchezMiddleware
import org.http4s.client.Client
import scala.annotation.nowarn

package object tracing {
  @nowarn("msg=parameter value evidence\\$1 in method toWithTracingSyntax is never used")
  implicit def toWithTracingSyntax[Alg[_[_]], F[_] : Trace](f: Alg[F]): WithTracingSyntax[Alg, F] =
    new WithTracingSyntax(f)

  implicit def toWithTracingSyntaxForKleisli[Alg[_[_]], F[_]](f: Alg[F])(implicit @nowarn NT: Not[Trace[F]]): WithTracingSyntaxForKleisli[Alg, F] =
    new WithTracingSyntaxForKleisli(f)

  implicit def toWithTracingSyntaxForClient[F[_]](f: Client[F]): WithTracingSyntaxForClient[F] =
    new WithTracingSyntaxForClient(f)

  private[tracing] def toTraceFunctionK[F[_] : Trace]: Instrumentation[F, *] ~> F = new (Instrumentation[F, *] ~> F) {
    override def apply[A](fa: Instrumentation[F, A]): F[A] = Trace[F].span(s"${fa.algebraName}.${fa.methodName}")(fa.value)
  }
}

package tracing {
  class WithTracingSyntax[Alg[_[_]], F[_]](private val alg: Alg[F]) extends AnyVal {
    def withTracing(implicit
                    T: Trace[F],
                    I: Instrument[Alg]): Alg[F] =
      Instrument[Alg].instrument(alg).mapK(toTraceFunctionK)
  }

  class WithTracingSyntaxForKleisli[Alg[_[_]], F[_]](private val alg: Alg[F]) extends AnyVal {
    def withTracing(implicit
                    F: MonadCancelThrow[F],
                    I: Instrument[Alg]): Alg[Kleisli[F, Span[F], *]] =
      alg.mapK(Kleisli.liftK[F, Span[F]]).instrument.mapK(toTraceFunctionK)
  }

  class WithTracingSyntaxForClient[F[_]](private val client: Client[F]) extends AnyVal {
    def tracedWith(span: Span[F])
                  (implicit F: Async[F]): Client[Kleisli[F, Span[F], *]] =
      NatchezMiddleware.client(client.translate(Kleisli.liftK[F, Span[F]])(Kleisli.applyK(span)))
  }

  sealed trait Not[A] {
    override def toString: String = "Not"
  }

  object Not {
    implicit def makeNot[A]: Not[A] = null.asInstanceOf[Not[A]]
    implicit def makeNotAmbig[A](implicit @nowarn a: A): Not[A] = sys.error("this method should never be called")
  }
}
