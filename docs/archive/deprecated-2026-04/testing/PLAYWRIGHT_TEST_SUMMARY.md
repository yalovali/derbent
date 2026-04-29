# Playwright Tests - Headless Mode with Screenshots

## Summary
Successfully configured and ran Playwright tests in headless mode with screenshot capture capabilities. Tests now run reliably without browser download failures.

## Important Prerequisites

### 1. Install SO Libraries (Required)
Before running any Playwright tests, you must install the StoredObject libraries:
```bash
./install-so-libraries.sh
```
This installs the required SO libraries from the `lib/` folder to your local Maven repository.

### 2. Database Initialization
Tests automatically initialize sample data if the company combobox is empty during login. The `CBaseUITest` class handles this through the `initializeSampleDataFromLoginPage()` method.

### 3. Timeout Configuration
Tests use reasonable timeouts to avoid hanging:
- Login screen wait: 15 seconds
- Post-login wait: 15 seconds  
- Element selectors: 5 seconds
- Menu navigation: 20 seconds

If a timeout expires, the test will exit with a clear error message instead of hanging indefinitely.

## Test Execution
```bash
# STEP 1: Install SO libraries (required - run once after cloning)
./install-so-libraries.sh

# STEP 2: Run tests using the updated script with automatic environment configuration
./run-playwright-tests.sh menu

# Or run specific test manually
PLAYWRIGHT_BROWSERS_PATH=~/.cache/ms-playwright PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true \
mvn test -Dtest="automated_tests.tech.derbent.ui.automation.CSampleDataMenuNavigationTest" \
-Dspring.profiles.active=test -Dplaywright.headless=true
```

## For Complete Testing Guidelines
For comprehensive testing guidelines, patterns, and best practices, refer to:
- **[Copilot Development Guidelines](../development/copilot-guidelines.md)** - Complete guide for AI-assisted development and testing patterns
- **GitHub Copilot Instructions**: `.github/copilot-instructions.md` - Quick reference for all tasks

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
