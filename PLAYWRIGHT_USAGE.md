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
