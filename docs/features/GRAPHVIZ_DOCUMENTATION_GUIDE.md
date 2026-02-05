# 📚 Comprehensive Graphviz Documentation Guide for Derbent

## 🎯 Overview

This guide provides complete instructions for generating, viewing, and maintaining automated code documentation for the Derbent project management system using **Doxygen** and **Graphviz**.

### What You'll Get

- 📊 **Class Diagrams**: Visual representation of inheritance hierarchies
- 🔗 **Collaboration Diagrams**: How classes work together
- 📞 **Call Graphs**: Method call dependencies (who calls what)
- 📈 **Caller Graphs**: Reverse dependencies (who is called by what)
- 🗂️ **Directory Graphs**: File and directory organization
- 📖 **API Documentation**: Searchable HTML documentation with cross-references
- 🎨 **Interactive Diagrams**: Clickable SVG diagrams for navigation

### Documentation Scope

- **Full Project**: All 386+ Java files across 95+ packages
- **Core Abstractions**: Base classes, interfaces, and utilities
- **Application Modules**: Activities, projects, meetings, users, workflow, etc.
- **UI Components**: Vaadin views, dialogs, and components
- **Services**: Business logic and data access layers

---

## 🚀 Quick Start

### 1. Install Prerequisites

```bash
# On Ubuntu/Debian
sudo apt-get update
sudo apt-get install doxygen graphviz

# On macOS
brew install doxygen graphviz

# On Windows (with Chocolatey)
choco install doxygen.install graphviz

# Optional: PlantUML for enhanced diagrams
sudo apt-get install plantuml
```

### 2. Generate Documentation

```bash
# Generate full project documentation
./scripts/generate-graphviz-docs.sh

# Generate with browser auto-open
./scripts/generate-graphviz-docs.sh --open

# Clean previous docs and regenerate
./scripts/generate-graphviz-docs.sh --clean

# Generate only Gantt chart documentation
./scripts/generate-graphviz-docs.sh --gantt
```

### 3. View Documentation

```bash
# Open in your default browser
xdg-open docs/graphviz-output/html/index.html

# Or manually navigate to:
# docs/graphviz-output/html/index.html
```

---

## 📖 Detailed Usage

### Script Options

#### `generate-graphviz-docs.sh`

```bash
Usage: ./scripts/generate-graphviz-docs.sh [options]

Options:
  --full       Generate full project documentation (default)
  --gantt      Generate only Gantt chart documentation
  --clean      Clean generated documentation before building
  --open       Open documentation in browser after generation
  --help       Show help message

Examples:
  # Basic generation
  ./scripts/generate-graphviz-docs.sh
  
  # Clean and regenerate
  ./scripts/generate-graphviz-docs.sh --clean --open
  
  # Gantt charts only
  ./scripts/generate-graphviz-docs.sh --gantt
```

#### `update-graphviz-config.sh`

```bash
Usage: ./scripts/update-graphviz-config.sh [--full | --gantt]

Interactive menu for:
  1) View current configuration
  2) Edit configuration in text editor
  3) Update specific setting
  4) Common configuration tasks
  5) Show configuration guide
  6) Validate configuration
  7) Switch between full/gantt configs
  8) Exit

Examples:
  # Edit full project configuration
  ./scripts/update-graphviz-config.sh --full
  
  # Edit Gantt configuration
  ./scripts/update-graphviz-config.sh --gantt
```

---

## ⚙️ Configuration

### Configuration Files

- **`Doxyfile`**: Full project documentation configuration
- **`Doxyfile.gantt`**: Gantt chart-specific documentation configuration

### Editing Configuration

#### Method 1: Interactive Script (Recommended)

```bash
./scripts/update-graphviz-config.sh
```

This provides a menu-driven interface for common tasks.

#### Method 2: Direct Edit

```bash
# Edit full project config
nano Doxyfile

# Edit Gantt config
nano Doxyfile.gantt
```

### Key Configuration Options

#### Project Information

```ini
PROJECT_NAME           = "Derbent Project Management System"
PROJECT_NUMBER         = "1.0"
PROJECT_BRIEF          = "Java Spring Boot + Vaadin PM application"
OUTPUT_DIRECTORY       = docs/graphviz-output
```

