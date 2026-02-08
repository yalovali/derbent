# Calimero Integration Documentation

**Date**: 2026-02-08  
**Status**: ACTIVE - Calimero scripts location and integration patterns  
**Related**: Network interfaces component, BAB dashboard integration

## CRITICAL: Calimero Source Location

**RULE**: ALL Calimero-related sources, scripts, and API implementations are located in the **separate** Calimero repository:

```bash
/home/yasin/git/calimero/
```

**NOT** in the Derbent project folder (`/home/yasin/git/derbent/`).

### Calimero Repository Structure

```
/home/yasin/git/calimero/
├── config/
│   └── scripts/              # Calimero system interface scripts
│       ├── list_all_interfaces.sh     # Master interface listing script
│       ├── list_usb_interfaces.sh     # USB device enumeration
│       ├── list_audio_interfaces.sh   # Audio device enumeration
│       ├── list_serial_interfaces.sh  # Serial port enumeration
│       └── ...
├── src/                      # C++ Calimero server implementation
├── docs/                     # Calimero API documentation
└── tests/                    # Calimero integration tests
```

## Network Interface Script Output Format

### Script Location

```bash
/home/yasin/git/calimero/config/scripts/list_all_interfaces.sh
```

### JSON Output Format

**Command**: `./list_all_interfaces.sh --json`

**Network Interface Object** (line 183 in script):

```json
{
  "interface": "eth0",
  "type": "ethernet",
  "state": "up",
  "mac_address": "b8:85:84:c1:53:bd",
  "ip_address": "192.168.1.100",
  "dhcp_status": "dhcp"
}
```

### Field Mapping

| Calimero Script Field | Java DTO Field | Type | Values |
|----------------------|----------------|------|---------|
| `interface` | `name` | String | "eth0", "wlx8c902d517a11" |
| `type` | `type` | String | "ethernet" |
| `state` | `status` | String | "up", "down" |
| `mac_address` | `macAddress` | String | "b8:85:84:c1:53:bd" |
| `ip_address` | `addresses` (List) | String | "192.168.1.100" or "none" |
| `dhcp_status` | `dhcpStatus` | String | "dhcp", "static", "unknown" |

## Integration with Derbent

### DTO Parsing (CDTONetworkInterface.java)

**Location**: `src/main/java/tech/derbent/bab/dashboard/dashboardproject_bab/dto/CDTONetworkInterface.java`

**Key Features**:

1. **Parses Calimero script output** with underscore field names (`mac_address`, `ip_address`, `dhcp_status`)
2. **Backward compatibility** with camelCase fields (`macAddress`, `ipAddress`)
3. **Filters invalid values** ("none", "unknown", "00:00:00:00:00:00")
4. **Converts to display format** via `getDhcpStatusDisplay()` ("dhcp" → "DHCP")

### Component Display (CComponentEthernetInterfaces.java)

**Location**: `src/main/java/tech/derbent/bab/dashboard/dashboardinterfaces/view/CComponentEthernetInterfaces.java`

**Grid Columns**:

1. **Interface** - Name (e.g., "eth0")
2. **Status** - Colored badge (Up=green, Down=red)
3. **IP Address** - List of addresses or "No IP"
4. **DHCP** - Colored badge (DHCP=blue, Static=gray, Unknown=light gray)
5. **MAC Address** - Hardware address
6. **Type** - Interface type ("ethernet")
7. **MTU** - Maximum transmission unit

## Script Enhancement (2026-02-08)

### Fixed Issues

**Problem**: MAC address showing "unknown" in grid display

**Root Cause**: Script was trying to extract MAC from first line of `ip link` output, which doesn't contain the link address.

**Solution**: Updated `get_network_interfaces()` function (line 121-143):

```bash
# Get MAC address from full interface output (use separate ip link command)
local mac=$(ip link show "$interface" 2>/dev/null | grep -oE '([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}' | head -1)
if [[ -z "$mac" || "$mac" == "ff:ff:ff:ff:ff:ff" ]]; then
    mac="00:00:00:00:00:00"
fi

# Get IP address (IPv4)
local ip_addr=$(ip -4 addr show "$interface" 2>/dev/null | grep -oP '(?<=inet\s)\d+(\.\d+){3}' | head -1)
if [[ -z "$ip_addr" ]]; then
    ip_addr="none"
fi
```

