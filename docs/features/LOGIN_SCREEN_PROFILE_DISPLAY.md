# Login Screen Active Profile Display Feature

**Date**: 2026-01-15  
**Feature**: Display active Spring profile(s) on login screen

## Overview

The login screen now displays the active Spring profile(s) alongside the default login credentials, helping developers and testers quickly identify which environment configuration is running.

## Implementation

### Location
`src/main/java/tech/derbent/base/login/view/CCustomLoginView.java`

### Code Changes
```java
// Get active profile(s) to display
String activeProfiles = String.join(", ", environment.getActiveProfiles());
if (activeProfiles.isEmpty()) {
    activeProfiles = "default";
}

final Paragraph passwordHint = new Paragraph("Default: admin/test123 | Profile: " + activeProfiles);
passwordHint.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
passwordHint.setWidthFull();
```

## Display Examples

### Single Profile
```
Default: admin/test123 | Profile: h2
Default: admin/test123 | Profile: test
Default: admin/test123 | Profile: prod
Default: admin/test123 | Profile: bab
```

### Multiple Profiles
```
Default: admin/test123 | Profile: test, h2
Default: admin/test123 | Profile: prod, postgresql
```

### No Profile Specified
```
Default: admin/test123 | Profile: default
```

## Benefits

### For Developers
- ✅ **Instant Environment Identification**: Immediately see which configuration is active
- ✅ **Prevents Configuration Errors**: Reduce mistakes from running wrong profile
- ✅ **Debugging Aid**: Easier to troubleshoot environment-specific issues
- ✅ **Multi-Profile Support**: See all active profiles when multiple are enabled

### For Testers
- ✅ **Test Environment Verification**: Confirm testing against correct environment
- ✅ **Screenshot Documentation**: Profile visible in test screenshots
- ✅ **Playwright Test Validation**: Automated tests can verify profile
- ✅ **Issue Reporting**: Profile information included in bug reports

### For DevOps
- ✅ **Deployment Verification**: Quickly verify correct profile after deployment
- ✅ **Configuration Audit**: Visual confirmation of active configuration
- ✅ **Environment Separation**: Clear distinction between dev/test/prod
- ✅ **Troubleshooting**: First step in diagnosing configuration issues

## Usage Scenarios

### Development
```bash
# Start with H2 in-memory database
mvn spring-boot:run -Dspring.profiles.active=h2
# Login screen shows: "Default: admin/test123 | Profile: h2"
```

### Testing
```bash
# Start with test profile
mvn spring-boot:run -Dspring.profiles.active=test
# Login screen shows: "Default: admin/test123 | Profile: test"
```

### Production
```bash
# Start with production profile
mvn spring-boot:run -Dspring.profiles.active=prod
# Login screen shows: "Default: admin/test123 | Profile: prod"
```

### BAB Gateway
```bash
# Start with BAB profile
mvn spring-boot:run -Dspring.profiles.active=bab
# Login screen shows: "Default: admin/test123 | Profile: bab"
```

### Multiple Profiles
```bash
# Start with multiple profiles
mvn spring-boot:run -Dspring.profiles.active=test,h2
# Login screen shows: "Default: admin/test123 | Profile: test, h2"
```

## Technical Details

### Profile Detection
- Uses Spring's `Environment.getActiveProfiles()` method
- Returns String array of active profile names
- Automatically handles multiple profiles with comma separation
- Falls back to "default" when no profiles are active

### Display Location
- Positioned below password field
- Same line as login button (right-aligned)
- Secondary text color (gray)
- Small font size (Lumo utility class)

### Visual Styling
```java
passwordHint.addClassNames(
    LumoUtility.TextColor.SECONDARY,  // Gray text
    LumoUtility.FontSize.SMALL        // Small font
);
passwordHint.setWidthFull();          // Full width
```

## Integration with Existing Features

