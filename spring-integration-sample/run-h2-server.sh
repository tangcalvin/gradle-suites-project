#!/usr/bin/env sh
# Start H2 TCP server first. Then run the Spring Boot app.
# H2 console: http://localhost:8080/h2-console — JDBC URL: jdbc:h2:tcp://localhost:9092/leaderdb, User: sa, Password: (empty)

set -e

cd "$(dirname "$0")/.."

echo "Starting H2 TCP server on port 9092..."
echo "Data directory: ./h2-data"
echo "JDBC URL for apps: jdbc:h2:tcp://localhost:9092/leaderdb"
echo ""
echo "Press Ctrl+C to stop the server."
echo ""

./gradlew :spring-integration-sample:runH2Server
