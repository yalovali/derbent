#!/bin/bash

# Comprehensive Test Script for CPageTestAuxillaryComprehensiveTest
# This script runs the comprehensive page tester that visits all buttons in CPageTestAuxillary

set -e

echo "🚀 Starting CPageTestAuxillary Comprehensive Test"
echo "=================================================="

# Ensure Java 21 is configured
source ./setup-java-env.sh

# Create screenshots directory
mkdir -p target/screenshots

# Run the comprehensive test
echo "📋 Running CPageTestAuxillaryComprehensiveTest..."
mvn test -Dtest=CPageTestAuxillaryComprehensiveTest -Dplaywright.headless=true

echo "✅ Test completed! Check target/screenshots/ for results"
