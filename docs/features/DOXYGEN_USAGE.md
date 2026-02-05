# Using Doxygen for Gantt Chart API Documentation

## Overview

This guide explains how to use Doxygen to generate automated API documentation and call graphs for the Gantt chart implementation.

## What is Doxygen?

Doxygen is a documentation generator that creates:
- API documentation from code comments
- Class hierarchies and relationships
- Call graphs showing method dependencies
- Collaboration diagrams
- File dependency graphs

## When to Use Doxygen vs PlantUML

### Use PlantUML (Already Done) ✅
- **Design documentation**: Architecture patterns, flows, concepts
- **Custom diagrams**: Specific scenarios and workflows
- **Maintainable diagrams**: Version-controlled diagram-as-code
- **Documentation-first**: Diagrams created as part of documentation effort

**Status**: Complete PlantUML diagrams available in `docs/diagrams/`

### Use Doxygen (Optional)
- **API reference**: Automated documentation from code comments
- **Code browsing**: Clickable cross-references between classes
- **Automatic call graphs**: Generated from actual code
- **Code-first**: Documentation extracted from implementation

**Status**: Doxyfile provided, can be generated on demand

## Generating Doxygen Documentation

### Prerequisites

```bash
# Install Doxygen and Graphviz (for diagrams)
sudo apt-get install doxygen graphviz
```

### Generate Documentation

```bash
# From project root directory
doxygen Doxyfile.gantt
```

This creates documentation in `docs/doxygen-output/html/`

### View Documentation

```bash
# Open in browser
xdg-open docs/doxygen-output/html/index.html

# Or use a web server
cd docs/doxygen-output/html
python3 -m http.server 8000
# Then open http://localhost:8000 in browser
```

## What's Included

The Doxygen configuration (`Doxyfile.gantt`) includes:

### Source Files
- All Java files in `src/main/java/tech/derbent/app/gannt/`
- `CGridViewBaseGannt.java` base class
- `IGanttDisplayable.java` interface

### Generated Documentation
1. **Class Hierarchy**
   - Inheritance diagrams
   - Class relationships
   - Interface implementations

2. **Call Graphs**
   - Method call dependencies
   - Caller/callee relationships
   - Interactive navigation

3. **Collaboration Diagrams**
   - Class interactions
   - Member usage
   - Dependencies

4. **Source Code Browser**
   - Syntax-highlighted code
   - Cross-referenced symbols
   - Quick navigation

5. **File Documentation**
   - File dependencies
   - Include graphs
   - Directory structure

## Configuration Details

The `Doxyfile.gantt` is configured with:

```
PROJECT_NAME           = "Derbent Gantt Chart Implementation"
INPUT                  = src/main/java/tech/derbent/app/gannt \
                         src/main/java/tech/derbent/api/views/grids/CGridViewBaseGannt.java \
                         src/main/java/tech/derbent/api/interfaces/IGanttDisplayable.java
RECURSIVE              = YES
FILE_PATTERNS          = *.java

CALL_GRAPH             = YES  # Show method call relationships
CALLER_GRAPH           = YES  # Show who calls each method
UML_LOOK               = YES  # UML-style diagrams
```

## Enhancing Code for Doxygen

To get better Doxygen output, add JavaDoc comments to code:

### Class Documentation
```java
/**
 * @brief Data transfer object for Gantt chart representation of project items
 * 
 * This class wraps project items (CActivity, CMeeting) to provide a unified 
 * interface for Gantt chart display. It handles the dynamic database fetch
 * pattern when items are selected.
 * 
 * @see CActivity
 * @see CMeeting
 * @see IGanttDisplayable
 */
public class CGanttItem extends CEntityOfProject<CGanttItem> {
```

### Method Documentation
```java
/**
 * @brief Fetches the complete entity from database when Gantt item is selected
 * 
 * This is the core of the dynamic fetch pattern. When a user clicks a Gantt
 * item in the grid, this method is called to fetch the full entity with all
 * relationships from the database.
 * 
 * @param activityService Service for fetching Activity entities
 * @param meetingService Service for fetching Meeting entities
 * @return The complete entity loaded from database
 * @throws IllegalArgumentException if entity type is not supported
 * @throws NullPointerException if entity is not found in database
 */
public CProjectItem<?> getGanntItem(
    CEntityOfProjectService<?> activityService, 
    CEntityOfProjectService<?> meetingService
) {
```

## Comparison: PlantUML vs Doxygen

### PlantUML Advantages (What We Have) ✅
- Clean, maintainable diagrams
- Focus on design and patterns
- Easy to update and version control
- Shows intended architecture
- Great for documentation-first approach

