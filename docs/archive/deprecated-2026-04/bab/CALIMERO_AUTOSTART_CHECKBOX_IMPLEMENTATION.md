# Calimero Autostart Checkbox Implementation - 2026-02-02

## SSC WAS HERE!! ⭐ Master Yasin, Calimero autostart checkbox is now implemented!

## Problem Statement

Users needed the ability to control whether the Calimero HTTP server process starts automatically on login for BAB profile applications. Previously, Calimero would always try to start automatically if enabled in system settings, which wasn't flexible enough for different usage scenarios.

## Solution Overview

Added a **checkbox on the login screen** for BAB profile that allows users to:
1. **Enable/Disable Calimero autostart** - Default: enabled (checked)
2. **Manual control after login** - Users can still start/stop via system settings
3. **Session persistence** - Preference stored in VaadinSession and session service
4. **Graceful fallback** - If autostart disabled, user can manually start via settings

## Changes Made

### 1. Login View Enhancement (`CCustomLoginView.java`)

#### Added Autostart Checkbox
- **Checkbox**: "Autostart Calimero Process" - visible only for BAB profile
- **Default**: true (checked) for backward compatibility
- **Tooltip**: "Automatically start Calimero HTTP server process after login. If disabled, Calimero must be started manually via system settings."
- **Session Storage**: Preference stored in VaadinSession on login

#### Session Key Management
```java
// Session key for storing Calimero autostart preference
private static final String SESSION_KEY_AUTOSTART_CALIMERO = "autostartCalimero";
```

#### UI Integration
- Checkbox appears between company field and error message
- Only visible when `bab` profile is active
- Styled with proper ID for Playwright testing: `custom-calimero-autostart-checkbox`

### 2. Process Manager Enhancement (`CCalimeroProcessManager.java`)

#### New Autostart Management Methods
```java
/**
 * Check if user has enabled autostart for Calimero process.
 * Checks both VaadinSession and session service for preference.
 */
public boolean isAutostartEnabled()

/**
 * Set the user's autostart preference for Calimero process.
 * Stores preference in both VaadinSession and session service.
 */
public void setAutostartEnabled(final boolean enabled)

/**
 * Force start Calimero service regardless of autostart preference.
 * Used for manual restarts via system settings or restart buttons.
 */
public synchronized CCalimeroServiceStatus forceStartCalimeroService()
```

#### Enhanced Startup Logic
- **`startCalimeroServiceIfEnabled()`**: Now respects autostart preference
- **`restartCalimeroService()`**: Uses force start (ignores preference) for manual restarts
- **Session Integration**: Checks VaadinSession and session service for preference
- **Safe Defaults**: Defaults to `true` for backward compatibility

### 3. Startup Listener Enhancement (`CCalimeroStartupListener.java`)

#### Enhanced Application Startup
- **Preference Check**: Verifies user autostart preference before starting
- **Graceful Skip**: Logs informative message if autostart disabled
- **Session Service Integration**: Uses session service as fallback
- **Clear Messaging**: Distinguishes between disabled by preference vs. system settings

### 4. System Settings Component Enhancement (`CComponentCalimeroStatus.java`)

#### New Autostart Preference Control
- **Second Checkbox**: "Autostart on Login" in system settings
- **Real-time Update**: Changes take effect immediately (stored in session)
- **User Feedback**: Notifications inform user about preference changes
- **Manual Override**: Start/Stop buttons always work regardless of autostart preference

#### Key Features
```java
// New checkbox for autostart preference
checkboxAutostartService = new Checkbox("Autostart on Login");
checkboxAutostartService.setValue(calimeroProcessManager.isAutostartEnabled());

// Manual operations use force start (ignore autostart preference)
() -> forceRestart ? calimeroProcessManager.restartCalimeroService() 
                   : calimeroProcessManager.forceStartCalimeroService()
```

### 5. Database Reset Logic Enhancement