### Schema Selector
The profile display complements the existing schema selector:
- Schema selector: Allows switching between "Derbent" and "BAB Gateway" schemas
- Profile display: Shows which Spring profile is active
- Both provide environment configuration visibility

### Database Reset Buttons
Profile helps identify which database will be reset:
- `h2` profile → Resets H2 in-memory database
- `test` profile → Resets test database
- `prod` profile → Should not show reset buttons (configured separately)

### Playwright Tests
Tests can now verify correct profile is active:
```java
@Test
void testLoginScreenShowsCorrectProfile() {
    loginPage.navigate();
    String hint = page.locator(".lumo-secondary.lumo-small").textContent();
    assertTrue(hint.contains("Profile: test"), 
        "Expected test profile but got: " + hint);
}
```

## Configuration

### Profile Configuration Files
```
application.properties              # Default configuration
application-h2.properties          # H2 profile configuration
application-test.properties        # Test profile configuration
application-prod.properties        # Production profile configuration
application-bab.properties         # BAB Gateway profile configuration
```

### Active Profile Priority
1. Command line: `-Dspring.profiles.active=h2`
2. Environment variable: `SPRING_PROFILES_ACTIVE=h2`
3. application.properties: `spring.profiles.active=h2`
4. Default: No profile (shows "default")

## Future Enhancements

### Possible Improvements
1. **Color Coding**: Different colors for different profiles (e.g., red for prod)
2. **Icon Indicators**: Add icons to represent profile type
3. **Tooltip**: Hover to see full profile configuration details
4. **Profile Description**: Show friendly name instead of technical profile name
5. **Warning Messages**: Special warning for production profile

### Example Color Coding
```java
String color = switch(activeProfiles) {
    case "prod" -> "error";      // Red for production
    case "test" -> "success";    // Green for test
    case "h2" -> "primary";      // Blue for development
    default -> "secondary";      // Gray for default
};
passwordHint.addClassNames(LumoUtility.TextColor.valueOf(color.toUpperCase()));
```

## Related Documentation

- **Login View**: `src/main/java/tech/derbent/base/login/view/CCustomLoginView.java`
- **Profile Configuration**: `src/main/resources/application-*.properties`
- **Environment Setup**: `.github/copilot-instructions.md`
- **Testing Guide**: `docs/testing/`

## Testing

### Manual Testing
1. Start application: `mvn spring-boot:run -Dspring.profiles.active=h2`
2. Navigate to: http://localhost:8080/login
3. Verify text shows: "Default: admin/test123 | Profile: h2"
4. Repeat with different profiles

### Automated Testing
```java
@Test
void testProfileDisplayOnLoginScreen() {
    page.navigate("http://localhost:8080/login");
    Locator hint = page.locator("p").filter(
        new Locator.FilterOptions().setHasText("Profile:")
    );
    assertTrue(hint.isVisible());
    assertTrue(hint.textContent().matches(".*Profile: \\w+.*"));
}
```

## Troubleshooting

### Profile Not Showing
**Problem**: Login screen shows "Profile: default" instead of expected profile

**Solutions**:
1. Verify profile is specified: `-Dspring.profiles.active=h2`
2. Check application logs for active profile confirmation
3. Ensure profile configuration file exists: `application-h2.properties`
4. Restart application after changing profile

### Multiple Profiles Showing
**Problem**: Shows "Profile: derbent, default" when expecting single profile

**Solution**: This is normal behavior when Spring automatically adds profiles. The application has a default "derbent" profile configured.

### Profile Name Too Long
**Problem**: Text wraps or gets truncated with many profiles

**Solution**: Consider limiting to first 2-3 profiles or use tooltip for full list

## Summary

The active profile display feature provides immediate visual feedback about the application's runtime configuration, enhancing developer experience and reducing configuration errors. The implementation is simple, non-intrusive, and integrates seamlessly with the existing login screen design.

---

**Status**: ✅ **IMPLEMENTED**  
**Commit**: d4b2298b  
**Last Updated**: 2026-01-15
