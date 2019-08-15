# RabbitMQ Topology Backup

A serverless app to regularly back up the topology of a given RabbitMQ cluster to CloudWatch Logs.

```shell
serverless deploy \
  --region eu-east-1 \
  --account {amazon-account-id} \
  --bucket my-amazing-s3-bucket \
  --stage sandbox
```
