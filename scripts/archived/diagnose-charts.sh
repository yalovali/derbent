#!/bin/bash

# StoredObject Charts Diagnostic Script
# This script helps diagnose why SO-Charts are not rendering

echo "=================================================="
echo "StoredObject Charts Diagnostic Tool"
echo "=================================================="
echo ""

# Check 1: Maven Repository Access
echo "1. Checking StoredObject Maven repository access..."
if curl -I -s --connect-timeout 10 https://storedobject.com/maven/ > /dev/null 2>&1; then
    echo "   ✓ Repository is accessible"
else
    echo "   ✗ Repository is NOT accessible"
    echo "   Action: Check internet connection and firewall settings"
fi
echo ""

# Check 2: Local Maven Repository
echo "2. Checking local Maven repository for StoredObject JARs..."
SO_COMPONENTS="$HOME/.m2/repository/org/vaadin/addons/so/so-components/14.0.7"
SO_CHARTS="$HOME/.m2/repository/org/vaadin/addons/so/so-charts/5.0.3"
SO_HELPER="$HOME/.m2/repository/org/vaadin/addons/so/so-helper/5.0.1"

if [ -d "$SO_COMPONENTS" ] && [ -f "$SO_COMPONENTS/so-components-14.0.7.jar" ]; then
    echo "   ✓ so-components-14.0.7.jar found"
    ls -lh "$SO_COMPONENTS/so-components-14.0.7.jar"
else
    echo "   ✗ so-components-14.0.7.jar NOT found"
    echo "   Expected location: $SO_COMPONENTS/so-components-14.0.7.jar"
fi

if [ -d "$SO_CHARTS" ] && [ -f "$SO_CHARTS/so-charts-5.0.3.jar" ]; then
    echo "   ✓ so-charts-5.0.3.jar found"
    ls -lh "$SO_CHARTS/so-charts-5.0.3.jar"
else
    echo "   ✗ so-charts-5.0.3.jar NOT found"
    echo "   Expected location: $SO_CHARTS/so-charts-5.0.3.jar"
fi

if [ -d "$SO_HELPER" ] && [ -f "$SO_HELPER/so-helper-5.0.1.jar" ]; then
    echo "   ✓ so-helper-5.0.1.jar found"
    ls -lh "$SO_HELPER/so-helper-5.0.1.jar"
else
    echo "   ✗ so-helper-5.0.1.jar NOT found"
    echo "   Expected location: $SO_HELPER/so-helper-5.0.1.jar"
fi
echo ""

# Check 3: Frontend Generated Resources
echo "3. Checking frontend generated resources..."
FRONTEND_DIR="src/main/frontend/generated/jar-resources"
if [ -d "$FRONTEND_DIR" ]; then
    echo "   ✓ jar-resources directory exists"
    echo "   Contents:"
    ls -la "$FRONTEND_DIR" 2>/dev/null | head -20
    
    # Check for SO-related resources
    SO_RESOURCES=$(find "$FRONTEND_DIR" -name "*so*" -o -name "*storedobject*" 2>/dev/null | wc -l)
    if [ "$SO_RESOURCES" -gt 0 ]; then
        echo "   ✓ Found $SO_RESOURCES SO-related resource files"
    else
        echo "   ⚠ No SO-related resources found (this is expected if JARs are missing)"
    fi
else
    echo "   ✗ jar-resources directory does NOT exist"
    echo "   Location: $FRONTEND_DIR"
fi
echo ""

# Check 4: Application Configuration
echo "4. Checking Vaadin configuration..."
if grep -q "com.storedobject" src/main/resources/application.properties 2>/dev/null; then
    echo "   ✓ com.storedobject in vaadin.allowed-packages"
    grep "vaadin.allowed-packages" src/main/resources/application.properties
else
    echo "   ✗ com.storedobject NOT in vaadin.allowed-packages"
    echo "   Action: This should have been fixed by the recent PR"
fi
echo ""

# Check 5: Java and Maven versions
echo "5. Checking Java and Maven versions..."
echo "   Java version:"
java -version 2>&1 | head -1
echo "   Maven version:"
mvn -version 2>&1 | head -1
echo ""

# Check 6: Network connectivity to StoredObject
echo "6. Testing detailed network connectivity..."
echo "   DNS resolution:"
nslookup storedobject.com 2>/dev/null | grep -A2 "Name:" || echo "   ⚠ Cannot resolve storedobject.com"
echo ""
echo "   HTTP HEAD request:"
curl -I -s --max-time 10 https://storedobject.com/maven/ | head -5 || echo "   ✗ Cannot reach repository"
echo ""

# Summary and Recommendations
echo "=================================================="
echo "Summary and Recommendations"
echo "=================================================="
echo ""

ISSUES=0

# Check if JARs are missing
if [ ! -f "$SO_COMPONENTS/so-components-14.0.7.jar" ] || \
   [ ! -f "$SO_CHARTS/so-charts-5.0.3.jar" ] || \
   [ ! -f "$SO_HELPER/so-helper-5.0.1.jar" ]; then
    echo "⚠ CRITICAL: StoredObject JAR files are missing"
    echo "   This is the primary reason charts are not rendering."
    echo ""
    echo "   Recommended actions:"
    echo "   1. Check internet/network connectivity to https://storedobject.com"
    echo "   2. Try: mvn clean install -U (force update dependencies)"
    echo "   3. Check Maven proxy settings if behind a corporate firewall"
    echo "   4. Consider manual JAR installation if repository is not accessible"
    echo ""
    ISSUES=$((ISSUES + 1))
fi

# Check if configuration is wrong
if ! grep -q "com.storedobject" src/main/resources/application.properties 2>/dev/null; then
    echo "⚠ WARNING: Vaadin configuration may be incorrect"
    echo "   The recent PR should have fixed this, but please verify."
    echo ""
    ISSUES=$((ISSUES + 1))
fi

if [ $ISSUES -eq 0 ]; then
    echo "✓ No major issues detected"
    echo ""
    echo "If charts are still not rendering, check:"
    echo "  - Browser console (F12) for JavaScript errors"
    echo "  - Application logs for ChartException or other errors"
    echo "  - Network tab in browser dev tools for failed resource requests"
else
    echo "Found $ISSUES issue(s) that need attention."
fi

echo ""
echo "For more detailed troubleshooting, see:"
echo "  docs/fixes/storedobject-charts-troubleshooting.md"
echo ""
echo "=================================================="