### Doxygen Advantages (Optional Add-on)
- Always synchronized with code (auto-generated)
- Complete API reference
- Call graphs from actual implementation
- Code browser with cross-references
- Shows actual implementation details

## Recommendation

**Current Status**: ✅ PlantUML documentation is complete and sufficient

**Doxygen Addition**: Optional, provides complementary value:
- **When to add**: If team wants automated API docs or code browser
- **Frequency**: Regenerate when making significant code changes
- **Audience**: Developers diving deep into implementation

**Both Together**: 
- PlantUML for design patterns and architecture
- Doxygen for API reference and code navigation

## Integrating Both

If using both PlantUML and Doxygen:

1. **Design Phase**: Create PlantUML diagrams
   - Architecture patterns
   - Call flows
   - UI layouts

2. **Implementation Phase**: Write code with JavaDoc
   - Document classes and methods
   - Reference PlantUML diagrams in comments

3. **Documentation Phase**: Generate both
   - PlantUML: `plantuml -tpng docs/diagrams/*.puml`
   - Doxygen: `doxygen Doxyfile.gantt`

4. **Linking**: Reference each other
   - PlantUML docs link to Doxygen output
   - Doxygen comments reference PlantUML diagrams

## Automated Generation

### In CI/CD Pipeline

```yaml
# .github/workflows/documentation.yml
name: Generate Documentation

on:
  push:
    branches: [ main ]
    paths:
      - 'src/main/java/tech/derbent/app/gannt/**'
      - 'docs/diagrams/*.puml'

jobs:
  generate-docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Install tools
        run: |
          sudo apt-get update
          sudo apt-get install -y doxygen graphviz plantuml
      
      - name: Generate PlantUML diagrams
        run: |
          cd docs/diagrams
          plantuml -tpng *.puml
      
      - name: Generate Doxygen docs
        run: doxygen Doxyfile.gantt
      
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./docs/doxygen-output/html
```

### Pre-commit Hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

# Regenerate documentation if Java files changed
if git diff --cached --name-only | grep -q "src/main/java/tech/derbent/app/gannt/"; then
    echo "Gantt chart code modified, consider regenerating documentation:"
    echo "  plantuml -tpng docs/diagrams/*.puml"
    echo "  doxygen Doxyfile.gantt"
fi
```

## Directory Structure

After generating Doxygen documentation:

```
derbent/
├── Doxyfile.gantt              # Doxygen configuration
├── docs/
│   ├── diagrams/               # PlantUML diagrams
│   │   ├── *.puml             # Source files
│   │   └── *.png              # Generated images
│   ├── doxygen-output/         # Doxygen output
│   │   └── html/              # HTML documentation
│   │       ├── index.html     # Main page
│   │       ├── classes.html   # Class list
│   │       ├── files.html     # File list
│   │       └── ...
│   └── implementation/         # Design docs
│       ├── GANTT_INDEX.md
│       ├── GANTT_DESIGN_PATTERN.md
│       └── ...
```

## Troubleshooting

### Doxygen Not Generating Graphs
```bash
# Check if Graphviz is installed
which dot

# Install if missing
sudo apt-get install graphviz

# Verify HAVE_DOT=YES in Doxyfile.gantt
grep HAVE_DOT Doxyfile.gantt
```

### PlantUML Not Found
```bash
# Check PlantUML location
ls -la /usr/share/plantuml/plantuml.jar

# Update PLANTUML_JAR_PATH in Doxyfile.gantt if different
```

### Empty Call Graphs
```bash
# Make sure CALL_GRAPH and CALLER_GRAPH are YES
grep CALL_GRAPH Doxyfile.gantt

# Increase DOT_GRAPH_MAX_NODES if needed
grep DOT_GRAPH_MAX_NODES Doxyfile.gantt
```

## Maintenance

### When to Regenerate

**PlantUML Diagrams**:
- Architecture changes
- New design patterns
- UI layout modifications
- Flow changes

**Doxygen Documentation**:
- After significant code changes
- Before releases
- When API changes
- Monthly (if using automated docs)

### Keeping Synchronized

1. Update PlantUML diagrams when design changes
2. Update code comments when implementation changes
3. Regenerate Doxygen after JavaDoc updates
4. Review and update both quarterly

## Conclusion

**Current State**: ✅ Comprehensive PlantUML documentation complete

**Doxygen Option**: Available via `Doxyfile.gantt` for:
- Automated API reference
- Call graphs from code
- Code browsing

**Recommendation**: PlantUML is sufficient for most needs. Add Doxygen if:
- Team wants automated code documentation
- Need interactive API reference
- Want code browser with cross-references

Both tools complement each other and can be used together.
