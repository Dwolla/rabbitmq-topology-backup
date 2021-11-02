package natchez.xray

import cats.data._
import cats.syntax.all._
import cats.effect._
import com.comcast.ip4s.{IpAddress, SocketAddress}
import natchez.Kernel

trait XRayEnvironment[F[_]] {
  def daemonAddress: F[Option[SocketAddress[IpAddress]]]
  def traceId: F[Option[String]]
  def kernelFromEnvironment: F[Kernel]
}

object XRayEnvironment {
  def apply[F[_] : XRayEnvironment]: XRayEnvironment[F] = implicitly

  implicit def instance[F[_] : Sync]: XRayEnvironment[F] = new XRayEnvironment[F] {
    override def daemonAddress: F[Option[SocketAddress[IpAddress]]] =
      OptionT(Sync[F].delay(sys.env.get("AWS_XRAY_DAEMON_ADDRESS")))
        .subflatMap(SocketAddress.fromStringIp)
        .value

    override def traceId: F[Option[String]] =
      Sync[F].delay(sys.env.get("_X_AMZN_TRACE_ID"))

    override def kernelFromEnvironment: F[Kernel] =
      OptionT(traceId)
        .map("X-Amzn-Trace-Id" -> _)
        .map(Map(_))
        .getOrElse(Map.empty)
        .map(Kernel)
  }
}