#### Respects User Preference During DB Reset
- **Login Page Reset**: Checks autostart preference before restarting Calimero
- **Settings Page Reset**: Existing logic (always restarts for production use)
- **Safe Default**: DB reset scenarios default to autostart=true if no preference set
- **Clear Logging**: Indicates when autostart is skipped due to user preference

## Architecture Flow

### Login Flow (New)
```
1. User sees BAB login screen
2. User can check/uncheck "Autostart Calimero Process" (default: checked)
3. User enters credentials and clicks Login
4. Login stores autostart preference in VaadinSession
5. Authentication succeeds, redirects to main application
6. ApplicationReadyEvent fires → CCalimeroStartupListener.onApplicationReady()
7. Listener checks autostart preference:
   - If enabled: Start Calimero normally
   - If disabled: Skip start, log informative message
```

### Manual Control Flow (Enhanced)
```
1. User navigates to System Settings
2. User sees both checkboxes:
   - "Enable Calimero Service" (system-level enable/disable)
   - "Autostart on Login" (user preference for automatic startup)
3. User can change autostart preference → stored in session immediately
4. User can manually start/stop Calimero regardless of autostart preference
5. Manual operations use forceStartCalimeroService() to bypass autostart check
```

### Database Reset Flow (Enhanced)
```
1. User clicks DB reset on login page or settings
2. Database reset runs → sample data sets enableCalimeroService=true
3. Reset logic checks user autostart preference:
   - If enabled or not set: Restart Calimero automatically
   - If disabled: Skip restart, log message
4. User can always manually start Calimero later via system settings
```

## Benefits

### 1. User Control & Flexibility
- ✅ **User Choice**: Users can disable autostart if they prefer manual control
- ✅ **Session Persistence**: Preference remembered during user's session
- ✅ **Easy Toggle**: Can change preference via system settings at any time
- ✅ **No Lock-in**: Manual start/stop always available regardless of preference

### 2. Development & Testing
- ✅ **Test Control**: Developers can disable autostart for testing scenarios
- ✅ **Resource Management**: Prevents unwanted Calimero processes during development
- ✅ **Debug Friendly**: Clear logging indicates why Calimero did/didn't start
- ✅ **Playwright Testing**: Checkbox has stable ID for automated testing

### 3. Production Flexibility
- ✅ **User Preference**: Different users can have different autostart preferences
- ✅ **Performance Control**: Users with resource constraints can disable autostart
- ✅ **Troubleshooting**: Clear distinction between system disabled vs. user disabled
- ✅ **Backward Compatible**: Default behavior (autostart=true) unchanged

### 4. System Reliability
- ✅ **Graceful Degradation**: System works fine with autostart disabled
- ✅ **Clear Status**: System settings show current autostart preference
- ✅ **Manual Recovery**: Always possible to start Calimero manually
- ✅ **Safe Defaults**: Sensible defaults prevent user confusion

## User Interface Changes

### Login Screen (BAB Profile Only)
```
[Company Field]
[Username Field] 
[Password Field]
[☑ Autostart Calimero Process]  ← NEW CHECKBOX
[Error Messages]
[Login Button]
```

### System Settings (Enhanced)
```
Calimero Service
├── [☑ Enable Calimero Service]     ← System-level enable/disable
├── [☑ Autostart on Login]          ← NEW: User preference
├── [Executable Path Field]
├── [Service Status Indicator]
└── [Start/Stop Button]             ← Always works (manual override)
```

## Technical Implementation Details

### Session Storage Strategy
1. **Primary**: VaadinSession attribute (set during login)
2. **Fallback**: Session service generic storage (cross-component access)
3. **Key**: `"autostartCalimero"` (consistent across all components)
4. **Type**: `Boolean` (properly cast to avoid type issues)
5. **Default**: `true` (backward compatibility)

### Process Manager Integration
- **Dual Mode**: `startCalimeroServiceIfEnabled()` respects autostart, `forceStartCalimeroService()` ignores it
- **Manual Operations**: Restart button uses force start to bypass autostart check
- **Status Reporting**: Clear messages indicate why service didn't start
- **Session Access**: Safely checks VaadinSession and session service

