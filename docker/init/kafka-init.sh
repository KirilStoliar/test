#!/bin/sh

set -eu

BROKER="redpanda:9092"

echo "Waiting for Redpanda Kafka API at ${BROKER}..."

until rpk topic list --brokers "$BROKER" >/dev/null 2>&1
do
    echo "Redpanda Kafka API is not ready yet..."
    sleep 2
done

echo "Redpanda Kafka API is ready."

create_topic() {
    topic="$1"

    if rpk topic describe "$topic" --brokers "$BROKER" >/dev/null 2>&1; then
        echo "Topic already exists: $topic"
    else
        echo "Creating topic: $topic"

        rpk topic create "$topic" \
            --brokers "$BROKER" \
            --partitions 3 \
            --replicas 1
    fi
}

create_topic "user.created"
create_topic "user.created.DLT"
create_topic "order.created"
create_topic "inventory.reserved"
create_topic "order.status-changed"

echo "Kafka topics initialization completed."

rpk topic list --brokers "$BROKER"