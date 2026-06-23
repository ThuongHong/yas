#!/bin/bash
# Trimmed infra for 16GB Minikube demo (Project 2).
# Installs ONLY: postgres, kafka (1 broker), elasticsearch (single node).
# Skips upstream observability stack, cert-manager, pgadmin, akhq, standalone zookeeper.
# Run keycloak + redis separately via ./setup-keycloak.sh and ./setup-redis.sh.
# Must be run from k8s/deploy/ (relative ./ paths + ./cluster-config.yaml). Needs: helm, yq.
# set -x only (not -e): `read -rd ''` returns nonzero at EOF by design.
set -x

helm repo add postgres-operator-charts https://opensource.zalando.com/postgres-operator/charts/postgres-operator
helm repo add strimzi https://strimzi.io/charts/
helm repo add elastic https://helm.elastic.co
helm repo update

read -rd '' POSTGRESQL_REPLICAS POSTGRESQL_USERNAME POSTGRESQL_PASSWORD \
KAFKA_REPLICAS ZOOKEEPER_REPLICAS ELASTICSEARCH_REPLICAES DOMAIN \
< <(yq -r '.postgresql.replicas, .postgresql.username, .postgresql.password,
 .kafka.replicas, .zookeeper.replicas, .elasticsearch.replicas, .domain' ./cluster-config.yaml)

# --- Postgres ---
helm upgrade --install postgres-operator postgres-operator-charts/postgres-operator \
 --create-namespace --namespace postgres
helm upgrade --install postgres ./postgres/postgresql \
 --namespace postgres \
 --set replicas="$POSTGRESQL_REPLICAS" \
 --set username="$POSTGRESQL_USERNAME" \
 --set password="$POSTGRESQL_PASSWORD"

# --- Kafka (strimzi; chart includes debezium connect for search CDC) ---
# Pin 0.45.2: last release with ZooKeeper + Kafka v1beta2 API (charts use both).
# Newer strimzi is KRaft-only and serves only v1 -> incompatible with these charts.
helm upgrade --install kafka-operator strimzi/strimzi-kafka-operator \
 --version 0.45.2 \
 --create-namespace --namespace kafka
helm upgrade --install kafka-cluster ./kafka/kafka-cluster \
 --namespace kafka \
 --set kafka.replicas="$KAFKA_REPLICAS" \
 --set zookeeper.replicas="$ZOOKEEPER_REPLICAS" \
 --set postgresql.username="$POSTGRESQL_USERNAME" \
 --set postgresql.password="$POSTGRESQL_PASSWORD"

# --- Elasticsearch (eck) ---
helm upgrade --install elastic-operator elastic/eck-operator \
 --create-namespace --namespace elasticsearch
helm upgrade --install elasticsearch-cluster ./elasticsearch/elasticsearch-cluster \
 --namespace elasticsearch \
 --set elasticsearch.replicas="$ELASTICSEARCH_REPLICAES" \
 --set kibana.ingress.hostname="kibana.$DOMAIN"
