# Derbent Environment Setup Guide

This guide explains the environment setup for running Derbent in any environment, including GitHub Copilot workspace.

## Prerequisites

- **Java 21** (REQUIRED - pom.xml specifies Java 21)
- **Maven 3.9+**
- **Git**

## Quick Start

### 1. Setup Java 21 Environment

```bash
# Source the Java environment setup script
source ./setup-java-env.sh
```

This script automatically:
- Detects Java 21 installation on the system
- Configures `JAVA_HOME` and `PATH` environment variables
- Verifies Java version

### 2. Install StoredObject Libraries

```bash
# Install SO libraries to local Maven repository (one-time setup)
./install-so-libraries.sh
```

This installs:
- `so-components-14.0.7.jar`
- `so-charts-5.0.3.jar`
- `so-helper-5.0.1.jar`

**Note**: The `lib/` folder is excluded from git (`.gitignore`) but the JAR files should be present locally after cloning or can be obtained separately.

### 3. Verify Environment

```bash
# Run environment verification
./verify-environment.sh
```

This checks:
- ✅ Java 21 is configured
- ✅ Maven is available
- ✅ SO libraries are installed
- ✅ Project compiles successfully

## Running the Application

```bash
# Development mode with H2 database
source ./setup-java-env.sh
mvn spring-boot:run -Dspring.profiles.active=h2
```

Application will be available at: http://localhost:8080

## Running Playwright Tests

```bash
# Run basic menu navigation test
./run-playwright-tests.sh menu

# Run all tests
./run-playwright-tests.sh all

# Run specific test category
./run-playwright-tests.sh comprehensive
```

All test scripts automatically source `setup-java-env.sh` for Java 21 configuration.

## For GitHub Copilot Users

The `.github/copilot-instructions.md` file contains comprehensive guidelines that:
- Automatically configure the environment for Copilot tasks
- Provide detailed build and test instructions
- Include coding standards and best practices
- Document the Playwright testing infrastructure

## Troubleshooting

### Java Version Issues

If you see "release version 21 not supported" error:
```bash
# Ensure Java 21 is sourced
source ./setup-java-env.sh
java -version  # Should show Java 21
```

### SO Libraries Not Found

If you see compilation errors about SO libraries:
```bash
# Reinstall SO libraries
./install-so-libraries.sh

# Verify installation
ls -la ~/.m2/repository/org/vaadin/addons/so/
```

### Playwright Browser Issues

If Playwright tests fail to find browsers:
```bash
# The test scripts automatically install browsers
# But you can manually install if needed
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

## File Structure Changes (2024-11)

### Cleaned Up
- **53 task-specific documentation files** → Moved to `docs/archived-tasks/`
- **7 debug/demo scripts** → Moved to `scripts/archived/`
- **Duplicate playwright-java.jar** → Removed (available via Maven)
- **lib/ folder** → Excluded from git (install locally with `install-so-libraries.sh`)

### Added
- `setup-java-env.sh` - Java 21 environment configuration
- `verify-environment.sh` - Quick environment verification
- Updated `.github/copilot-instructions.md` - Java 21 requirements

### Key Files
- `run-playwright-tests.sh` - Main Playwright test runner (auto-configures Java 21)
- `run-all-playwright-tests.sh` - Comprehensive test runner
- `run-playwright-visible-h2.sh` - Visual test runner with H2
- `install-so-libraries.sh` - SO library installer

## Summary

All scripts and configurations have been updated to ensure Java 21 is used consistently. The environment setup is now automatic via `setup-java-env.sh`, making it easier to work in different environments including GitHub Copilot workspace.
