#!/bin/bash
# Setup Java 21 environment for Derbent application

export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

echo "Java environment configured:"
java -version 2>&1 | head -1
