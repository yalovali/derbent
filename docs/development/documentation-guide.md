# Documentation Guide

## Overview

This guide explains the organization and purpose of documentation in the Derbent project, helping you find the information you need quickly.

## Documentation Structure

```
derbent/
├── README.md                        # Project overview and quick start
├── LICENSE                          # MIT License
├── docs/                           # Main documentation directory
│   ├── architecture/               # Design patterns and standards
│   ├── development/                # Developer guides
│   ├── testing/                    # Testing documentation
│   ├── implementation/             # Feature implementation details
│   ├── features/                   # Feature documentation
│   ├── components/                 # Component documentation
│   ├── configuration/              # Configuration guides
│   ├── fixes/                      # Bug fix documentation
│   ├── diagrams/                   # Architecture diagrams
│   ├── archived-tasks/             # Older archived tasks
│   └── archive/                    # Historical documentation
│       └── tasks/                  # Task completion records
└── .github/
    └── copilot-instructions.md     # AI coding assistant instructions
```

## Documentation Categories

### 1. Root Level Documentation

#### README.md
**Purpose**: First point of contact for new users and contributors

**Contents**:
- Project vision and mission
- Key features and capabilities
- Quick start guide
- Technology stack overview
- Use cases
- Links to detailed documentation

**When to Update**: 
- Major feature additions
- Technology stack changes
- Setup process changes
- Project goals evolve

#### LICENSE
**Purpose**: Legal information (MIT License)

**When to Update**: Rarely (only if license terms change)

### 2. Architecture Documentation (`docs/architecture/`)

#### Purpose
Explains the design decisions, patterns, and standards that guide development.

#### Key Files

**`coding-standards.md`** (CRITICAL)
- C-prefix naming convention
- Type safety requirements
- Metadata-driven development
- Notification system patterns
- Field and method naming
- Package organization rules

**`entity-inheritance-patterns.md`**
- Base entity class hierarchy
- When to extend which base class
- Generic type parameters
- Repository patterns

**`service-layer-patterns.md`**
- Service class structure
- Transaction management
- Business logic organization
- Dependency injection patterns

**`view-layer-patterns.md`**
- View/Page base classes
- UI component patterns
- Form binding with CEnhancedBinder
- Grid configuration

**Other Files**:
- `bean-access-patterns.md` - Spring bean access
- `interface-hierarchy-before-after.md` - Interface refactoring
- `multi-user-singleton-advisory.md` - Multi-tenant patterns

#### When to Reference
- Before implementing new features
- When designing new modules
- During code reviews
- When onboarding new developers

### 3. Development Documentation (`docs/development/`)

#### Purpose
Practical guides for setting up, building, and working with the codebase.

#### Key Files

**`getting-started.md`**
- Environment setup
- First build
- Running the application
- Common issues and solutions

**`project-structure.md`**
- Package organization
- Module structure patterns
- File naming conventions
- Directory purposes

**`copilot-guidelines.md`**
- GitHub Copilot best practices
- AI-assisted development workflows
- Code generation patterns

**`ai-tools-guide.md`** (NEW)
- Configuration files for different AI tools
- Tool-specific setup
- Consistency across tools

**`environment-setup.md`** (NEW)
- Detailed environment configuration
- Java 21 setup
- Maven configuration
- Database setup

**`MULTI_USER_QUICK_REFERENCE.md`**
- Multi-tenant development
- Company-based isolation
- Session management

**`multi-user-development-checklist.md`**
- Checklist for multi-user features
- Common pitfalls
- Testing considerations

#### When to Reference
- Setting up development environment
- Learning project structure
- Understanding AI tool support
- Implementing multi-tenant features

### 4. Testing Documentation (`docs/testing/`)

#### Purpose
Testing strategies, guides, and test result archives.

#### Key Files

**`PLAYWRIGHT_TEST_SUMMARY.md`**
- Playwright test overview
- Test categories
- Running tests
- Screenshot verification

