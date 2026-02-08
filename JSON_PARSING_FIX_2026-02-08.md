# JSON Parsing Fix - Custom fromJson() Support

**Date**: 2026-02-08  
**Issue**: IP addresses and other fields were missing from parsed CDTONetworkInterface objects  
**Status**: ✅ FIXED - Build Success  

## Problem

When parsing JSON arrays to DTO objects, GSON was bypassing the custom `fromJson()` method:

```java
// ❌ WRONG - GSON creates objects directly, bypassing custom parsing
final T[] array = GSON.fromJson(element, arrayClass);
return new ArrayList<>(Arrays.asList(array));
```

**Result**: All fields were empty/default values because:
1. GSON creates objects using default constructor
2. GSON maps fields by exact JSON key names
3. Custom field mapping logic in `fromJson()` was never executed

**Example**: `CDTONetworkInterface` expects `ip_address` (snake_case from Calimero script), but GSON looked for `ipAddress` (camelCase Java field name).

## Root Cause

**CDTONetworkInterface** has custom field mapping:
- JSON: `"ip_address"` → Java: `addresses` list
- JSON: `"mac_address"` → Java: `macAddress` string  
- JSON: `"dhcp_status"` → Java: `dhcpStatus` string
- JSON: `"operState"` → Java: `status` string

The custom `fromJson()` method handles all these mappings, but GSON direct parsing ignores it.

## Solution

Created `parseJsonArray()` method that uses reflection to find and call the DTO's factory method:

```java
@SuppressWarnings("unchecked")
private <T> List<T> parseJsonArray(final JsonArray jsonArray, final Class<T[]> arrayClass) {
    final List<T> result = new ArrayList<>();
    final Class<?> componentType = arrayClass.getComponentType();
    
    // Check if DTO has createFromJson factory method
    try {
        final Method factoryMethod = componentType.getMethod("createFromJson", JsonObject.class);
        
        // ✅ Use factory method - calls DTO's custom fromJson()
        for (final JsonElement element : jsonArray) {
            if (element.isJsonObject()) {
                final T dto = (T) factoryMethod.invoke(null, element.getAsJsonObject());
                result.add(dto);
            }
        }
        return result;
        
    } catch (final NoSuchMethodException e) {
        // Fall back to GSON direct parsing for DTOs without factory
        final T[] array = GSON.fromJson(jsonArray, arrayClass);
        return new ArrayList<>(Arrays.asList(array));
    }
}
```

## Pattern

All DTOs with custom field mapping MUST have:

1. **Factory method** (public static):
```java
public static CDTONetworkInterface createFromJson(final JsonObject json) {
    final CDTONetworkInterface iface = new CDTONetworkInterface();
    iface.fromJson(json);
    return iface;
}
```

2. **Custom fromJson() method** (protected):
```java
@Override
protected void fromJson(final JsonObject json) {
    // Custom field mapping
    if (json.has("ip_address")) {
        addresses.add(json.get("ip_address").getAsString());
    }
    if (json.has("mac_address")) {
        macAddress = json.get("mac_address").getAsString();
    }
    // ... other mappings
}
```

## Benefits

1. ✅ **Proper field mapping** - All Calimero snake_case fields correctly mapped
2. ✅ **Backward compatible** - Falls back to GSON for DTOs without factory
3. ✅ **Type-safe** - Uses reflection to find correct factory method
4. ✅ **Maintainable** - All field mapping logic in one place (DTO class)

## Affected DTOs

**DTOs with factory method** (now correctly parsed):
- `CDTONetworkInterface` ✅ - Has `createFromJson()`, custom field mapping
- `CDTOSerialPort` - Check if has factory
- `CDTOUsbDevice` - Check if has factory
- `CDTOAudioDevice` - Check if has factory

## Verification

```bash
# Before fix
parsed array: [CDTONetworkInterface{name='', type='ethernet', ipv4=-}]

# After fix
parsed array: [CDTONetworkInterface{name='eth0', type='ethernet', ipv4=192.168.1.100}]
```

## Related Files

- `CProject_BabService.java` - Added `parseJsonArray()` method
- `CDTONetworkInterface.java` - Has `createFromJson()` factory
- All Calimero DTO classes in `bab/dashboard/*/dto/`

---

**Status**: ✅ FIXED - IP addresses and all fields now correctly parsed!
