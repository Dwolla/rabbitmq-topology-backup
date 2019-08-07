package com.dwolla.rabbitmq.topology

import cats._
import cats.effect._
import org.http4s._
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import io.circe.syntax._
import io.circe._

package object model {
  import io.circe.generic.extras.Configuration
  private[model] implicit def config: Configuration = Configuration.default.withSnakeCaseConstructorNames.withSnakeCaseMemberNames
}


package model {

  case class RabbitMqTopology(rabbitVersion: String,
                              users: List[RabbitUser],
                              vhosts: List[VHost],
                              permissions: List[RabbitPermission],
                              parameters: List[RabbitParameter],
                              globalParameters: List[RabbitParameter],
                              policies: List[RabbitPolicies],
                              queues: List[RabbitQueue],
                              exchanges: List[RabbitExchange],
                              bindings: List[RabbitBinding],
                             )

  object RabbitMqTopology {
    implicit def codec[F[_]]: Codec.AsObject[RabbitMqTopology] = deriveCodec[RabbitMqTopology]
    implicit def entityDecoder[F[_] : Sync]: EntityDecoder[F, RabbitMqTopology] = jsonOf[F, RabbitMqTopology]
    implicit def entityEncoder[F[_] : Applicative]: EntityEncoder[F, RabbitMqTopology] = EntityEncoder[F, Json].contramap(_.asJson)
  }

  case class RabbitUser(name: String,
                        passwordHash: String,
                        hashingAlgorithm: String,
                        tags: String,
                       )

  object RabbitUser {
    implicit val rabbitUserCodec: Codec[RabbitUser] = deriveCodec
  }

  case class VHost(name: String)

  object VHost {
    implicit val vHostCodec: Codec[VHost] = deriveCodec
  }

  case class RabbitPermission(user: String,
                              vhost: String,
                              configure: String,
                              write: String,
                              read: String,
                             )

  object RabbitPermission {
    implicit val rabbitPermissionCodec: Codec[RabbitPermission] = deriveCodec
  }

  case class RabbitParameter(name: String,
                             value: String,
                            )

  object RabbitParameter {
    implicit val rabbitParameterCodec: Codec[RabbitParameter] = deriveCodec
  }

  /**
    * {
    *   "vhost": "/",
    *   "name": "ha-all-queues",
    *   "pattern": ".*",
    *   "apply-to": "all",
    *   "definition": {
    *     "ha-mode": "exactly",
    *     "ha-params": 3
    *   },
    *   "priority": 100
    * },
    */
  case class RabbitPolicies(vhost: String,
                            name: String,
                            pattern: String,
                            `apply-to`: String,
                            definition: Json,
                            priority: Int,
                           )

  object RabbitPolicies {
    implicit val rabbitPoliciesCodec: Codec[RabbitPolicies] = deriveCodec
  }

  /**
   *  {
   *    "name": "email-service.delay.30000",
   *    "vhost": "/",
   *    "durable": true,
   *    "auto_delete": false,
   *    "arguments": {
   *      "x-dead-letter-exchange": "email-service.work"
   *    }
   *  },
   */
  case class RabbitQueue(name: String,
                         vhost: String,
                         durable: Boolean,
                         autoDelete: Boolean,
                         arguments: Json,
                        )

  object RabbitQueue {
    implicit val rabbitQueueCodec: Codec[RabbitQueue] = deriveCodec
  }

  /**
   * {{{
   * {
   *   "source": "access-control.delay",
   *   "vhost": "/",
   *   "destination": "access-control.delay",
   *   "destination_type": "queue",
   *   "routing_key": "",
   *   "arguments": {}
   * }
   * }}}
   */
  case class RabbitBinding(source: String,
                           vhost: String,
                           destination: String,
                           destinationType: String,
                           routingKey: String,
                           arguments: Json,
                          )

  object RabbitBinding {
    implicit val rabbitBindingCodec: Codec[RabbitBinding] = deriveCodec
  }

  /**
   * {{{
   *   {
   *     "name": "notes.error",
   *     "vhost": "/",
   *     "type": "fanout",
   *     "durable": true,
   *     "auto_delete": false,
   *     "internal": false,
   *     "arguments": {}
   *   }
   * }}}
   */
  case class RabbitExchange(name: String,
                            vhost: String,
                            `type`: String,
                            durable: Boolean,
                            autoDelete: Boolean,
                            internal: Boolean,
                            arguments: Json,
                           )

  object RabbitExchange {
    implicit val rabbitExchangeCodec: Codec[RabbitExchange] = deriveCodec
  }

}

