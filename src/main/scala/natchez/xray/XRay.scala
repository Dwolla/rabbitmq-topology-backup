// Copyright (c) 2019-2020 by Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

// adapted from https://github.com/tpolecat/natchez/pull/427

package natchez
package xray

import natchez.EntryPoint
import cats.effect.{Resource, Sync}
import com.comcast.ip4s._
import fs2.io.net.Network

object XRay {

  def entryPoint[F[_]: Sync: Network](
                                       daemonAddress: SocketAddress[IpAddress] =
                                       SocketAddress(ip"127.0.0.1", port"2000")
                                     ): Resource[F, EntryPoint[F]] =
    Network[F]
      .openDatagramSocket()
      .map { socket =>
        new XRayEntryPoint[F](socket, daemonAddress)
      }
}