#### Source Files

```ini
INPUT                  = src/main/java/tech/derbent \
                         README.md
RECURSIVE              = YES
FILE_PATTERNS          = *.java *.md
EXCLUDE                = src/test */bin/* */target/*
```

#### Diagram Generation

```ini
HAVE_DOT               = YES          # Enable Graphviz
CLASS_GRAPH            = YES          # Class inheritance diagrams
COLLABORATION_GRAPH    = YES          # Class collaboration diagrams
CALL_GRAPH             = YES          # Method call graphs
CALLER_GRAPH           = YES          # Reverse call graphs
GRAPHICAL_HIERARCHY    = YES          # Visual class hierarchy
DIRECTORY_GRAPH        = YES          # Directory structure
UML_LOOK               = YES          # UML-style notation
```

#### Graph Complexity

```ini
DOT_GRAPH_MAX_NODES    = 100          # Max nodes per graph (0 = unlimited)
MAX_DOT_GRAPH_DEPTH    = 0            # Max depth (0 = unlimited)
DOT_IMAGE_FORMAT       = png          # Output format (png, svg, etc.)
INTERACTIVE_SVG        = YES          # Clickable SVG diagrams
```

#### Output Formats

```ini
GENERATE_HTML          = YES          # HTML documentation
GENERATE_LATEX         = NO           # LaTeX/PDF documentation
GENERATE_XML           = NO           # XML output
GENERATE_TREEVIEW      = YES          # Tree navigation view
```

---

## 📋 Common Configuration Tasks

### Enable/Disable Call Graphs

Call graphs show which methods call which other methods. They can be large for complex codebases.

```bash
# Using the interactive script
./scripts/update-graphviz-config.sh
# Select: 4) Common configuration tasks
# Select: 1) Enable/Disable call graphs

# Or edit directly
nano Doxyfile
# Find and modify:
CALL_GRAPH             = YES    # or NO
CALLER_GRAPH           = YES    # or NO
```

### Adjust Graph Complexity

For large projects, limit graph size to improve generation speed:

```bash
# Edit configuration
nano Doxyfile

# Adjust these values:
DOT_GRAPH_MAX_NODES    = 50     # Smaller graphs (default: 100)
MAX_DOT_GRAPH_DEPTH    = 3      # Limit depth (default: 0 = unlimited)
```

### Change Output Directory

```bash
# Edit configuration
nano Doxyfile

# Change:
OUTPUT_DIRECTORY       = docs/my-custom-output
```

### Add/Remove Source Directories

```bash
# Edit configuration
nano Doxyfile

# Add to INPUT:
INPUT                  = src/main/java/tech/derbent \
                         src/main/java/additional/package \
                         README.md

# Add to EXCLUDE:
EXCLUDE                = src/test \
                         */bin/* \
                         */target/* \
                         */archived/*
```

### Enable PDF Output

```bash
# Edit configuration
nano Doxyfile

# Change:
GENERATE_LATEX         = YES

# Then after running doxygen:
cd docs/graphviz-output/latex
make pdf
```

---

## 📊 Understanding the Output

### Documentation Structure

```
docs/graphviz-output/
├── html/
│   ├── index.html              # Main entry point
│   ├── annotated.html          # Class list
│   ├── hierarchy.html          # Class hierarchy
│   ├── files.html              # File list
│   ├── namespaces.html         # Package/namespace list
│   ├── classes/                # Individual class pages
│   ├── graphs/                 # Generated diagrams
│   └── search/                 # Search functionality
├── latex/                      # LaTeX output (if enabled)
└── doxygen-warnings.log        # Warnings and errors
```

### Navigation Tips

1. **Start at `index.html`**: Main page with project overview
2. **Class List**: Click "Classes" → "Class List" to see all classes
3. **Class Hierarchy**: Click "Classes" → "Class Hierarchy" for inheritance tree
4. **Search**: Use the search box in the top-right corner
5. **Graphs**: Look for diagram icons on class pages

### Types of Diagrams

#### Class Diagram
- Shows class inheritance hierarchy
- Displays member variables and methods
- Color-coded by type (class, interface, abstract)

#### Collaboration Diagram
- Shows which classes are used by a class
- Displays member relationships
- Helps understand dependencies

