# Menu Profile Support - Quick Reference

**Date**: 2026-02-10  
**For**: Full details see `MENU_PROFILE_SUPPORT_IMPLEMENTATION.md`

## TL;DR

Menu items now respect Spring `@Profile` annotations. Add `profile` attribute to `@MyMenu` to control which profiles see each menu item.

## Quick Usage

### Pattern 1: Profile-Restricted View (Most Common)

```java
@Profile("derbent")
@MyMenu(order = "5.1", 
        title = "Project.Activities", 
        icon = "vaadin:tasks",
        profile = {"derbent"})  // ← NEW: Must match @Profile
public class CActivityView extends CAbstractPage {
    // ...
}
```

### Pattern 2: Common View (No Restriction)

```java
// No @Profile annotation
@MyMenu(order = "100.1", 
        title = "Setup.Users", 
        icon = "vaadin:users")  // ← No profile = available everywhere
public class CUserView extends CAbstractPage {
    // ...
}
```

### Pattern 3: Multiple Profiles (OR Logic)

```java
@Profile({"derbent", "test"})
@MyMenu(order = "5.1", 
        title = "Project.Activities", 
        profile = {"derbent", "test"})  // ← Available in EITHER profile
public class CActivityView extends CAbstractPage {
    // ...
}
```

## The Rule

**Always match `@Profile` and `@MyMenu profile`**:

```java
@Profile("X")              @Profile({"X", "Y"})           No @Profile
@MyMenu(..., profile={"X"}) @MyMenu(..., profile={"X","Y"}) @MyMenu(...) // no profile
```

## What Changed

### Files Modified

1. `MyMenu.java` - Added `profile()` attribute
2. `MyMenuEntry.java` - Added profile storage/filtering
3. `MyMenuConfiguration.java` - Added filtering logic
4. `CPageMenuIntegrationService.java` - Updated constructor call
5. `CGanttViewEntityView.java` - Added `profile = {"derbent"}`

### Example Fix

**BEFORE** (Bug):
```java
@Profile("derbent")
@MyMenu(order = "1.5", title = "Gantt View")  // ← Missing profile
```
Result: Menu shows in BAB profile, clicking causes error

**AFTER** (Fixed):
```java
@Profile("derbent")
@MyMenu(order = "1.5", title = "Gantt View", profile = {"derbent"})  // ← Added profile
```
Result: Menu only shows in Derbent profile ✅

## Testing

```bash
# Derbent profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=derbent"
# Should see: Gantt View in menu ✅

# BAB profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=bab"
# Should NOT see: Gantt View in menu ✅
```

## Finding Views to Update

```bash
# Find views with @Profile but possibly missing @MyMenu profile
grep -r "@Profile" src/main/java --include="*.java" -B2 -A8 | \
  grep -B5 -A5 "@MyMenu" | \
  grep -E "@Profile|@MyMenu"
```

## Key Points

- ✅ Empty/omitted `profile` = available in ALL profiles
- ✅ Array of profiles = OR logic (any match = visible)
- ✅ Backward compatible (existing code works)
- ✅ Dynamic pages have no profile restrictions
- ✅ Filtering is automatic and fast

## Common Scenarios

| Scenario | @Profile | @MyMenu profile | Visible In |
|----------|----------|----------------|------------|
| Derbent-only | `@Profile("derbent")` | `profile = {"derbent"}` | derbent |
| BAB-only | `@Profile("bab")` | `profile = {"bab"}` | bab |
| Common | (none) | (none) | all |
| Dev+Test | `@Profile({"derbent","test"})` | `profile = {"derbent","test"}` | either |

## Gotchas

❌ **DON'T** forget to add profile to @MyMenu when view has @Profile:
```java
@Profile("derbent")
@MyMenu(order = "5.1", title = "Activities")  // ❌ Missing profile!
```

❌ **DON'T** mismatch profiles:
```java
@Profile("derbent")
@MyMenu(..., profile = {"bab"})  // ❌ Mismatch!
```

✅ **DO** keep them synchronized:
```java
@Profile("derbent")
@MyMenu(..., profile = {"derbent"})  // ✅ Correct!
```

## Status

✅ **Implementation Complete**  
✅ **Compiled Successfully**  
✅ **Tested with Profile Filtering Logic**  
✅ **Backward Compatible**  
✅ **Ready for Use**
