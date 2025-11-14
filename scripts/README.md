# 📜 Derbent Scripts Directory

This directory contains automation scripts for the Derbent project.

## 📚 Documentation Generation Scripts

### `generate-graphviz-docs.sh`
Main script for generating comprehensive code documentation using Doxygen and Graphviz.

**Usage**:
```bash
./scripts/generate-graphviz-docs.sh [options]

Options:
  --full       Generate full project documentation (default)
  --gantt      Generate only Gantt chart documentation
  --clean      Clean previous documentation before building
  --open       Open documentation in browser after generation
  --help       Show help message
```

**Prerequisites**:
- doxygen
- graphviz

**Output**:
- Full project: `docs/graphviz-output/html/index.html`
- Gantt only: `docs/doxygen-output/html/index.html`

### `update-graphviz-config.sh`
Interactive helper for editing Doxygen configuration files.

**Usage**:
```bash
./scripts/update-graphviz-config.sh [--full | --gantt]
```

**Features**:
- View current configuration
- Edit in text editor
- Update specific settings
- Common configuration tasks
- Configuration validation
- Built-in guide

## 📖 Documentation

For complete documentation on using these scripts, see:
- **[Graphviz Documentation Guide](../docs/GRAPHVIZ_DOCUMENTATION_GUIDE.md)** - Comprehensive usage guide

## 🚀 Quick Start

```bash
# 1. Install prerequisites
sudo apt-get install doxygen graphviz

# 2. Generate documentation
./scripts/generate-graphviz-docs.sh --open

# 3. Configure if needed
./scripts/update-graphviz-config.sh
```

## 📂 Other Script Directories

- `archived/` - Archived historical scripts
- `codex-prep/` - Scripts for specific tooling preparation
