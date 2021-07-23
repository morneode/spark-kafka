# README

## Create Topic

```bash
$KAFKA/bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic quickstart-events --partitions 1 --replication-factor 1
```

Using the running docker:

```bash
docker exec -it docker_kafka2_1 kafka-topics --zookeeper zookeeper:2181 --create --topic quickstart-events --partitions 1 --replication-factor 1
```

## Produce some messages

```bash
$KAFKA/bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092
```

## Consume some events

```bash
$KAFKA/bin/kafka-console-consumer.sh --topic quickstart-events --from-beginning --bootstrap-server localhost:9092
```

## Reference List

- https://devshawn.com/blog/apache-kafka-docker-quick-start/
