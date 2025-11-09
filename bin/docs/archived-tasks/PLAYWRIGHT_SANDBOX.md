# Running Playwright Tests in Sandbox/CI Environments

## Issue Summary

Playwright tests fail in sandbox environments (like GitHub Copilot Workspace) because:

1. **StoredObject Libraries**: The SO libraries must be installed to the local Maven repository before running tests
2. **Playwright Browser Installation**: Playwright tries to download browsers which may fail in restricted environments

## Solutions

### 1. Install StoredObject Libraries (Required)

Before running any Maven command (including tests), install the SO libraries:

```bash
./install-so-libraries.sh
```

This installs:
- `so-components-14.0.7.jar`
- `so-charts-5.0.3.jar`
- `so-helper-5.0.1.jar`

To your local Maven repository (`~/.m2/repository/org/vaadin/addons/so/`).

### 2. Playwright Browser Dependencies

The sandbox environment has these browsers pre-installed:
- Chromium: `/usr/bin/chromium`
- Google Chrome: `/usr/bin/google-chrome`
- Firefox: `/usr/bin/firefox`

And all required system libraries:
- `libgbm1` (generic buffer management)
- `libasound2t64` (ALSA sound library)
- `libatk1.0-0t64` (accessibility toolkit)
- `libgtk-3-0t64` (GTK UI library)
- `libx11-6` (X11 client library)

However, Playwright's Java client tries to download its own browser binaries, which fails in restricted environments.

### 3. Running Tests

#### Option A: Skip Browser Tests (Fastest)

```bash
# Just verify build and dependencies
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
mvn clean compile
```

#### Option B: Run with System Browsers (If Configured)

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
export PLAYWRIGHT_BROWSERS_PATH=/usr

# Run tests
./run-playwright-tests.sh menu
```

Note: This may still fail if the system browser versions don't match Playwright's expectations.

#### Option C: Install Playwright Browsers Manually (If Possible)

```bash
# Try to install Playwright browsers
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

This will likely fail in sandbox due to download restrictions.

## Current Status

✅ **Build works**: Project compiles successfully with Java 21  
✅ **SO Libraries**: Installed via `install-so-libraries.sh`  
✅ **System Dependencies**: All required libraries present  
⚠️ **Playwright Tests**: Fail due to browser download restrictions in sandbox

## Recommendations for Local Development

1. Install SO libraries once: `./install-so-libraries.sh`
2. Let Playwright download browsers on first run (works in local dev)
3. Run tests normally: `./run-playwright-tests.sh menu`

## Recommendations for CI/Sandbox

1. Always run `./install-so-libraries.sh` in setup phase
2. Skip Playwright UI tests or use alternative testing:
   - Unit tests: `mvn test -Dtest='!**/*UI*'`
   - Integration tests without browser: `mvn verify -DskipUITests=true`
3. Run Playwright tests only in environments with unrestricted internet access

## Alternative: Docker

For consistent test execution, use Docker with Playwright pre-installed:

```dockerfile
FROM mcr.microsoft.com/playwright/java:v1.40.0-jammy
# Copy project and run tests
```
