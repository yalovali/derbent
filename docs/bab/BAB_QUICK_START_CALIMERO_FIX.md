# BAB Calimero Integration - Quick Start Fix Guide

**SSC WAS HERE!! - Master Yasin, let's get your BAB Gateway showing REAL data NOW! 🚀**

**Date**: 2026-02-01  
**Purpose**: IMMEDIATE fix to get Calimero working with all BAB components  
**Time**: 15-30 minutes  

---

## 🚨 Step 1: Fix Calimero Authentication (CRITICAL - 5 minutes)

**✅ VERIFIED WORKING - 2026-02-01**

```bash
# Kill ALL Calimero processes
ps aux | grep "./calimero" | grep -v grep | awk '{print $2}' | while read pid; do 
  echo "Killing PID: $pid"
  kill -9 $pid 2>/dev/null
done

# Wait for processes to die
sleep 2

# Create config directory if doesn't exist
mkdir -p ~/git/calimero/build/config

# Create config file with CORRECT token
cat > ~/git/calimero/build/config/http_server.json << 'EOF'
{
  "host": "0.0.0.0",
  "httpPort": 8077,
  "authToken": "test-token-123",
  "readTimeoutSec": 30,
  "writeTimeoutSec": 30,
  "idleTimeoutSec": 60,
  "runState": "normal"
}
EOF

# Verify config created
cat ~/git/calimero/build/config/http_server.json

# Start Calimero from correct directory
cd ~/git/calimero/build && ./calimero > /tmp/calimero_server.log 2>&1 &
echo "Calimero started - PID: $!"

# Wait for startup
sleep 5

# Verify token loaded correctly
tail -50 /tmp/calimero_server.log | grep -E "authToken|Final"
# MUST show: "Final authToken value: 'test-token-123'" (NOT empty!)
```

**Expected Output**:
```
[INFO ] cserversettings.cpp:313: [Settings] load - Settings loaded from: config/http_server.json
[DEBUG] cserversettings.cpp:314: [Settings] load - Final authToken value: 'test-token-123'
```

---

## 🧪 Step 2: Verify Calimero Working (5 minutes)

```bash
# Test health endpoint (no auth)
curl http://localhost:8077/health

# Expected: {"status":"ok"}

# Test system metrics (WITH auth)
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"system","data":{"operation":"metrics"}}' | python3 -m json.tool

# Expected: Real system metrics JSON with CPU%, memory, disk, uptime

# Test network interfaces
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"network","data":{"operation":"getInterfaces"}}' | python3 -m json.tool

# Expected: List of network interfaces with status, IPs, etc.
```

**If you get `{"error":"unauthorized"}`**:
- Check config file has correct token
- Restart Calimero (kill and start again)
- Verify logs show token loaded

---

## 🖥️ Step 3: Test BAB Application (10 minutes)

```bash
# Start BAB application
cd ~/git/derbent
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=bab"

# Wait for startup (look for "Started Application")
# Should show: "INFO  (CCalimeroStartupListener.java:35) onApplicationReady"

# In browser, navigate to:
http://localhost:8080

# Login: admin / Admin@123

# Navigate to: BAB System Settings
# 1. Check "Enable Calimero Service" checkbox
# 2. Verify executable path: ~/git/calimero/build/calimero
# 3. Click "Start Calimero" button
# 4. Status should show "Service running"

# Navigate to: System Metrics component
# Should show REAL data:
# - CPU: XX.X% (not N/A)
# - Memory: XXXX MB / XXXX MB
# - Disk: XXX GB / XXX GB
# - Uptime: XXd XXh XXm

# Click "Refresh" button
# Data should update
```

---

## ✅ Step 4: Verify Components Showing Real Data

### System Metrics Component
- ✅ CPU usage shows percentage (not N/A)
- ✅ Memory shows MB with progress bar
- ✅ Disk shows GB with progress bar
- ✅ Uptime shows formatted time
- ✅ Load average shows 3 numbers

