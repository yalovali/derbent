#!/bin/bash

# Install StoredObject Libraries to Local Maven Repository
# This script installs the SO libraries from lib/ folder to the local Maven repository
# Run this script once after cloning the repository or when the libraries are updated

set -e

echo "🔧 Installing StoredObject Libraries to Local Maven Repository"
echo "=============================================================="

# Check if lib folder exists
if [ ! -d "lib" ]; then
    echo "❌ Error: lib/ folder not found"
    echo "Please make sure you're running this script from the project root directory"
    exit 1
fi

# Check if JARs exist
if [ ! -f "lib/so-components-14.0.7.jar" ] || [ ! -f "lib/so-charts-5.0.3.jar" ] || [ ! -f "lib/so-helper-5.0.1.jar" ]; then
    echo "❌ Error: Required JAR files not found in lib/ folder"
    echo "Expected files:"
    echo "  - lib/so-components-14.0.7.jar"
    echo "  - lib/so-charts-5.0.3.jar"
    echo "  - lib/so-helper-5.0.1.jar"
    exit 1
fi

echo ""
echo "📦 Installing so-components-14.0.7.jar..."
mvn install:install-file \
  -Dfile=lib/so-components-14.0.7.jar \
  -DgroupId=org.vaadin.addons.so \
  -DartifactId=so-components \
  -Dversion=14.0.7 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -DcreateChecksum=true \
  -q

echo "📦 Installing so-charts-5.0.3.jar..."
mvn install:install-file \
  -Dfile=lib/so-charts-5.0.3.jar \
  -DgroupId=org.vaadin.addons.so \
  -DartifactId=so-charts \
  -Dversion=5.0.3 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -DcreateChecksum=true \
  -q

echo "📦 Installing so-helper-5.0.1.jar..."
mvn install:install-file \
  -Dfile=lib/so-helper-5.0.1.jar \
  -DgroupId=org.vaadin.addons.so \
  -DartifactId=so-helper \
  -Dversion=5.0.1 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -DcreateChecksum=true \
  -q

echo ""
echo "✅ StoredObject libraries installed successfully!"
echo ""
echo "Libraries installed to: ~/.m2/repository/org/vaadin/addons/so/"
echo "  - so-components 14.0.7"
echo "  - so-charts 5.0.3"
echo "  - so-helper 5.0.1"
echo ""
echo "You can now build and run the project with: mvn clean compile"
