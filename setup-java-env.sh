#!/bin/bash

# Setup Java 21 Environment for Derbent
# This script ensures Java 21 is used for building and testing

# Check if Java 21 is available
if [ -d "/usr/lib/jvm/temurin-21-jdk-amd64" ]; then
    export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH
    echo "✅ Java 21 environment configured"
elif [ -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH
    echo "✅ Java 21 environment configured (OpenJDK)"
else
    echo "⚠️  Java 21 not found, using system default: $(java -version 2>&1 | head -1)"
fi

# Verify Java version
java -version 2>&1 | head -1
