# Playwright Test Screenshots

This directory contains screenshots captured during Playwright UI automation tests of the Derbent project management application.

## Available Screenshots

### Current Screenshots
- **post-login.png** (67K) - Dashboard view immediately after successful login (H2 database)
- **sample-journey-post-login.png** (82K) - Application state after navigating through sample data
- **post-login-20251012-headless-postgres.png** (1.0M) - Post-login view captured with PostgreSQL database in headless mode

## Generating New Screenshots

### Prerequisites
Before running tests, ensure you have:
1. Java 21 environment configured: `source ./setup-java-env.sh`

### Running Tests

To generate new screenshots:

```bash
# Run menu navigation test (generates screenshots in target/screenshots/)
./run-playwright-tests.sh menu

# Copy screenshots to this directory for version control
cp target/screenshots/*.png screenshots/
```

### Available Test Suites

All test suites generate screenshots in headless mode:

- `./run-playwright-tests.sh menu` - Menu navigation test (default)
- `./run-playwright-tests.sh login` - Company-aware login pattern test
- `./run-playwright-tests.sh comprehensive` - Comprehensive dynamic views test (2-5 minutes)
- `./run-playwright-tests.sh status-types` - Type and Status CRUD operations test
- `./run-playwright-tests.sh buttons` - Button functionality test across all pages
- `./run-playwright-tests.sh all` - Run all tests sequentially

## Test Configuration

Tests run with the following configuration:
- **Browser:** Chromium (Playwright build)
- **Mode:** Headless
- **Resolution:** 1280 x 720
- **Database:** H2 (in-memory) for testing
- **Screenshot Format:** PNG with RGBA color

## Documentation

For complete testing guidelines and patterns, see:
- **[Playwright Test Summary](../docs/testing/PLAYWRIGHT_TEST_SUMMARY.md)** - Test configuration and results
- **[Copilot Development Guidelines](../docs/development/copilot-guidelines.md)** - Complete testing patterns
- **Test Script Usage:** Run `./run-playwright-tests.sh help` for all options

## Notes

- The `target/` directory is excluded from git by `.gitignore`
- Screenshots must be manually copied from `target/screenshots/` to this directory to be committed
- Tests automatically initialize sample data if the database is empty
- Original screenshots remain in `target/screenshots/` after test execution
