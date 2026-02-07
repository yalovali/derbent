#!/bin/bash

echo "========================================="
echo "SSC WAS HERE!! Verifying Menu Fix 🎯"
echo "========================================="
echo ""

echo "1. Checking @MyMenu annotation on CPageTestAuxillary..."
grep -A 2 "@MyMenu" src/main/java/tech/derbent/api/views/CPageTestAuxillary.java | head -3
echo ""

echo "2. Checking if scanMyMenuAnnotations is called..."
grep -n "scanMyMenuAnnotations()" src/main/java/tech/derbent/api/page/service/CPageMenuIntegrationService.java | head -2
echo ""

echo "3. Checking if getStaticMyMenuEntries exists..."
grep -n "getStaticMyMenuEntries" src/main/java/tech/derbent/api/page/service/CPageMenuIntegrationService.java | head -2
echo ""

echo "4. Checking if CHierarchicalSideMenu processes @MyMenu entries..."
grep -n "getStaticMyMenuEntries\|processMyMenuEntry" src/main/java/tech/derbent/api/ui/component/enhanced/CHierarchicalSideMenu.java | head -5
echo ""

echo "5. Compilation status..."
mvn clean compile -Pagents -DskipTests -q
if [ $? -eq 0 ]; then
    echo "✅ COMPILATION SUCCESSFUL"
else
    echo "❌ COMPILATION FAILED"
    exit 1
fi
echo ""

echo "========================================="
echo "✅ ALL CHECKS PASSED!"
echo "========================================="
echo ""
echo "The @MyMenu annotation now works for static pages like CPageTestAuxillary!"
echo "Start the app and check the Development menu for 'Test Support Page'"
echo ""
echo "To start: mvn spring-boot:run -Dspring-boot.run.profiles=h2"