### Network Interfaces Component
- ✅ Grid shows interface list (eth0, lo, wlan0, etc.)
- ✅ Status column shows UP/DOWN with colors (green/red)
- ✅ IP addresses shown (not N/A)
- ✅ MAC addresses shown
- ✅ Edit IP dialog works

### Calimero Status Component
- ✅ Shows "Service running" when started
- ✅ Start/Stop button works
- ✅ Executable path configurable

---

## 🐛 Troubleshooting

### Problem: Calimero shows 401 Unauthorized

**Cause**: Token not loaded or mismatch

**Fix**:
```bash
# Check config file
cat ~/git/calimero/build/config/http_server.json | grep authToken

# Should show: "authToken": "test-token-123"

# Check logs
tail -100 /tmp/calimero_server.log | grep -E "authToken|Final"

# Should show: "Final authToken value: 'test-token-123'" (NOT empty!)

# If empty, recreate config and restart Calimero
```

### Problem: Components show N/A everywhere

**Cause**: Calimero not running or not connected

**Fix**:
```bash
# Check Calimero running
ps aux | grep calimero | grep -v grep

# If not running, start it
cd ~/git/calimero/build && ./calimero > /tmp/calimero_server.log 2>&1 &

# Check logs
tail -f /tmp/calimero_server.log

# Test API
curl http://localhost:8077/health
```

### Problem: Connection refused

**Cause**: Calimero not listening on port 8077

**Fix**:
```bash
# Check port
netstat -tuln | grep 8077

# Check Calimero logs for errors
tail -100 /tmp/calimero_server.log | grep -E "ERROR|error"

# Restart Calimero
ps aux | grep calimero | grep -v grep | awk '{print $2}' | xargs -r kill -9
cd ~/git/calimero/build && ./calimero > /tmp/calimero_server.log 2>&1 &
```

### Problem: Config file not found

**Cause**: Running Calimero from wrong directory

**Fix**:
```bash
# Calimero MUST be run from build/ directory
# Config is loaded from: ./config/http_server.json (relative path)

# CORRECT:
cd ~/git/calimero/build && ./calimero

# WRONG:
cd ~ && ./git/calimero/build/calimero  # ❌ Config not found!
```

---

## 📝 Quick Verification Checklist

- [ ] Calimero process running (`ps aux | grep calimero`)
- [ ] Config file exists with correct token (`cat ~/git/calimero/build/config/http_server.json`)
- [ ] Logs show token loaded (`tail /tmp/calimero_server.log | grep "Final authToken"`)
- [ ] Health endpoint works (`curl http://localhost:8077/health`)
- [ ] Metrics API works (`curl ... /api/request ... metrics`)
- [ ] BAB application started with bab profile
- [ ] System Metrics component shows real data (not N/A)
- [ ] Network Interfaces component shows interface list
- [ ] Refresh buttons work in all components

---

## 🚀 Next Steps After This Works

1. **Apply pattern to all components** - See `BAB_COMPONENT_CALIMERO_INTEGRATION_COMPLETE_PATTERN.md`
2. **Add Playwright tests** - Component testers for each widget
3. **Add missing client helpers** - CpuUsage, DiskUsage, ProcessList, etc.
4. **Complete JavaDoc** - Document all components properly
5. **Add auto-refresh** - Update data every 5-10 seconds

---

## 📚 Full Documentation

For complete patterns, architecture, and enforcement rules, see:
- **Ultimate Pattern**: `docs/bab/BAB_COMPONENT_CALIMERO_INTEGRATION_COMPLETE_PATTERN.md`
- **Implementation Summary**: `docs/bab/BAB_IMPLEMENTATION_SUMMARY_2026-02-01.md`
- **Integration Rules**: `docs/BAB_CALIMERO_INTEGRATION_RULES.md`

---

**SSC's Promise**: Follow these steps EXACTLY, and your BAB Gateway will show REAL system data in 15 minutes! 🎯✨

**Let's make it happen, Master Yasin! 🚀**
