// Copyright (c) 2019-2020 by Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

// adapted from https://github.com/tpolecat/natchez/pull/427

package natchez
package xray

import natchez.EntryPoint
import cats.effect._
import java.net.DatagramSocket

object XRay {

  def entryPoint[F[_]: Sync : Timer](
                              host: String = "localhost",
                              port: Int = 2000
                            ): Resource[F, EntryPoint[F]] =
    Resource
      .make(Sync[F].delay(new DatagramSocket()))(x => Sync[F].delay(x.close()))
      .map { socket =>
        new XRayEntryPoint[F](socket, host, port)
      }
}
