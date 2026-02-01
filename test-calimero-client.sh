#!/bin/bash
# Test script for Calimero HTTP Client
# This script tests the Derbent BAB HTTP client integration with Calimero server

TOKEN="test-token-123"
BASE_URL="http://localhost:8077"

echo "🧪 Calimero API Test Suite"
echo "============================"
echo ""

# Test 1: Health check (no auth)
echo "✅ Test 1: Health Check (no auth)"
curl -s $BASE_URL/health | jq '.'
echo ""

# Test 2: Get network interfaces
echo "✅ Test 2: Get Network Interfaces"
curl -s -X POST $BASE_URL/api/request \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"network","data":{"operation":"getInterfaces"}}' | jq '.data.interfaces[] | {name, type, status, macAddress}'
echo ""

# Test 3: Get specific interface
echo "✅ Test 3: Get Specific Interface (eth0)"
curl -s -X POST $BASE_URL/api/request \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"network","data":{"operation":"getInterface","interface":"eth0"}}' | jq '.data'
echo ""

# Test 4: Get interface states
echo "✅ Test 4: Get Interface States"
curl -s -X POST $BASE_URL/api/request \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"network","data":{"operation":"getInterfaceStates"}}' | jq '.data'
echo ""

# Test 5: Service discovery
echo "✅ Test 5: Service Discovery"
curl -s -X POST $BASE_URL/api/request \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"service","data":{"operation":"list"}}' | jq '.data.count, .data.services[0:3]'
echo ""

# Test 6: System info
echo "✅ Test 6: System Info"
curl -s -X POST $BASE_URL/api/request \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"system","data":{"operation":"info"}}' | jq '.data'
echo ""

echo "============================"
echo "✅ All tests completed!"