#### Call Graph
- Shows which methods call this method
- Forward call dependencies
- Useful for understanding execution flow

#### Caller Graph
- Shows which methods are called by this method
- Reverse call dependencies
- Useful for impact analysis

---

## 🎯 Use Cases

### 1. Understanding Class Hierarchies

**Scenario**: You need to understand how the entity classes are organized.

**Steps**:
1. Generate documentation: `./scripts/generate-graphviz-docs.sh`
2. Open `docs/graphviz-output/html/hierarchy.html`
3. Navigate through the inheritance tree
4. Click on `CEntityDB` to see all entities that extend it

### 2. Finding Who Calls a Method

**Scenario**: You want to know where `CActivity.save()` is called from.

**Steps**:
1. Generate documentation with call graphs enabled
2. Open documentation and search for `CActivity`
3. Find the `save()` method
4. Look at the **Caller Graph** diagram
5. See all methods that call `save()`

### 3. Understanding Module Dependencies

**Scenario**: You want to see how the activities module relates to other modules.

**Steps**:
1. Generate documentation: `./scripts/generate-graphviz-docs.sh`
2. Open `docs/graphviz-output/html/files.html`
3. Navigate to `src/main/java/tech/derbent/app/activities/`
4. View the directory graph
5. See which other modules it depends on

### 4. Onboarding New Developers

**Scenario**: A new developer needs to understand the codebase structure.

**Steps**:
1. Share the generated documentation URL
2. Start with the main page for project overview
3. Review the class hierarchy for architecture understanding
4. Explore specific modules relevant to their work
5. Use search to find specific classes or methods

### 5. Code Review and Refactoring

**Scenario**: You're refactoring a service and need to understand its impact.

**Steps**:
1. Generate documentation with call graphs
2. Find the service class in documentation
3. Review the caller graph to see what depends on it
4. Check collaboration diagram to see what it uses
5. Plan refactoring with full understanding of dependencies

---

## 🔧 Troubleshooting

### Issue: "Graphviz not found"

**Solution**:
```bash
# Check if installed
which dot

# If not installed
sudo apt-get install graphviz

# Verify installation
dot -V
```

### Issue: "Doxygen not found"

**Solution**:
```bash
# Check if installed
which doxygen

# If not installed
sudo apt-get install doxygen

# Verify installation
doxygen --version
```

### Issue: No diagrams generated

**Check**:
1. Verify `HAVE_DOT = YES` in Doxyfile
2. Verify Graphviz is installed: `which dot`
3. Check warnings log: `cat docs/graphviz-output/doxygen-warnings.log`

**Solution**:
```bash
# Verify configuration
./scripts/update-graphviz-config.sh
# Select: 6) Validate configuration

# If HAVE_DOT is NO, enable it
nano Doxyfile
# Change: HAVE_DOT = YES

# Regenerate
./scripts/generate-graphviz-docs.sh --clean
```

### Issue: Graphs are too large/complex

**Solution**:
```bash
# Edit configuration
nano Doxyfile

# Reduce complexity:
DOT_GRAPH_MAX_NODES    = 50     # Fewer nodes
MAX_DOT_GRAPH_DEPTH    = 3      # Limited depth

# Disable some graph types:
CALL_GRAPH             = NO     # Disable call graphs
COLLABORATION_GRAPH    = NO     # Disable collaboration graphs

# Regenerate
./scripts/generate-graphviz-docs.sh --clean
```

### Issue: Generation is very slow

**Causes**:
- Large codebase (386+ files)
- Call graphs enabled (expensive)
- Unlimited graph depth

**Solutions**:
```bash
# 1. Disable call graphs temporarily
CALL_GRAPH             = NO
CALLER_GRAPH           = NO

# 2. Limit graph complexity
DOT_GRAPH_MAX_NODES    = 50
MAX_DOT_GRAPH_DEPTH    = 2

# 3. Use more threads (if available)
DOT_NUM_THREADS        = 8      # Use 8 threads

# 4. Generate only specific modules
INPUT                  = src/main/java/tech/derbent/app/activities
```

### Issue: Out of memory during generation