**Result**: Now correctly extracts MAC addresses like `b8:85:84:c1:53:bd` instead of "unknown".

### Test Output

```bash
cd /home/yasin/git/calimero
./config/scripts/list_all_interfaces.sh --json | grep -A 5 "network_interfaces"
```

**Sample Output**:

```json
"network_interfaces": [
  {"interface": "eno1", "type": "ethernet", "state": "down", "mac_address": "b8:85:84:c1:53:bd", "ip_address": "none", "dhcp_status": "unknown"},
  {"interface": "wlx8c902d517a11", "type": "ethernet", "state": "up", "mac_address": "8c:90:2d:51:7a:11", "ip_address": "192.168.68.66", "dhcp_status": "unknown"}
]
```

## Data Flow

```
1. Calimero Script (list_all_interfaces.sh)
   ↓ Executes system commands (ip link, ip addr)
   ↓ Returns JSON with underscore field names

2. Calimero HTTP Server (C++)
   ↓ Serves script output via HTTP API
   ↓ Endpoint: POST /api/request (type="iot", operation="getAllInterfaces")

3. CProject_BabService.refreshInterfacesJson()
   ↓ Calls Calimero HTTP API
   ↓ Stores raw JSON in project.interfacesJson field

4. CProject_BabService.getNetworkInterfaces()
   ↓ Parses JSON from project.interfacesJson
   ↓ Creates List<CDTONetworkInterface>

5. CComponentEthernetInterfaces.refreshComponent()
   ↓ Gets interfaces from service
   ↓ Displays in grid with colored badges
```

## Development Workflow

### When Updating Calimero Scripts

1. **Edit scripts** in `/home/yasin/git/calimero/config/scripts/`
2. **Test locally**:
   ```bash
   cd /home/yasin/git/calimero
   ./config/scripts/list_all_interfaces.sh --json | jq '.system_interfaces.network_interfaces'
   ```
3. **Verify field names** match DTO expectations
4. **Update DTO parsing** if script adds new fields
5. **Update component** if new columns needed
6. **Compile Derbent**:
   ```bash
   cd /home/yasin/git/derbent
   ./mvnw clean compile -Pagents -DskipTests
   ```

### When Adding New Interface Types

1. **Create script** in Calimero repo (e.g., `list_video_interfaces.sh`)
2. **Add to master script** (`list_all_interfaces.sh`)
3. **Create DTO** in Derbent (`CDTOVideoDevice.java`)
4. **Add service method** (`CProject_BabService.getVideoDevices()`)
5. **Create component** (`CComponentVideoInterfaces.java`)
6. **Add placeholder field** in entity
7. **Register in initializer**

## Known Limitations

1. **DHCP Status Detection**:
   - Currently returns "unknown" for most interfaces
   - Requires NetworkManager, systemd-networkd, or dhclient process inspection
   - Script has detection logic but may need tuning per system

2. **Interface Types**:
   - All interfaces currently marked as "ethernet"
   - Could be enhanced to detect "wireless", "bridge", "vlan", etc.

3. **MTU Display**:
   - Not populated by current script version
   - Could be added via `ip link show` output parsing

## Future Enhancements

1. **Real-time Updates**:
   - WebSocket push from Calimero when interfaces change
   - Auto-refresh component without manual refresh button

2. **Interface Configuration**:
   - Edit IP address dialog
   - DHCP enable/disable toggle
   - MTU configuration

3. **Advanced Metrics**:
   - RX/TX bytes, packets, errors (available in script but not displayed)
   - Bandwidth graphs
   - Historical data

## Related Documentation

- `BAB_SESSION_COMPLETE_2026-02-08.md` - BAB component patterns
- `AGENTS.md` - Section 6.11 @Transient Placeholder Pattern
- Calimero README: `/home/yasin/git/calimero/README.md`

---

**REMEMBER**: Always check `/home/yasin/git/calimero/` for Calimero script sources, NOT the Derbent project folder!
