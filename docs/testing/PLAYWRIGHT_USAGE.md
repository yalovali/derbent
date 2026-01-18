# Playwright Test Execution Guide

## Quick Start

The unified script `run-playwright-tests.sh` now supports visible browser and console output control.

### Default Behavior (Recommended for Development)
```bash
./run-playwright-tests.sh menu
# Runs with: Visible Browser + Console Output
```

### Environment Variables

- **PLAYWRIGHT_HEADLESS**: Control browser visibility
  - `false` (default) - Browser window is visible
  - `true` - Browser runs in headless mode (no window)

- **PLAYWRIGHT_SHOW_CONSOLE**: Control console output
  - `true` (default) - Full Maven and test output shown
  - `false` - Output suppressed (quiet mode)

## Usage Examples

### Interactive Testing (Watch the Browser)
```bash
./run-playwright-tests.sh menu
# or explicitly:
PLAYWRIGHT_HEADLESS=false PLAYWRIGHT_SHOW_CONSOLE=true ./run-playwright-tests.sh menu
```

### Silent Testing (No Browser, No Output)
```bash
PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SHOW_CONSOLE=false ./run-playwright-tests.sh menu
```

### Headless with Console Output (CI/CD Mode)
```bash
PLAYWRIGHT_HEADLESS=true ./run-playwright-tests.sh menu
```

### Visible Browser without Console Clutter
```bash
PLAYWRIGHT_SHOW_CONSOLE=false ./run-playwright-tests.sh menu
```

## Available Test Suites

| Command | Description |
|---------|-------------|
| `menu` (default) | Sample data menu navigation test |
| `login` | Company-aware login pattern test |
| `comprehensive` | Complete navigation and CRUD operations |
| `status-types` | Type and Status CRUD operations |
| `buttons` | Button functionality across all pages |
| `all` | Run all test suites sequentially |
| `clean` | Clean test artifacts |
| `install` | Install Playwright browsers |
| `help` | Show usage information |

## Coverage Result Sheets

The comprehensive suite writes coverage metrics for each page to:
- `test-results/playwright/coverage/page-coverage-<timestamp>.csv`
- `test-results/playwright/coverage/page-coverage-<timestamp>.md`

These reports include visited pages, grid/CRUD/kanban presence, and CRUD button availability.

## Sample Data Initialization

Playwright login checks the company selector. If there are no company options, it triggers the login-page "DB Full" reset to repopulate sample data.
This guard bypasses the in-memory initialization flag when H2 has been recreated between test classes. Force a reset via:
`-Dplaywright.forceSampleReload=true`

## JaCoCo Coverage (Playwright Runs)

Playwright runs now generate JaCoCo coverage reports during `mvn test`:
- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`

## Common Workflows

### Development Workflow
```bash
# Make UI changes
# Test with visible browser to see what happens
./run-playwright-tests.sh menu

# Check screenshots
ls -lh target/screenshots/
```

### Pre-Commit Testing
```bash
# Run all tests quickly in headless mode
PLAYWRIGHT_HEADLESS=true ./run-playwright-tests.sh all
```

### Debugging Failed Tests
```bash
# Run with visible browser and full output
./run-playwright-tests.sh [test-suite]

# Check generated screenshots
ls -lh target/screenshots/
```

### Focused Rerun Rule (Mandatory)

When the comprehensive suite fails and you need to edit code, rerun the suite
by filtering to the edited page using a route/title substring. This reduces the
feedback loop while keeping the same test coverage path.

Use `test.routeKeyword` to filter pages:
```bash
MAVEN_OPTS="-Dtest.routeKeyword=comments" ./run-playwright-tests.sh comprehensive
```

### Reporting Rule (Mandatory)

When running Playwright tests, always capture and report a concise error summary that includes:
- The failing test class and scenario.
- The first meaningful browser console error (if any).
- The first meaningful server-side exception or fail-fast message (if any).

## Migration from Old Scripts

**Removed scripts** (functionality now in `run-playwright-tests.sh`):
- ❌ `run-playwright-visible-h2.sh` 
- ❌ `run-playwright-visible-postgres.sh`
- ❌ `run-all-playwright-tests.sh`
- ❌ `run-playwright-tests.bat`

**Migration guide**:
```bash
# Old: run-playwright-visible-h2.sh
# New: ./run-playwright-tests.sh menu

# Old: run-all-playwright-tests.sh  
# New: ./run-playwright-tests.sh all

# Old: run-playwright-tests.sh (was headless only)
# New: Same script, now visible by default
```

## Troubleshooting

### Browser Not Opening
```bash
# Check if headless mode is accidentally enabled
echo $PLAYWRIGHT_HEADLESS  # Should be empty or 'false'

# Explicitly set to visible mode
PLAYWRIGHT_HEADLESS=false ./run-playwright-tests.sh menu
```

### Too Much Console Output
```bash
# Suppress console output
PLAYWRIGHT_SHOW_CONSOLE=false ./run-playwright-tests.sh menu
```

### Need to See Everything
```bash
# Full visibility (browser + console)
PLAYWRIGHT_HEADLESS=false PLAYWRIGHT_SHOW_CONSOLE=true ./run-playwright-tests.sh menu
```