**Solution**:
```bash
# Reduce graph complexity
DOT_GRAPH_MAX_NODES    = 30
COLLABORATION_GRAPH    = NO
CALL_GRAPH             = NO

# Or increase JVM memory (if using Java tools)
export JAVA_OPTS="-Xmx2g"
```

---

## 🎨 Advanced Features

### PlantUML Integration

Doxygen can use PlantUML for enhanced diagrams:

```bash
# Install PlantUML
sudo apt-get install plantuml

# Configure in Doxyfile
PLANTUML_JAR_PATH      = /usr/share/plantuml/plantuml.jar
PLANTUML_INCLUDE_PATH  = docs/diagrams

# PlantUML diagrams in your Java files:
/**
 * @startuml
 * class CActivity {
 *   +save()
 *   +delete()
 * }
 * @enduml
 */
```

### Interactive SVG Diagrams

Enable clickable, zoomable SVG diagrams:

```ini
INTERACTIVE_SVG        = YES
DOT_IMAGE_FORMAT       = svg
```

Benefits:
- Clickable nodes navigate to class pages
- Zoom in/out on complex diagrams
- Better quality for large diagrams

### Markdown Integration

Include markdown files in documentation:

```ini
INPUT                  = src/main/java \
                         README.md \
                         docs/architecture/*.md

FILE_PATTERNS          = *.java *.md
USE_MDFILE_AS_MAINPAGE = README.md
```

### Custom Styling

Create custom CSS for documentation:

```bash
# Create custom CSS
cat > custom.css << 'EOF'
body {
    font-family: 'Roboto', sans-serif;
}
h1 {
    color: #2c3e50;
}
EOF

# Configure in Doxyfile
HTML_EXTRA_STYLESHEET  = custom.css
```

---

## 🔄 Maintenance

### When to Regenerate Documentation

**Always Regenerate**:
- After significant code changes
- Before major releases
- After refactoring
- When onboarding new team members

**Consider Regenerating**:
- Weekly during active development
- After adding new modules
- When architecture changes
- Before code reviews

### Automating Documentation Generation

#### Git Pre-commit Hook

```bash
# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# Regenerate docs if Java files changed

if git diff --cached --name-only | grep -q "src/main/java"; then
    echo "Java files modified, consider regenerating documentation:"
    echo "  ./scripts/generate-graphviz-docs.sh"
fi
EOF

chmod +x .git/hooks/pre-commit
```

#### Scheduled Regeneration

```bash
# Add to crontab for weekly regeneration
crontab -e

# Add line:
0 2 * * 0 cd /path/to/derbent && ./scripts/generate-graphviz-docs.sh --clean
```

#### CI/CD Integration

```yaml
# .github/workflows/documentation.yml
name: Generate Documentation

on:
  push:
    branches: [ main ]
    paths:
      - 'src/main/java/**'

jobs:
  generate-docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Install dependencies
        run: |
          sudo apt-get update
          sudo apt-get install -y doxygen graphviz
      
      - name: Generate documentation
        run: ./scripts/generate-graphviz-docs.sh --clean
      
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./docs/graphviz-output/html
```

### Keeping Configuration Updated

```bash
# Regular maintenance checklist
./scripts/update-graphviz-config.sh

1. Validate configuration
2. Check for new packages to include
3. Update excluded directories
4. Adjust graph complexity as needed
5. Review and update project information
```

---

## 📚 Documentation Best Practices

### Writing Good Javadoc

Doxygen extracts documentation from JavaDoc comments:

```java
/**
 * @brief Activity entity representing a project task
 * 
 * This class extends CEntityOfProject and provides comprehensive
 * task management functionality including status tracking, time
 * estimation, and user assignment.
 * 
 * @see CProject
 * @see CUser
 * @see CWorkflow
 * 
 * @author Derbent Team
 * @version 1.0
 * @since 2024-01-01
 */
public class CActivity extends CEntityOfProject<CActivity> {
    
    /**
     * @brief Saves the activity to the database
     * 
     * This method performs validation, updates timestamps,
     * and persists the activity. It triggers workflow status
     * updates if the status has changed.
     * 
     * @param validateOnly If true, only validates without saving
     * @return The saved activity instance
     * @throws ValidationException if validation fails
     * @throws OptimisticLockException if concurrent modification detected
     */
    public CActivity save(boolean validateOnly) {
        // Implementation
    }
}
```

