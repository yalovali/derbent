# AI Tools Configuration Guide

## Overview

Derbent provides configuration files for multiple AI coding assistants to ensure consistent development experience regardless of which tool you use.

## Available Configuration Files

### 1. GitHub Copilot (Primary)
**File**: `.github/copilot-instructions.md` (468 lines)

**Purpose**: Comprehensive guidelines for GitHub Copilot

**Coverage**:
- Complete environment setup instructions
- Build and test procedures with timing expectations
- Validation scenarios and checklists
- UI testing with Playwright
- Architecture patterns and coding standards
- Common failures and solutions
- Repository-specific custom instructions

**When to Use**: When using GitHub Copilot in VS Code, IntelliJ, or other supported IDEs

### 2. Cursor IDE
**File**: `.cursorrules`

**Purpose**: Quick reference for Cursor IDE AI assistant

**Coverage**:
- Essential setup commands
- Mandatory C-prefix convention
- Notification system patterns
- Build commands with timing
- Package structure rules
- Common mistakes to avoid

**When to Use**: When using Cursor IDE as your primary editor

### 3. Cline (Previously Claude Dev)
**File**: `.clinerules`

**Purpose**: Detailed rules for Cline AI assistant

**Coverage**:
- Environment setup (critical first step)
- All mandatory coding rules
- Complete build & test commands
- Project structure overview
- Common patterns for new features
- Validation checklist
- Links to documentation

**When to Use**: When using Cline extension in VS Code

### 4. AI Digest / General AI Tools
**File**: `.aidigestconfig`

**Purpose**: Universal configuration for various AI tools

**Coverage**:
- Project type and tech stack
- Critical rules summary
- Build commands
- Key file locations
- Directory structure
- Documentation priority

**When to Use**: For AI tools that don't have specific config files, or for AI Digest tool

## File Locations Reference

All AI configuration files are in the repository root:
```
derbent/
├── .github/
│   └── copilot-instructions.md    # GitHub Copilot (COMPLETE)
├── .cursorrules                    # Cursor IDE
├── .clinerules                     # Cline AI Assistant
└── .aidigestconfig                 # AI Digest & general
```

## Configuration File Discovery

Different AI tools look for configuration in standard locations:

| Tool | Primary Config | Fallback |
|------|---------------|----------|
| GitHub Copilot | `.github/copilot-instructions.md` | Repository context |
| Cursor | `.cursorrules` | User settings |
| Cline | `.clinerules` | System prompt |
| AI Digest | `.aidigestconfig` | Project structure |
| Windsurf | `.windsurfrules` | Project files |
| Continue | `.continuerules` | Configuration |

## Consistency Across Tools

All configuration files enforce the same core rules:

### 1. C-Prefix Convention (MANDATORY)
```java
✅ CActivity, CUser, CProject, CActivityService
❌ Activity, User, Project, ActivityService
```

### 2. Package Structure
```
tech.derbent.{module}.{layer}
```

### 3. Notification System
```java
// Use this
notificationService.showSuccess("Message");
CNotifications.showError("Message");

// NOT this
Notification.show("Message");
```

### 4. Environment Setup
```bash
source ./setup-java-env.sh  # ALWAYS FIRST
```

### 5. Code Formatting
```bash
mvn spotless:apply  # Before every commit
```

## Choosing the Right Config File

### For New Developers
Start with: `.github/copilot-instructions.md`
- Most comprehensive (468 lines)
- Covers all edge cases
- Includes validation scenarios
- Build timing expectations
- Troubleshooting guide

### For Quick Reference
Use: `.cursorrules` or `.clinerules`
- Condensed essential rules
- Quick command reference
- Common patterns
- Validation checklist

### For AI Tool Integration
Use: `.aidigestconfig`
- Machine-readable format
- Tech stack definition
- Key patterns summary
- Priority documentation links

## Maintaining Configuration Files

### When to Update

Update ALL config files when:
1. Core coding rules change
2. New mandatory patterns introduced
3. Build process changes
4. Package structure reorganized
5. Critical dependencies updated

### Consistency Check

Verify consistency across files:
```bash
# Check all files mention C-prefix rule
grep -l "C-prefix\|CActivity" .cursorrules .clinerules .aidigestconfig .github/copilot-instructions.md

# Check all files mention notification pattern
grep -l "CNotificationService\|notificationService" .cursorrules .clinerules .aidigestconfig .github/copilot-instructions.md

# Check all files mention environment setup
grep -l "setup-java-env" .cursorrules .clinerules .aidigestconfig .github/copilot-instructions.md
```

## Testing AI Configuration

### Validation Steps

1. **Environment Setup Test**
   - Verify all configs mention `source ./setup-java-env.sh`
   - Check Java 21 requirement

2. **Coding Rules Test**
   - Verify C-prefix convention in all files
   - Check notification pattern rules
   - Confirm package structure format

3. **Build Commands Test**
   - Verify mvn commands are correct
   - Check timing expectations match reality
   - Confirm test script references

4. **Documentation Links Test**
   - Verify all file paths are correct
   - Check documentation references exist

## Adding Support for New AI Tools

To add support for a new AI tool:

1. **Research Tool's Config Location**
   - Check tool documentation for config file names
   - Identify expected format (markdown, YAML, JSON)

2. **Create Config File**
   - Use `.clinerules` as template for markdown format
   - Use `.aidigestconfig` as template for structured format

3. **Include Essential Rules**
   - C-prefix convention (MANDATORY)
   - Notification pattern (MANDATORY)
   - Environment setup (CRITICAL)
   - Package structure
   - Build commands

4. **Test Configuration**
   - Open project with the new tool
   - Verify tool reads configuration
   - Test that rules are being followed

5. **Document in This Guide**
   - Add to "Available Configuration Files" section
   - Update "File Locations Reference" table
   - Add to "Consistency Check" validation

## Common Issues

### AI Not Following Rules

**Symptom**: AI suggests code without C-prefix or uses Notification.show()

**Solutions**:
1. Verify config file exists in root directory
2. Check file is not in .gitignore
3. Restart IDE/tool to reload configuration
4. Explicitly reference the config file in prompts

### Config File Conflicts

**Symptom**: Different AI tools give conflicting advice

**Solutions**:
1. Review all config files for consistency
2. Use `.github/copilot-instructions.md` as source of truth
3. Update conflicting files to match
4. Run consistency check commands

### Outdated Configuration

**Symptom**: Config references old commands or patterns

**Solutions**:
1. Compare with current codebase
2. Update all config files simultaneously
3. Verify with actual build/test commands
4. Document update in commit message

## Best Practices

1. **Single Source of Truth**
   - `.github/copilot-instructions.md` is the authoritative reference
   - Other configs should be consistent with it

2. **Keep Configs Synchronized**
   - Update all files when changing core rules
   - Use same examples across files
   - Maintain consistent terminology

3. **Test Configuration Changes**
   - Verify with actual AI tools
   - Check that rules are enforced
   - Validate all referenced files exist

4. **Document Tool-Specific Features**
   - Note which config is for which tool
   - Explain when to use each file
   - Provide tool-specific troubleshooting

## Related Documentation

- **Coding Standards**: `docs/architecture/coding-standards.md`
- **Project Structure**: `docs/development/project-structure.md`
- **Getting Started**: `docs/development/getting-started.md`
- **Copilot Guidelines**: `docs/development/copilot-guidelines.md`

## Questions?

For questions about:
- **AI tool configuration**: Review this guide
- **Coding standards**: See `docs/architecture/coding-standards.md`
- **Setup issues**: See `docs/development/getting-started.md`
- **Build problems**: See `.github/copilot-instructions.md`