**`PLAYWRIGHT_USAGE.md`** (NEW)
- Detailed Playwright usage
- Writing new tests
- Test patterns

**`playwright-screenshots/`**
- Historical test screenshots
- Visual regression references

#### When to Reference
- Writing new tests
- Debugging test failures
- Understanding UI testing strategy
- Reviewing test coverage

### 5. Implementation Documentation (`docs/implementation/`)

#### Purpose
Detailed documentation of specific feature implementations.

#### Typical Contents
- Feature design documents
- Implementation approaches
- API documentation
- Integration guides

#### When to Reference
- Understanding specific features
- Implementing similar features
- Debugging feature-specific issues

### 6. Archive Documentation (`docs/archive/`)

#### Purpose
Preserves historical documentation without cluttering main directories.

#### Contents

**`tasks/`** (NEW)
- Task completion summaries
- Implementation summaries
- Test run reports
- Screenshot summaries
- Historical development records

**`README.md`** (NEW)
- Explains archive purpose
- How to use archived docs
- Reference to current docs

#### When to Reference
- Understanding project history
- Researching past decisions
- Finding context for old code
- Learning from previous implementations

**Note**: Archive docs may be outdated. Always check current documentation first.

### 7. Other Documentation Directories

#### `docs/features/`
Feature-specific documentation

#### `docs/components/`
UI component documentation

#### `docs/configuration/`
Configuration guides and examples

#### `docs/fixes/`
Bug fix documentation and workarounds

#### `docs/diagrams/`
Architecture and workflow diagrams

#### `docs/archived-tasks/`
Older task archives (pre-reorganization)

## AI Tool Configuration Files

Located in repository root for automatic discovery by AI tools:

### `.github/copilot-instructions.md`
**For**: GitHub Copilot
**Size**: 468 lines (most comprehensive)
**Contains**: Complete development guide including environment setup, build procedures, validation scenarios, and troubleshooting

### `.cursorrules`
**For**: Cursor IDE
**Size**: ~100 lines (quick reference)
**Contains**: Essential coding rules, build commands, common patterns

### `.clinerules`
**For**: Cline AI Assistant
**Size**: ~200 lines (detailed)
**Contains**: Mandatory rules, build commands, validation checklist, examples

### `.aidigestconfig`
**For**: AI Digest and general AI tools
**Size**: ~70 lines (structured)
**Contains**: Project metadata, tech stack, key patterns, documentation priorities

See `docs/development/ai-tools-guide.md` for complete AI configuration documentation.

## Finding Information

### By Topic

| Topic | Primary Location | Additional References |
|-------|-----------------|----------------------|
| **Setup** | `docs/development/getting-started.md` | `.github/copilot-instructions.md` |
| **Coding Rules** | `docs/architecture/coding-standards.md` | AI config files |
| **Package Structure** | `docs/development/project-structure.md` | `coding-standards.md` |
| **Entity Design** | `docs/architecture/entity-inheritance-patterns.md` | `service-layer-patterns.md` |
| **UI Development** | `docs/architecture/view-layer-patterns.md` | `components/` |
| **Testing** | `docs/testing/PLAYWRIGHT_TEST_SUMMARY.md` | `copilot-guidelines.md` |
| **AI Tools** | `docs/development/ai-tools-guide.md` | Root config files |
| **Multi-Tenancy** | `docs/development/MULTI_USER_QUICK_REFERENCE.md` | `multi-user-development-checklist.md` |
| **Build Issues** | `.github/copilot-instructions.md` | `getting-started.md` |

### By Role

#### New Developer
1. `README.md` - Project overview
2. `docs/development/getting-started.md` - Setup
3. `docs/architecture/coding-standards.md` - Rules
4. `docs/development/project-structure.md` - Organization

#### Feature Developer
1. `docs/architecture/coding-standards.md` - Standards
2. `docs/architecture/entity-inheritance-patterns.md` - Entity design
3. `docs/architecture/service-layer-patterns.md` - Business logic
4. `docs/architecture/view-layer-patterns.md` - UI development

