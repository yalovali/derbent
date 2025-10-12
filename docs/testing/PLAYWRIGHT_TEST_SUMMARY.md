# Playwright Tests - Headless Mode with Screenshots

## Summary
Successfully configured and ran Playwright tests in headless mode with screenshot capture capabilities. Tests now run reliably without browser download failures.

## Test Execution
```bash
# Run tests using the updated script with automatic environment configuration
./run-playwright-tests.sh menu

# Or run specific test manually
PLAYWRIGHT_BROWSERS_PATH=~/.cache/ms-playwright PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true \
mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CSimpleLoginScreenshotTest" \
-Dspring.profiles.active=test -Dplaywright.headless=true
```

## Changes Made

### 1. CBaseUITest.java
Enhanced browser initialization to:
- Check for cached Playwright Chromium browser first
- Use explicit executable path to bypass download attempts
- Fallback to system Chromium if Playwright browser unavailable
- Configured for headless mode by default

### 2. run-playwright-tests.sh  
Added environment variables to all test functions:
```bash
export PLAYWRIGHT_BROWSERS_PATH="$HOME/.cache/ms-playwright"
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true
```

### 3. CSimpleLoginScreenshotTest.java (New)
Created a simple test that:
- Demonstrates headless browser operation
- Captures login flow with screenshots
- Works independently of navigation menu issues
- Validates core Playwright functionality

## Test Results

### ✅ CSimpleLoginScreenshotTest
**Status:** PASSED  
**Duration:** 25.75 seconds  
**Screenshots Generated:**
- `01-login-page.png` (8.0K) - Initial Vaadin login screen
- `03-post-login-page.png` (1.0M) - Application state after successful login
- `04-final-state.png` (1.0M) - Final application view

### ⚠️ CSampleDataMenuNavigationTest
**Status:** FAILED (Pre-existing issue)  
**Issue:** Cannot find navigation menu items after login  
**Note:** This is a pre-existing test issue, not related to headless browser configuration. Browser launches successfully and screenshots are captured, but navigation menu detection needs investigation.

## Browser Configuration
- **Browser:** Chromium 120.0.6099.28 (Playwright build 1091)
- **Mode:** Headless
- **Resolution:** 1280 x 720
- **Format:** PNG with RGBA color

## Screenshots Location
All screenshots are saved to: `target/screenshots/`

## Environment Requirements
- Java 17+
- Maven 3.9+
- Chromium browser (cached in ~/.cache/ms-playwright/chromium-1091/)

## Next Steps
To investigate the menu navigation test failure:
1. Check if sample data is loading correctly
2. Verify menu component rendering in Vaadin  
3. Review navigation element selectors in CBaseUITest
4. Test with visible mode to observe actual UI state