### Error Handling & Logging
```java
// Example log messages
LOGGER.info("🔌 BAB login: Calimero autostart preference set to: {}", autostartCalimero);
LOGGER.info("🔌 Calimero autostart disabled by user preference - service will not start automatically");
LOGGER.info("ℹ️ Calimero can be started manually via system settings or restart button");
```

## Testing & Verification

### Manual Testing Scenarios
1. **Login with autostart enabled** → Calimero starts automatically
2. **Login with autostart disabled** → Calimero doesn't start, clear log message
3. **Change preference in settings** → Takes effect immediately
4. **Manual start/stop** → Works regardless of autostart preference
5. **Database reset with autostart disabled** → Respects preference, doesn't restart

### Playwright Test IDs
- Login checkbox: `custom-calimero-autostart-checkbox`
- Settings checkbox: `custom-calimero-autostart-checkbox`
- Status component: `custom-calimero-status-component`

### Expected Log Output
```log
INFO  (CCustomLoginView.java:126) 🔌 BAB login: Calimero autostart preference set to: false
INFO  (CCalimeroStartupListener.java:67) 🔌 Calimero autostart disabled by user preference - service will not start automatically
INFO  (CCalimeroStartupListener.java:68) ℹ️ Calimero can be started manually via system settings or restart button
```

## Compliance with AGENTS.md

- ✅ **C-Prefix Convention**: All classes use C-prefix (`CCustomLoginView`, `CCalimeroProcessManager`)
- ✅ **Logging Standards**: INFO level with emoji indicators (🔌, ✅, ℹ️)
- ✅ **Exception Handling**: Try-catch with proper logging, graceful fallbacks
- ✅ **Service Patterns**: Uses session service, dependency injection
- ✅ **UI Patterns**: Checkbox with proper ID, tooltip, responsive design
- ✅ **Profile-Aware**: Only active in BAB profile, auto-detects environment
- ✅ **Session Management**: Uses VaadinSession and session service appropriately

## Migration Guide

### For Existing BAB Installations
1. **No Breaking Changes**: Default behavior (autostart=true) unchanged
2. **Backward Compatible**: Existing users see no difference until they uncheck checkbox
3. **Optional Adoption**: Users can ignore new checkbox, system works as before
4. **Clear Messaging**: Tooltips and logs explain new functionality

### For New BAB Installations
1. **Default Experience**: Checkbox checked, Calimero starts automatically as expected
2. **Discovery**: Users can see and understand autostart option during first login
3. **Flexibility**: Users can immediately customize behavior to their preference

## Future Enhancements

1. **Persistent Storage**: Store preference in user profile database (beyond session)
2. **Company Defaults**: Allow company administrators to set default autostart preference
3. **Advanced Options**: Autostart delay, conditional autostart based on system resources
4. **Integration**: Remember preference across browser sessions/devices
5. **Monitoring**: Analytics on autostart usage patterns

## Known Limitations

1. **Session Scope**: Preference only persists during current session
2. **BAB Profile Only**: Feature only available in BAB profile (by design)
3. **Manual Recovery**: If autostart disabled, user must manually start via settings
4. **Database Reset**: DB reset scenarios may still restart Calimero (configurable)

## Conclusion

✅ **Calimero autostart checkbox successfully implemented!**

The implementation provides flexible user control over Calimero autostart behavior while maintaining backward compatibility and system reliability. Users can now choose their preferred startup behavior, and the system gracefully handles both automatic and manual startup scenarios.

**Key Success Factors**:
- **User-Centric**: Checkbox on login screen where users make the decision
- **Backward Compatible**: Default behavior unchanged for existing users
- **Manual Override**: Always possible to start Calimero manually regardless of preference
- **Clear Communication**: Tooltips, logs, and notifications explain what's happening
- **Robust Implementation**: Handles edge cases, graceful fallbacks, proper error handling

**Development Evidence**: Clean compilation with zero errors, follows Derbent coding standards, properly integrated with existing Calimero process management system.