#### Test Writer
1. `docs/testing/PLAYWRIGHT_TEST_SUMMARY.md` - Test overview
2. `docs/testing/PLAYWRIGHT_USAGE.md` - Test patterns
3. `docs/development/copilot-guidelines.md` - Testing workflows

#### AI Tool User
1. Root config files (`.cursorrules`, `.clinerules`, etc.)
2. `docs/development/ai-tools-guide.md` - Tool configuration
3. `.github/copilot-instructions.md` - Complete guide

## Documentation Best Practices

### When Writing Documentation

1. **Choose the Right Location**
   - Architecture decisions → `docs/architecture/`
   - How-to guides → `docs/development/`
   - Test documentation → `docs/testing/`
   - Feature specs → `docs/implementation/` or `docs/features/`
   - Historical records → `docs/archive/`

2. **Use Clear Structure**
   - Start with overview/purpose
   - Use headers for navigation
   - Include code examples
   - Link to related docs

3. **Keep It Current**
   - Update docs when code changes
   - Mark outdated sections clearly
   - Archive old docs rather than deleting

4. **Cross-Reference**
   - Link between related documents
   - Reference from code comments
   - Include in AI config files

### When Reading Documentation

1. **Start with Current Docs**
   - Check main `docs/` directories first
   - Archives are for historical context only

2. **Check Multiple Sources**
   - AI config files for quick reference
   - Architecture docs for deep understanding
   - Implementation docs for specific features

3. **Verify Accuracy**
   - Compare with actual code
   - Check commit dates
   - Test examples before using

## Maintaining Documentation

### Regular Updates

**Monthly**:
- Review AI config files for accuracy
- Check for broken links
- Update getting-started guide if needed

**Per Feature**:
- Document new patterns in architecture/
- Update project-structure.md if packages change
- Add implementation details to implementation/
- Update AI config files with new rules

**Per Release**:
- Update README.md with new features
- Archive old task documentation
- Review and update all coding standards
- Verify all documentation links work

### Quality Checks

```bash
# Find all documentation
find docs -name "*.md"

# Check for broken internal links (manual review)
grep -r "\[.*\](.*\.md)" docs/

# Find outdated references to old structure
grep -r "abstracts/" docs/  # Old package name

# Verify AI configs are consistent
grep "C-prefix" .cursorrules .clinerules .aidigestconfig .github/copilot-instructions.md
```

## Common Documentation Tasks

### Adding New Architecture Pattern
1. Create file in `docs/architecture/`
2. Document the pattern with examples
3. Update `coding-standards.md` to reference it
4. Add to AI config files if mandatory
5. Update this guide's "Architecture Documentation" section

### Documenting New Feature
1. Create file in `docs/implementation/` or `docs/features/`
2. Explain purpose, design, and usage
3. Include code examples
4. Link from README.md if user-facing
5. Update `project-structure.md` if new packages added

### Archiving Documentation
1. Move file to `docs/archive/tasks/`
2. Update `docs/archive/README.md` with description
3. Add note in original location pointing to archive (if needed)
4. Remove from navigation/index if present

### Updating AI Configuration
1. Identify what needs to change
2. Update `.github/copilot-instructions.md` (source of truth)
3. Update `.cursorrules`, `.clinerules`, `.aidigestconfig` consistently
4. Test with actual AI tools
5. Update `docs/development/ai-tools-guide.md`

## Questions?

- **Can't find something?** Check this guide's "Finding Information" section
- **Documentation outdated?** Check if there's a newer version in main docs/
- **Need to add documentation?** Follow "Documentation Best Practices"
- **AI tool issues?** See `docs/development/ai-tools-guide.md`

## Related Resources

- **AI Tools**: `docs/development/ai-tools-guide.md`
- **Project Structure**: `docs/development/project-structure.md`
- **Getting Started**: `docs/development/getting-started.md`
- **Coding Standards**: `docs/architecture/coding-standards.md`
