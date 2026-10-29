#!/usr/bin/env bash
# Build every jar and (re)start the whole stack from scratch: fresh databases,
# fresh Kafka topics. Returns once every container with a healthcheck is healthy.
#   ./start.sh          build + start
#   docker compose down stop everything
set -euo pipefail
cd "$(dirname "$0")"
mvn -q package
docker compose down --remove-orphans
docker compose up -d --build --force-recreate --wait
echo "Ready: http://localhost:8080"
