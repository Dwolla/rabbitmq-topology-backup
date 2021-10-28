// Copyright (c) 2019-2020 by Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

// adapted from https://github.com/tpolecat/natchez/pull/427

package natchez
package xray

import java.net.DatagramSocket
import cats.effect._
import cats.syntax.all._
import java.net.InetAddress
import io.circe._
import io.circe.syntax._
import java.net.DatagramPacket

final class XRayEntryPoint[F[_]: Sync : Timer](
                                        socket: DatagramSocket,
                                        host: String,
                                        port: Int
                                      ) extends EntryPoint[F] {

  val addr = InetAddress.getByName(host)

  def sendSegment(foo: JsonObject): F[Unit] = {
    val payload = (XRayEntryPoint.header + foo.asJson.noSpaces).getBytes()
    val packet = new DatagramPacket(payload, payload.length, addr, port)
    Sync[F].delay(socket.send(packet))
  }

  def make(span: F[XRaySpan[F]]): Resource[F, Span[F]] =
    Resource.makeCase(span)(XRaySpan.finish(_, this, _)).widen

  def root(name: String): Resource[F, Span[F]] =
    make(XRaySpan.root(name, this))

  def continue(name: String, kernel: Kernel): Resource[F, Span[F]] =
    make(XRaySpan.fromKernel(name, kernel, this))

  def continueOrElseRoot(name: String, kernel: Kernel): Resource[F, Span[F]] =
    make(XRaySpan.fromKernelOrElseRoot(name, kernel, this))
}

object XRayEntryPoint {
  val header = "{\"format\": \"json\", \"version\": 1}\n"
}
