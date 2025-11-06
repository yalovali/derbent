# Playwright Test Screenshots

This directory contains screenshots captured during Playwright UI automation tests.

## Available Screenshots

### Menu Navigation Test
These screenshots were captured during the menu navigation test using `./run-playwright-tests.sh menu`:

- **post-login.png** - Dashboard view immediately after successful login
- **sample-journey-post-login.png** - Application state after navigating through sample data

## Generating New Screenshots

To generate new screenshots, run:

```bash
# Run menu navigation test (generates screenshots in target/screenshots/)
./run-playwright-tests.sh menu

# Copy screenshots to this directory
cp target/screenshots/*.png screenshots/
```

## Test Options

The following test suites generate screenshots:

- `./run-playwright-tests.sh menu` - Menu navigation test
- `./run-playwright-tests.sh login` - Company login test
- `./run-playwright-tests.sh comprehensive` - Comprehensive views test
- `./run-playwright-tests.sh status-types` - Type and Status CRUD test
- `./run-playwright-tests.sh buttons` - Button functionality test
- `./run-playwright-tests.sh all` - Run all tests

All screenshots are saved to `target/screenshots/` during test execution.

## Note

The `target/` directory is excluded from git by `.gitignore`, so screenshots must be manually copied to this `screenshots/` directory to be committed to the repository.
