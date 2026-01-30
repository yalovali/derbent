#!/bin/bash

# BAB HTTP Client - Quick Test Script
# Tests connection to Calimero server from command line
# 
# Usage: ./test-calimero-connection.sh

echo "🤖 SSC WAS HERE!! Testing Calimero Connection"
echo "=============================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

CALIMERO_IP="127.0.0.1"
CALIMERO_PORT="8077"

echo "📍 Target: http://${CALIMERO_IP}:${CALIMERO_PORT}"
echo ""

# Test 1: Health Check
echo "Test 1: Health Check Endpoint"
echo "------------------------------"
HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" http://${CALIMERO_IP}:${CALIMERO_PORT}/health)
HTTP_CODE=$(echo "$HEALTH_RESPONSE" | tail -n 1)
BODY=$(echo "$HEALTH_RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✅ Health check SUCCESS${NC}"
    echo "Response: $BODY"
else
    echo -e "${RED}❌ Health check FAILED (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
    echo ""
    echo "Is Calimero server running?"
    echo "Start it with: cd ~/git/calimero && ./build/calimero"
    exit 1
fi

echo ""

# Test 2: Hello Request
echo "Test 2: Hello Message (System API)"
echo "-----------------------------------"

REQUEST_JSON='{
  "type": "question",
  "path": "/api/v1/system",
  "data": {
    "operation": "hello",
    "project_id": "test_project_123",
    "project_name": "Test Project",
    "timestamp": '$(date +%s000)'
  }
}'

echo "Request JSON:"
echo "$REQUEST_JSON" | jq '.' 2>/dev/null || echo "$REQUEST_JSON"
echo ""

HELLO_RESPONSE=$(curl -s -w "\n%{http_code}" \
    -X POST \
    -H "Content-Type: application/json" \
    -d "$REQUEST_JSON" \
    http://${CALIMERO_IP}:${CALIMERO_PORT}/api/request)

HTTP_CODE=$(echo "$HELLO_RESPONSE" | tail -n 1)
BODY=$(echo "$HELLO_RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✅ Hello request SUCCESS${NC}"
    echo "Response:"
    echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
    
    # Check status field
    STATUS=$(echo "$BODY" | jq -r '.status' 2>/dev/null)
    if [ "$STATUS" = "0" ]; then
        echo -e "${GREEN}✅ Response status: SUCCESS (0)${NC}"
    else
        echo -e "${YELLOW}⚠️  Response status: $STATUS${NC}"
    fi
else
    echo -e "${RED}❌ Hello request FAILED (HTTP $HTTP_CODE)${NC}"
    echo "Response: $BODY"
fi

echo ""
echo "=============================================="
echo "✅ All Tests Complete!"
echo ""
echo "Next Steps:"
echo "1. Start BAB application: mvn spring-boot:run -Dspring-boot.run.arguments=\"--spring.profiles.active=bab\""
echo "2. Create a BAB Gateway Project with IP: ${CALIMERO_IP}"
echo "3. Call project.connectToCalimero() from Java"
echo "4. Call project.sayHelloToCalimero() to test bidirectional communication"
echo ""
echo "See docs/bab/HTTP_CLIENT_IMPLEMENTATION_COMPLETE.md for detailed testing guide"
