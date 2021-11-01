# RabbitMQ Topology Backup

A serverless app to regularly back up the topology of a given RabbitMQ cluster to CloudWatch Logs.

Set your AWS Account ID and S3 Bucket in `.env`, then run:

```shell
serverless deploy \
  --region eu-east-1 \
  --stage sandbox
```