### Organizing Documentation

1. **Main Page**: Use README.md as the entry point
2. **Module Pages**: Create module overview files
3. **Architecture**: Link to architecture documentation
4. **Examples**: Include code examples in comments
5. **Cross-references**: Use `@see` tags to link related classes

---

## 🔍 Comparison: Doxygen vs JavaDoc vs PlantUML

### Doxygen
- ✅ **Pros**: Automatic graphs, call trees, visual navigation
- ✅ **Best for**: Exploring codebases, understanding architecture
- ❌ **Cons**: Requires external tools, can be slow

### JavaDoc (Standard)
- ✅ **Pros**: Java standard, IDE support, simple
- ✅ **Best for**: API reference, library documentation
- ❌ **Cons**: No visual diagrams, limited navigation

### PlantUML
- ✅ **Pros**: Beautiful diagrams, design documentation
- ✅ **Best for**: Architecture documentation, design docs
- ❌ **Cons**: Manual maintenance, not code-driven

### Recommendation for Derbent

**Use All Three Together**:

1. **JavaDoc**: Basic API documentation in code
2. **PlantUML**: Design and architecture diagrams (`docs/diagrams/`)
3. **Doxygen**: Generated comprehensive documentation with graphs

**Workflow**:
1. Write JavaDoc comments in code
2. Create PlantUML diagrams for architecture
3. Generate Doxygen docs automatically
4. Link everything together

---

## 📞 Getting Help

### Resources

- **Doxygen Manual**: https://www.doxygen.nl/manual/
- **Graphviz Documentation**: https://graphviz.org/documentation/
- **This Guide**: `docs/GRAPHVIZ_DOCUMENTATION_GUIDE.md`
- **Configuration Help**: `./scripts/update-graphviz-config.sh` → "5) Show configuration guide"

### Common Questions

**Q: How long does generation take?**
A: For Derbent (386 files): ~2-5 minutes with all graphs enabled, ~30-60 seconds without call graphs.

**Q: How much disk space is needed?**
A: Typical full documentation: 50-150 MB depending on graph settings.

**Q: Can I generate docs for a specific module?**
A: Yes, edit `INPUT` in Doxyfile to specify only certain directories.

**Q: How do I share documentation with the team?**
A: Host on a web server, commit to GitHub Pages, or share the HTML directory.

**Q: Can I customize the appearance?**
A: Yes, use `HTML_EXTRA_STYLESHEET` to add custom CSS.

---

## 🎯 Quick Reference

### Essential Commands

```bash
# Generate documentation
./scripts/generate-graphviz-docs.sh

# Edit configuration
./scripts/update-graphviz-config.sh

# Clean and regenerate
./scripts/generate-graphviz-docs.sh --clean

# Generate and open
./scripts/generate-graphviz-docs.sh --open

# View documentation
xdg-open docs/graphviz-output/html/index.html

# Check warnings
cat docs/graphviz-output/doxygen-warnings.log
```

### Key Files

- **`Doxyfile`**: Full project configuration
- **`Doxyfile.gantt`**: Gantt-specific configuration
- **`docs/graphviz-output/html/index.html`**: Main documentation
- **`docs/graphviz-output/doxygen-warnings.log`**: Generation warnings

### Configuration Locations

- **Project info**: Lines 10-15 in Doxyfile
- **Source files**: Lines 80-100 in Doxyfile
- **Diagrams**: Lines 230-250 in Doxyfile
- **Output format**: Lines 130-150 in Doxyfile

---

## 📝 Summary

You now have a complete Graphviz documentation system for Derbent that provides:

✅ Automated documentation generation  
✅ Visual class diagrams and hierarchies  
✅ Call graphs and dependency analysis  
✅ Interactive HTML documentation  
✅ Easy-to-use scripts and configuration tools  
✅ Comprehensive troubleshooting guides  

**Next Steps**:

1. Install prerequisites: `sudo apt-get install doxygen graphviz`
2. Generate documentation: `./scripts/generate-graphviz-docs.sh`
3. Open and explore: `xdg-open docs/graphviz-output/html/index.html`
4. Customize as needed: `./scripts/update-graphviz-config.sh`

Happy documenting! 🚀