/**
{
  "rabbit_version": "3.6.16",
  "users": [
    {
      "name": "guest",
      "password_hash": "Qt6Tf9g+AzBETWSigqpTpf/Sk6qpwr8QAkIz06MkQY6S0iUp",
      "hashing_algorithm": "rabbit_password_hashing_sha256",
      "tags": "administrator"
    }
  ],
  "vhosts": [
    {
      "name": "/"
    }
  ],
  "permissions": [
    {
      "user": "guest",
      "vhost": "/",
      "configure": ".*",
      "write": ".*",
      "read": ".*"
    }
  ],
  "parameters": [],
  "global_parameters": [
    {
      "name": "cluster_name",
      "value": "rabbit@rabbit3.91.us-west-2.prod.dwolla.net"
    }
  ],
  "policies": [
    {
      "vhost": "/",
      "name": "ha-all-queues",
      "pattern": ".*",
      "apply-to": "all",
      "definition": {
        "ha-mode": "exactly",
        "ha-params": 3
      },
      "priority": 100
    },
    {
      "vhost": "/",
      "name": "error-queue",
      "pattern": ".*\\.error",
      "apply-to": "queues",
      "definition": {
        "ha-mode": "exactly",
        "ha-params": 3,
        "message-ttl": 604800000
      },
      "priority": 1000
    }
  ],
  "queues": [
    {
      "name": "email-service.delay.30000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "access-control.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "email-event-translator.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "contracts-app",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service.delay.1000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "consumer-events-service.work"
      }
    },
    {
      "name": "sift-service.delay.48000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "email-service.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "catchall",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "catchall.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "catchall.work"
      }
    },
    {
      "name": "reconciliation.herodotus.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "reconciliation.herodotus.work"
      }
    },
    {
      "name": "sales-intelligence.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.notifications.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-account.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.3000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "ui-v2.delay.10000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "ui-v2.work"
      }
    },
    {
      "name": "transaction-shovel",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "verification-events.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "verification-events.work"
      }
    },
    {
      "name": "sift-service.delay.1536000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "email-service.delay.7200000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "transaction-index-historical-application.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-index-historical-application.work"
      }
    },
    {
      "name": "accounting-event-consumer",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.7200000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    },
    {
      "name": "notification-events-service.delay.1000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "notification-events-service.work"
      }
    },
    {
      "name": "email-service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "reconciliation.herodotus.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "wire-transfer-uploader",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "ui-v2.work"
      }
    },
    {
      "name": "transaction-index-historical-application.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-verifier",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model-replay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.delay.1000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "ui-v2.work"
      }
    },
    {
      "name": "settlement-transfer-app.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "records-service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "notes.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "notes.work"
      }
    },
    {
      "name": "transaction-index-reindex-aws.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-reindex-aws.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-index-reindex-aws.work"
      }
    },
    {
      "name": "email-event-translator",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.96000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "sift-service.delay.3072000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "scheduled-payment-service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.notifications",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-replay-v2.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "funding-sources-read-model-replay-v2.work"
      }
    },
    {
      "name": "email-service.delay.21600000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "notes.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-account",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "beneficial-ownership",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-v2.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "funding-sources-read-model-v2.work"
      }
    },
    {
      "name": "cma-balance-delta-service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "device-activity-service.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "device-activity-service.work"
      }
    },
    {
      "name": "notification-events-service.delay.3600000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "notification-events-service.work"
      }
    },
    {
      "name": "accounting-event-consumer.delay.1000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    },
    {
      "name": "consumer-events-service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.24000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "transaction-read-model-replay.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-read-model-replay.work"
      }
    },
    {
      "name": "transaction-shovel.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-shovel.work"
      }
    },
    {
      "name": "sift-partner-integration.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "settlement-transfer-app",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "oauth-token",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service.delay.3600000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "consumer-events-service.work"
      }
    },
    {
      "name": "contracts-app.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "contracts-app.work"
      }
    },
    {
      "name": "access-control",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "oauth-token.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "oauth-token.work"
      }
    },
    {
      "name": "dwolla.eventhandler.transactions",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "verification-events",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.3600000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    },
    {
      "name": "sift-service.delay.192000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "accounting-event-consumer.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    },
    {
      "name": "notification-events-service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "notes",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "rebalance.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sales-intelligence",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "scheduled-payment-service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "rebalance",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.1000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "funding-sources-read-model-v2",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-replay-v2.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-shovel.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "cma-balance-delta-service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "webhook-subscriptions.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "rebalance.delay.100",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "rebalance.work"
      }
    },
    {
      "name": "transaction-index-reindex-aws",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "scheduled-payment-service.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "scheduled-payment-service.work"
      }
    },
    {
      "name": "transaction-index-reindex-aws.delay.1000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-index-reindex-aws.work"
      }
    },
    {
      "name": "funding-sources-read-model-v2.delay.100",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "funding-sources-read-model-v2.work"
      }
    },
    {
      "name": "contracts-app.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "rebalance.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "rebalance.work"
      }
    },
    {
      "name": "billable-events.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "customer-notification.delay.10000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "customer-notification.work"
      }
    },
    {
      "name": "rebalance.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "rebalance.work"
      }
    },
    {
      "name": "customer-notification.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "customer-notification.work"
      }
    },
    {
      "name": "beneficial-ownership.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "device-activity-service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.6000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "verification-events.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.300000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "dwolla.eventhandler.transactions.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.768000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "dwolla.ach.service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model-replay.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.fraud",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "billable-events.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "billable-events.work"
      }
    },
    {
      "name": "oauth-token.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.delay.30000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "ui-v2.work"
      }
    },
    {
      "name": "sift-partner-integration.delay.30000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-partner-integration.work"
      }
    },
    {
      "name": "sift-partner-integration.delay.600000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-partner-integration.work"
      }
    },
    {
      "name": "device-activity-service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "consumer-events-service.work"
      }
    },
    {
      "name": "billable-events",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "email-service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-application",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-replay-v2.delay.100",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "funding-sources-read-model-replay-v2.work"
      }
    },
    {
      "name": "customer-notification",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-replay-v2",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-v2.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-partner-integration",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "catchall.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.1800000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    },
    {
      "name": "wire-transfer-uploader.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "wire-transfer-uploader.work"
      }
    },
    {
      "name": "email-service.delay.1800000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "email-service.delay.3600000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "email-event-translator.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-event-translator.work"
      }
    },
    {
      "name": "transaction-index-verifier.delay.10000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-index-verifier.work"
      }
    },
    {
      "name": "ui-v2",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "beneficial-ownership.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "beneficial-ownership.work"
      }
    },
    {
      "name": "transaction-index-reindex-aws.delay.2000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-index-reindex-aws.work"
      }
    },
    {
      "name": "ui-v2.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.12000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "email-service.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "email-service.work"
      }
    },
    {
      "name": "sift-service.delay.384000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sift-service.work"
      }
    },
    {
      "name": "access-control.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "access-control.work"
      }
    },
    {
      "name": "notification-events-service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.21600000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    },
    {
      "name": "accounting-event-consumer.delay.300000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    },
    {
      "name": "sales-intelligence.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "sales-intelligence.work"
      }
    },
    {
      "name": "wire-transfer-uploader.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-verifier.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    },
    {
      "name": "webhook-subscriptions.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "webhook-subscriptions.work"
      }
    },
    {
      "name": "transaction-read-model",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "records-service.delay.10000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "records-service.work"
      }
    },
    {
      "name": "reconciliation.herodotus",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "cma-balance-delta-service.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "cma-balance-delta-service.work"
      }
    },
    {
      "name": "webhook-subscriptions",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "notification-events-service.delay.60000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "notification-events-service.work"
      }
    },
    {
      "name": "dwolla.ach.service.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "dwolla.ach.service.work"
      }
    },
    {
      "name": "transaction-index-historical-account.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-index-historical-account.work"
      }
    },
    {
      "name": "settlement-transfer-app.delay",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "settlement-transfer-app.work"
      }
    },
    {
      "name": "records-service",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "dwolla.ach.service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.fraud.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "transaction-read-model.work"
      }
    },
    {
      "name": "ui-v2.delay.5000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "ui-v2.work"
      }
    },
    {
      "name": "customer-notification.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "sift-service.error",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.30000",
      "vhost": "/",
      "durable": true,
      "auto_delete": false,
      "arguments": {
        "x-dead-letter-exchange": "accounting-event-consumer.work"
      }
    }
  ],
  "exchanges": [
    {
      "name": "notes.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "device-activity-service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-replay-v2.delay.100",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "verification-events.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notes.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-shovel.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "contracts-app.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service.delay.1000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.7200000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-application.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service.delay.3600000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "device-activity-service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-application.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-event-translator.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-account.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.96000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "beneficial-ownership.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "records-service.delay.10000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notification-events-service.delay.3600000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "records-service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notification-events-service.delay.1000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "reconciliation.herodotus.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-reindex-aws.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.ach.service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-verifier.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.24000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.3072000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "catchall.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-application.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-reindex-aws.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "customer-notification.delay.10000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.21600000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "contracts-app.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.delay.30000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "access-control.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "scheduled-payment-service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notification-events-service.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "customer-notification.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.6000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-event-translator.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "oauth-token.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.notifications.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-reindex-aws.delay.1000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "beneficial-ownership.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.transactions.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-reindex-aws.delay.2000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.fraud.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "access-control.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.ach.service.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-v2.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.3600000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-reindex-aws.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "settlement-transfer-app.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-verifier.delay.10000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.30000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.7200000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-replay-v2.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notification-events-service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "billable-events.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "reconciliation.herodotus.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "wire-transfer-uploader.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "wire-transfer-uploader.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.3600000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.12000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-replay-v2.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.1536000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model-replay.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "contracts-app.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-account.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "verification-events.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "access-control.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.30000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notification-events-service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.notifications.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sales-intelligence.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-historical-account.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "scheduled-payment-service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model-replay.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-partner-integration.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "cma-balance-delta-service.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "consumer-events-service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.768000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "wire-transfer-uploader.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "rebalance.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.192000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.delay.10000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "customer-notification.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.1800000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-v2.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "oauth-token.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "rebalance.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-replay-v2.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "scheduled-payment-service.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "billable-events.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "cma-balance-delta-service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.fraud.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.21600000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "ui-v2.delay.1000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.3000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "webhook-subscriptions.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-index-verifier.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-v2.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.48000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-partner-integration.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-partner-integration.delay.600000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "customer-notification.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "beneficial-ownership.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "events.catchall",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.300000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sales-intelligence.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "verification-events.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-service.delay.384000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model-replay.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "rebalance.delay.100",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "catchall.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "rebalance.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.1000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-event-translator.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "device-activity-service.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.1800000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "catchall.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-read-model.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "oauth-token.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.eventhandler.transactions.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-shovel.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "settlement-transfer-app.delay",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "email-service.delay.300000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "rebalance.delay.5000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "notes.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "webhook-subscriptions.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "records-service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "dwolla.ach.service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "reconciliation.herodotus.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "cma-balance-delta-service.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sift-partner-integration.delay.30000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "webhook-subscriptions.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "accounting-event-consumer.delay.1000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "billable-events.delay.60000",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "transaction-shovel.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "settlement-transfer-app.error",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "sales-intelligence.work",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "events",
      "vhost": "/",
      "type": "topic",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    },
    {
      "name": "funding-sources-read-model-v2.delay.100",
      "vhost": "/",
      "type": "fanout",
      "durable": true,
      "auto_delete": false,
      "internal": false,
      "arguments": {}
    }
  ],
  "bindings": [
    {
      "source": "access-control.delay",
      "vhost": "/",
      "destination": "access-control.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "access-control.error",
      "vhost": "/",
      "destination": "access-control.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "access-control.work",
      "vhost": "/",
      "destination": "access-control",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.1000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.1000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.1800000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.1800000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.21600000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.21600000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.30000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.30000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.300000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.300000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.3600000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.3600000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.5000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.60000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.delay.7200000",
      "vhost": "/",
      "destination": "accounting-event-consumer.delay.7200000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.error",
      "vhost": "/",
      "destination": "accounting-event-consumer.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "accounting-event-consumer.work",
      "vhost": "/",
      "destination": "accounting-event-consumer",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "beneficial-ownership.delay",
      "vhost": "/",
      "destination": "beneficial-ownership.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "beneficial-ownership.error",
      "vhost": "/",
      "destination": "beneficial-ownership.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "beneficial-ownership.work",
      "vhost": "/",
      "destination": "beneficial-ownership",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "billable-events.delay.60000",
      "vhost": "/",
      "destination": "billable-events.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "billable-events.error",
      "vhost": "/",
      "destination": "billable-events.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "billable-events.work",
      "vhost": "/",
      "destination": "billable-events",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "catchall.delay",
      "vhost": "/",
      "destination": "catchall.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "catchall.error",
      "vhost": "/",
      "destination": "catchall.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "catchall.work",
      "vhost": "/",
      "destination": "catchall",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "cma-balance-delta-service.delay.5000",
      "vhost": "/",
      "destination": "cma-balance-delta-service.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "cma-balance-delta-service.error",
      "vhost": "/",
      "destination": "cma-balance-delta-service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "cma-balance-delta-service.work",
      "vhost": "/",
      "destination": "cma-balance-delta-service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "consumer-events-service.delay.1000",
      "vhost": "/",
      "destination": "consumer-events-service.delay.1000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "consumer-events-service.delay.3600000",
      "vhost": "/",
      "destination": "consumer-events-service.delay.3600000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "consumer-events-service.delay.60000",
      "vhost": "/",
      "destination": "consumer-events-service.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "consumer-events-service.error",
      "vhost": "/",
      "destination": "consumer-events-service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "consumer-events-service.work",
      "vhost": "/",
      "destination": "consumer-events-service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "contracts-app.delay",
      "vhost": "/",
      "destination": "contracts-app.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "contracts-app.error",
      "vhost": "/",
      "destination": "contracts-app.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "contracts-app.work",
      "vhost": "/",
      "destination": "contracts-app",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "customer-notification.delay.10000",
      "vhost": "/",
      "destination": "customer-notification.delay.10000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "customer-notification.delay.60000",
      "vhost": "/",
      "destination": "customer-notification.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "customer-notification.error",
      "vhost": "/",
      "destination": "customer-notification.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "customer-notification.work",
      "vhost": "/",
      "destination": "customer-notification",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "device-activity-service.delay",
      "vhost": "/",
      "destination": "device-activity-service.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "device-activity-service.error",
      "vhost": "/",
      "destination": "device-activity-service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "device-activity-service.work",
      "vhost": "/",
      "destination": "device-activity-service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.ach.service.delay",
      "vhost": "/",
      "destination": "dwolla.ach.service.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.ach.service.error",
      "vhost": "/",
      "destination": "dwolla.ach.service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.ach.service.work",
      "vhost": "/",
      "destination": "dwolla.ach.service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.eventhandler.fraud.error",
      "vhost": "/",
      "destination": "dwolla.eventhandler.fraud.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.eventhandler.fraud.work",
      "vhost": "/",
      "destination": "dwolla.eventhandler.fraud",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.eventhandler.notifications.error",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.eventhandler.notifications.work",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.eventhandler.transactions.error",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "dwolla.eventhandler.transactions.work",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-event-translator.delay.60000",
      "vhost": "/",
      "destination": "email-event-translator.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-event-translator.error",
      "vhost": "/",
      "destination": "email-event-translator.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-event-translator.work",
      "vhost": "/",
      "destination": "email-event-translator",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.1000",
      "vhost": "/",
      "destination": "email-service.delay.1000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.1800000",
      "vhost": "/",
      "destination": "email-service.delay.1800000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.21600000",
      "vhost": "/",
      "destination": "email-service.delay.21600000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.30000",
      "vhost": "/",
      "destination": "email-service.delay.30000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.300000",
      "vhost": "/",
      "destination": "email-service.delay.300000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.3600000",
      "vhost": "/",
      "destination": "email-service.delay.3600000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.5000",
      "vhost": "/",
      "destination": "email-service.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.60000",
      "vhost": "/",
      "destination": "email-service.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.delay.7200000",
      "vhost": "/",
      "destination": "email-service.delay.7200000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.error",
      "vhost": "/",
      "destination": "email-service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "email-service.work",
      "vhost": "/",
      "destination": "email-service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "events.catchall",
      "destination_type": "exchange",
      "routing_key": "#",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.invite.resent",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.reset.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.reset.successful",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.reset.unsuccessful",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.user.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.user.deactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.user.deleted",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.user.joined",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.user.reactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "accesscontrol.user.updated.permissions",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.adjustmententry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.ach.service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.ach.service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model-replay.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.ach.service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.ach.service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.exported",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.exported",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.ach.service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model-replay.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.ach.service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "accounting.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.creditentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.creditentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.creditentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.creditentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.creditentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.creditentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.directentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.facilitatorfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.facilitatorfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model-replay.work",
      "destination_type": "exchange",
      "routing_key": "accounting.facilitatorfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.facilitatorfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.facilitatorfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model-replay.work",
      "destination_type": "exchange",
      "routing_key": "accounting.facilitatorfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.facilitatorfeeentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.facilitatorfeeentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feecreditentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feecreditentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feecreditentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feecreditentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feecreditentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feecreditentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feeentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.feeentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.fundsreservation.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.fundsreservation.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.fundsreservation.released",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.fundsreservation.released",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.adjustmententry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.adjustmententry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.adjustmententry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.adjustmententry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.adjustmententry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.adjustmententry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.bankentry.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.creditentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.creditentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.creditentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.creditentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.creditentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.creditentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.directentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.directentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.directentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.directentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.directentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.directentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.facilitatorfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.facilitatorfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.facilitatorfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.facilitatorfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.facilitatorfeeentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.facilitatorfeeentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feecreditentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feecreditentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feecreditentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feecreditentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feecreditentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feecreditentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feeentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.feeentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.fundsreservation.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.fundsreservation.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.fundsreservation.released",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.fundsreservation.released",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.operatingfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.operatingfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.operatingfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.operatingfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.settlementtransferentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.settlementtransferentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.settlementtransferentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.settlementtransferentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.settlementtransferentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.settlementtransferentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.wireentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.wireentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.wireentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.wireentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.wireentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.imported.wireentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.operatingfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.operatingfeeentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.operatingfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.operatingfeeentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "settlement-transfer-app.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "settlement-transfer-app.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "settlement-transfer-app.work",
      "destination_type": "exchange",
      "routing_key": "accounting.settlementtransferentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "wire-transfer-uploader.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.received",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "wire-transfer-uploader.work",
      "destination_type": "exchange",
      "routing_key": "accounting.wireentry.retried",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sales-intelligence.work",
      "destination_type": "exchange",
      "routing_key": "accounts.user.registered",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "applicationreview.review.approved",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "applicationreview.review.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "applicationreview.review.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "applicationreview.review.submitted",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "applicationreview.review.underreview",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "applicationreview.review.withdrawn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "balancelabel.entry.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "balancelabel.label.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "balancelabel.label.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.document.needed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.document.needed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.document.needed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.verification.incomplete",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.verification.incomplete",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "beneficialownership.owner.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "billpay.payment.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "client.verification.status.changed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "consumer.event.banktransfer.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "consumer.event.banktransfer.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "consumer.event.banktransfer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "consumer.event.banktransfer.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "webhook-subscriptions.work",
      "destination_type": "exchange",
      "routing_key": "consumer.event.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "consumer.events.replay.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "consumer.events.replay.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "contracts-app.work",
      "destination_type": "exchange",
      "routing_key": "contracts.invoice.processed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "billable-events.work",
      "destination_type": "exchange",
      "routing_key": "customer.record.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "customer.record.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "customer.record.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "customer.record.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "customer.record.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "customer.record.migrated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "customer.record.replay.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notes.work",
      "destination_type": "exchange",
      "routing_key": "customer.record.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.activated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.activated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.activated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.activated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.address.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.deactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.deactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.deactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "oauth-token.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.deactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.deactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "scheduled-payment-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.deactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "contracts-app.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.setting.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.setting.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "oauth-token.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "scheduled-payment-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-index-historical-account.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.transaction.reindex.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.wiredeposits.disabled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.account.wiredeposits.enabled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.canceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.canceledorreversed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.completedbankentryoncancel",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "billable-events.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.dispatchedforothertransfer",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.entryrejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.exported",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.feescanceled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.feescanceledorreversed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.feesreversed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "accounting-event-consumer.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.fundsreservationreleased",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.marksuccessful",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.processed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.processed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "billable-events.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "rebalance.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achrecord.rejected",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achreturns.filefailed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.achreturns.fileprocessed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.admin.access.approved",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.admin.access.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notes.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.admin.action",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-shovel.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.elastic.scan.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-shovel.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.elastic.shovel.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.email.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.email.sent",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.added",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "contracts-app.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.added",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.added",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.added",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.added",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.added",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.authorized",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "contracts-app.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.removed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.unremoved",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.unremoved",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.unremoved",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationattemptsexceeded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationattemptsexceeded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationattemptsexceeded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositcompleted",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositcompleted",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositcompleted",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositfailed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositfailed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositsadded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositsadded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositsadded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verificationdepositsadded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "billable-events.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.financialinstitution.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.masspay.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.masspay.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.masspay.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.masspay.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.masspay.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.masspay.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.oauthconsumer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.oauthconsumer.reactivated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.oauthconsumer.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-index-verifier.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transaction.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transaction.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-index-historical-application.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transaction.reindex.all.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transactions.assigned",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-index-historical-account.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transactions.assigned",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.autowithdraw.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.autowithdraw.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.claimed.direct",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model-replay.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.completed.notificationrequested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.markfundedsuccessful",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.reclaimed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.reclaimed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.reclaimed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.reclaimed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.transfer.source.latereject",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sales-intelligence.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.failedlogin",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "device-activity-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.loggedin",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.loggedin",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.loggedout",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.nasscoreupdated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.passwordchanged",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.passwordchanged",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.pinchanged",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.accountinfo",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.accountinfo",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.accountinfo",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.accountinfo",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.invalidssn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.invalidssn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.invalidssn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.invalidssn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.kba",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.kba",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.ssn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.ssn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.ssn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verification.ssn",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.user.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.needed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.needed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.needed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "records-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.needed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.uploaded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.uploaded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "verification-events.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.uploaded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.verification.document.verified",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "billable-events.work",
      "destination_type": "exchange",
      "routing_key": "dwolla.wiretransfer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-service.work",
      "destination_type": "exchange",
      "routing_key": "email.commands.sendtemplate",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "customer-notification.work",
      "destination_type": "exchange",
      "routing_key": "email.delivery.status",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "email.event.translator.negative.balance.email.sent",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "email.event.translator.negative.balance.email.sent",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model-replay.work",
      "destination_type": "exchange",
      "routing_key": "fee.replay.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "billable-events.work",
      "destination_type": "exchange",
      "routing_key": "fiaccountvalidator.balancerefresh.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-replay-v2.work",
      "destination_type": "exchange",
      "routing_key": "fundingsources.balance.reindex.request",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "fundingsources.balanceinquiry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "fundingsources.balanceinquiry.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-replay-v2.work",
      "destination_type": "exchange",
      "routing_key": "fundingsources.bank.reindex.request",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "funding-sources-read-model-replay-v2.work",
      "destination_type": "exchange",
      "routing_key": "fundingsources.wire.replay.request",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "members.account.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sales-intelligence.work",
      "destination_type": "exchange",
      "routing_key": "members.account.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "members.account.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "beneficial-ownership.work",
      "destination_type": "exchange",
      "routing_key": "members.account.ownershipstatus.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notes.work",
      "destination_type": "exchange",
      "routing_key": "members.account.suspended",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-partner-integration.work",
      "destination_type": "exchange",
      "routing_key": "members.account.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sift-service.work",
      "destination_type": "exchange",
      "routing_key": "members.account.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.transactions.work",
      "destination_type": "exchange",
      "routing_key": "members.account.upgraded",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "sales-intelligence.work",
      "destination_type": "exchange",
      "routing_key": "members.user.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "notification.event.banktransfer.cancelled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "notification.event.banktransfer.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "notification.event.banktransfer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "notification-events-service.work",
      "destination_type": "exchange",
      "routing_key": "notification.event.banktransfer.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "customer-notification.work",
      "destination_type": "exchange",
      "routing_key": "notification.event.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "oauth.consumer.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-index-reindex-aws.work",
      "destination_type": "exchange",
      "routing_key": "read.model.transaction.saved",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-index-reindex-aws.work",
      "destination_type": "exchange",
      "routing_key": "read.model.transaction.status.updated",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "rebalance.transaction.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "rebalance.transaction.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "cma-balance-delta-service.work",
      "destination_type": "exchange",
      "routing_key": "rebalance.transaction.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "reconciliation.herodotus.work",
      "destination_type": "exchange",
      "routing_key": "rebalance.transaction.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.fraud.work",
      "destination_type": "exchange",
      "routing_key": "sift.achrecord.created.scored",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "consumer-events-service.work",
      "destination_type": "exchange",
      "routing_key": "statements.statement.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "statements.statement.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model-replay.work",
      "destination_type": "exchange",
      "routing_key": "transaction.reindex.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "transaction-read-model-replay.work",
      "destination_type": "exchange",
      "routing_key": "transaction.replay.requested",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "twofactor.user.disabled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "twofactor.user.disabled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "access-control.work",
      "destination_type": "exchange",
      "routing_key": "twofactor.user.enabled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "twofactor.user.enabled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "dwolla.eventhandler.notifications.work",
      "destination_type": "exchange",
      "routing_key": "twofactor.user.forcedisabled",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "ui-v2.work",
      "destination_type": "exchange",
      "routing_key": "uiv2.export.created",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "webhook-subscriptions.work",
      "destination_type": "exchange",
      "routing_key": "webhook.request.completed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "webhook-subscriptions.work",
      "destination_type": "exchange",
      "routing_key": "webhook.request.failed",
      "arguments": {}
    },
    {
      "source": "events",
      "vhost": "/",
      "destination": "email-event-translator.work",
      "destination_type": "exchange",
      "routing_key": "webhook.subscription.paused",
      "arguments": {}
    },
    {
      "source": "events.catchall",
      "vhost": "/",
      "destination": "catchall.work",
      "destination_type": "exchange",
      "routing_key": "#",
      "arguments": {}
    },
    {
      "source": "funding-sources-read-model-replay-v2.delay.100",
      "vhost": "/",
      "destination": "funding-sources-read-model-replay-v2.delay.100",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "funding-sources-read-model-replay-v2.delay.5000",
      "vhost": "/",
      "destination": "funding-sources-read-model-replay-v2.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "funding-sources-read-model-replay-v2.error",
      "vhost": "/",
      "destination": "funding-sources-read-model-replay-v2.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "funding-sources-read-model-replay-v2.work",
      "vhost": "/",
      "destination": "funding-sources-read-model-replay-v2",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "funding-sources-read-model-v2.delay.100",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.delay.100",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "funding-sources-read-model-v2.delay.5000",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "funding-sources-read-model-v2.error",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "funding-sources-read-model-v2.work",
      "vhost": "/",
      "destination": "funding-sources-read-model-v2",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notes.delay",
      "vhost": "/",
      "destination": "notes.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notes.error",
      "vhost": "/",
      "destination": "notes.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notes.work",
      "vhost": "/",
      "destination": "notes",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notification-events-service.delay.1000",
      "vhost": "/",
      "destination": "notification-events-service.delay.1000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notification-events-service.delay.3600000",
      "vhost": "/",
      "destination": "notification-events-service.delay.3600000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notification-events-service.delay.60000",
      "vhost": "/",
      "destination": "notification-events-service.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notification-events-service.error",
      "vhost": "/",
      "destination": "notification-events-service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "notification-events-service.work",
      "vhost": "/",
      "destination": "notification-events-service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "oauth-token.delay",
      "vhost": "/",
      "destination": "oauth-token.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "oauth-token.error",
      "vhost": "/",
      "destination": "oauth-token.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "oauth-token.work",
      "vhost": "/",
      "destination": "oauth-token",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "rebalance.delay.100",
      "vhost": "/",
      "destination": "rebalance.delay.100",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "rebalance.delay.5000",
      "vhost": "/",
      "destination": "rebalance.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "rebalance.delay.60000",
      "vhost": "/",
      "destination": "rebalance.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "rebalance.error",
      "vhost": "/",
      "destination": "rebalance.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "rebalance.work",
      "vhost": "/",
      "destination": "rebalance",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "reconciliation.herodotus.delay.5000",
      "vhost": "/",
      "destination": "reconciliation.herodotus.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "reconciliation.herodotus.error",
      "vhost": "/",
      "destination": "reconciliation.herodotus.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "reconciliation.herodotus.work",
      "vhost": "/",
      "destination": "reconciliation.herodotus",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "records-service.delay.10000",
      "vhost": "/",
      "destination": "records-service.delay.10000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "records-service.error",
      "vhost": "/",
      "destination": "records-service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "records-service.work",
      "vhost": "/",
      "destination": "records-service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sales-intelligence.delay.60000",
      "vhost": "/",
      "destination": "sales-intelligence.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sales-intelligence.error",
      "vhost": "/",
      "destination": "sales-intelligence.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sales-intelligence.work",
      "vhost": "/",
      "destination": "sales-intelligence",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "scheduled-payment-service.delay",
      "vhost": "/",
      "destination": "scheduled-payment-service.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "scheduled-payment-service.error",
      "vhost": "/",
      "destination": "scheduled-payment-service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "scheduled-payment-service.work",
      "vhost": "/",
      "destination": "scheduled-payment-service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "settlement-transfer-app.delay",
      "vhost": "/",
      "destination": "settlement-transfer-app.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "settlement-transfer-app.error",
      "vhost": "/",
      "destination": "settlement-transfer-app.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "settlement-transfer-app.work",
      "vhost": "/",
      "destination": "settlement-transfer-app",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-partner-integration.delay.30000",
      "vhost": "/",
      "destination": "sift-partner-integration.delay.30000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-partner-integration.delay.600000",
      "vhost": "/",
      "destination": "sift-partner-integration.delay.600000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-partner-integration.error",
      "vhost": "/",
      "destination": "sift-partner-integration.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-partner-integration.work",
      "vhost": "/",
      "destination": "sift-partner-integration",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.12000",
      "vhost": "/",
      "destination": "sift-service.delay.12000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.1536000",
      "vhost": "/",
      "destination": "sift-service.delay.1536000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.192000",
      "vhost": "/",
      "destination": "sift-service.delay.192000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.24000",
      "vhost": "/",
      "destination": "sift-service.delay.24000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.3000",
      "vhost": "/",
      "destination": "sift-service.delay.3000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.3072000",
      "vhost": "/",
      "destination": "sift-service.delay.3072000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.384000",
      "vhost": "/",
      "destination": "sift-service.delay.384000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.48000",
      "vhost": "/",
      "destination": "sift-service.delay.48000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.6000",
      "vhost": "/",
      "destination": "sift-service.delay.6000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.768000",
      "vhost": "/",
      "destination": "sift-service.delay.768000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.delay.96000",
      "vhost": "/",
      "destination": "sift-service.delay.96000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.error",
      "vhost": "/",
      "destination": "sift-service.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "sift-service.work",
      "vhost": "/",
      "destination": "sift-service",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-historical-account.delay",
      "vhost": "/",
      "destination": "transaction-index-historical-account.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-historical-account.error",
      "vhost": "/",
      "destination": "transaction-index-historical-account.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-historical-account.work",
      "vhost": "/",
      "destination": "transaction-index-historical-account",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-historical-application.delay",
      "vhost": "/",
      "destination": "transaction-index-historical-application.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-historical-application.error",
      "vhost": "/",
      "destination": "transaction-index-historical-application.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-historical-application.work",
      "vhost": "/",
      "destination": "transaction-index-historical-application",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-reindex-aws.delay.1000",
      "vhost": "/",
      "destination": "transaction-index-reindex-aws.delay.1000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-reindex-aws.delay.2000",
      "vhost": "/",
      "destination": "transaction-index-reindex-aws.delay.2000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-reindex-aws.delay.5000",
      "vhost": "/",
      "destination": "transaction-index-reindex-aws.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-reindex-aws.error",
      "vhost": "/",
      "destination": "transaction-index-reindex-aws.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-reindex-aws.work",
      "vhost": "/",
      "destination": "transaction-index-reindex-aws",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-verifier.delay.10000",
      "vhost": "/",
      "destination": "transaction-index-verifier.delay.10000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-verifier.error",
      "vhost": "/",
      "destination": "transaction-index-verifier.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-index-verifier.work",
      "vhost": "/",
      "destination": "transaction-index-verifier",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-read-model-replay.delay.5000",
      "vhost": "/",
      "destination": "transaction-read-model-replay.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-read-model-replay.error",
      "vhost": "/",
      "destination": "transaction-read-model-replay.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-read-model-replay.work",
      "vhost": "/",
      "destination": "transaction-read-model-replay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-read-model.delay.5000",
      "vhost": "/",
      "destination": "transaction-read-model.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-read-model.error",
      "vhost": "/",
      "destination": "transaction-read-model.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-read-model.work",
      "vhost": "/",
      "destination": "transaction-read-model",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-shovel.delay",
      "vhost": "/",
      "destination": "transaction-shovel.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-shovel.error",
      "vhost": "/",
      "destination": "transaction-shovel.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "transaction-shovel.work",
      "vhost": "/",
      "destination": "transaction-shovel",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "ui-v2.delay.1000",
      "vhost": "/",
      "destination": "ui-v2.delay.1000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "ui-v2.delay.10000",
      "vhost": "/",
      "destination": "ui-v2.delay.10000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "ui-v2.delay.30000",
      "vhost": "/",
      "destination": "ui-v2.delay.30000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "ui-v2.delay.5000",
      "vhost": "/",
      "destination": "ui-v2.delay.5000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "ui-v2.delay.60000",
      "vhost": "/",
      "destination": "ui-v2.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "ui-v2.error",
      "vhost": "/",
      "destination": "ui-v2.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "ui-v2.work",
      "vhost": "/",
      "destination": "ui-v2",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "verification-events.delay.60000",
      "vhost": "/",
      "destination": "verification-events.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "verification-events.error",
      "vhost": "/",
      "destination": "verification-events.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "verification-events.work",
      "vhost": "/",
      "destination": "verification-events",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "webhook-subscriptions.delay",
      "vhost": "/",
      "destination": "webhook-subscriptions.delay",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "webhook-subscriptions.error",
      "vhost": "/",
      "destination": "webhook-subscriptions.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "webhook-subscriptions.work",
      "vhost": "/",
      "destination": "webhook-subscriptions",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "wire-transfer-uploader.delay.60000",
      "vhost": "/",
      "destination": "wire-transfer-uploader.delay.60000",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "wire-transfer-uploader.error",
      "vhost": "/",
      "destination": "wire-transfer-uploader.error",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    },
    {
      "source": "wire-transfer-uploader.work",
      "vhost": "/",
      "destination": "wire-transfer-uploader",
      "destination_type": "queue",
      "routing_key": "",
      "arguments": {}
    }
  ]
}
*/