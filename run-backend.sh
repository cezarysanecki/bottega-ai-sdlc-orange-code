#!/usr/bin/env bash
#
# Healthcare Demo - Backend Server
# Starts Spring Boot with embedded PostgreSQL
# Requires: Java 22+
#
set -e

echo ""
echo "=============================================="
echo "   Healthcare Demo - Backend"
echo "=============================================="
echo ""

# Check for Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed."
    echo ""
    echo "Please install Java 22 or later from one of these sources:"
    echo "  - https://adoptium.net/ (recommended)"
    echo "  - https://www.oracle.com/java/technologies/downloads/"
    echo ""
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 22 ]; then
    echo "ERROR: Java 22+ required (found Java $JAVA_VERSION)"
    echo "Please upgrade Java from https://adoptium.net/"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -1)"
echo ""

# Change to the directory containing this script
cd "$(dirname "$0")/backend"

echo "Starting backend server..."
echo "API will be available at: http://localhost:8080"
echo ""

# Run with demo profile (embedded PostgreSQL)
exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo -DskipTests
