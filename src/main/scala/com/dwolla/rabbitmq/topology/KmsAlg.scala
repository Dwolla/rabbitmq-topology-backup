package com.dwolla.rabbitmq.topology

import java.util.concurrent._

import io.circe.Decoder
import cats.effect._
import cats.implicits._
import software.amazon.awssdk.services.kms._
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.model.DecryptRequest
import software.amazon.awssdk.utils.BinaryUtils

import scala.util.Try

trait KmsAlg[F[_]] {
  def decrypt(string: String): F[String]
}

object KmsAlg {
  private def acquireKmsClient[F[_] : Sync]: F[KmsAsyncClient] =
    Sync[F].delay(KmsAsyncClient.builder().build())

  private def releaseKmsClient[F[_] : Sync](client: KmsAsyncClient): F[Unit] =
    Sync[F].delay(client.close())

  def resource[F[_] : ConcurrentEffect]: Resource[F, KmsAlg[F]] =
    for {
      client <- Resource.make(acquireKmsClient[F])(releaseKmsClient[F])
    } yield new KmsAlg[F] {
      private def buildRequest(ciphertextBlob: SdkBytes) =
        DecryptRequest.builder().ciphertextBlob(ciphertextBlob).build()

      override def decrypt(string: String): F[String] =
        for {
          ciphertextBlob <- Try(SdkBytes.fromByteArray(BinaryUtils.fromBase64(string))).liftTo[F]
          resp <- AwsEval.eval[F](buildRequest(ciphertextBlob))(client.decrypt)(_.plaintext().asUtf8String())
        } yield resp

    }

  implicit def providedKmsEncryptedValueDecoder[F[_]](implicit kmsAlg: KmsAlg[F]): Decoder[F[String]] =
    Decoder[String].map(kmsAlg.decrypt)
}

object AwsEval {
  class PartiallyAppliedEvalF[F[_]] {
    def apply[Req, Res, O](req: => Req)
                          (client: Req => CompletableFuture[Res])
                          (extractor: Res => O)
                          (implicit ev: ConcurrentEffect[F]): F[O] =
      cfToF[F](client(req)).map(extractor)
  }

  def eval[F[_]] = new PartiallyAppliedEvalF[F]

  private class PartialCompletableFutureToF[F[_]] {
    def apply[A](makeCf: => CompletableFuture[A])
                (implicit ev: ConcurrentEffect[F]): F[A] =
      Concurrent.cancelableF[F, A] { cb =>
        val cf = makeCf
        cf.handle[Unit]((result: A, err: Throwable) => {
          err match {
            case null =>
              cb(Right(result))
            case _: CancellationException =>
              ()
            case ex: CompletionException if ex.getCause ne null =>
              cb(Left(ex.getCause))
            case ex =>
              cb(Left(ex))
          }
        })

        val cancelToken: CancelToken[F] = Sync[F].delay(cf.cancel(true)).void
        cancelToken.pure[F]
      }

  }

  private def cfToF[F[_]] = new PartialCompletableFutureToF[F]

}
