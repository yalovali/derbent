# 🚀 Graphviz Documentation Quick Start Examples

This file contains practical examples and common workflows for generating documentation.

## 📋 Basic Workflows

### 1. First Time Setup

```bash
# Step 1: Install prerequisites
sudo apt-get update
sudo apt-get install doxygen graphviz

# Optional: Install PlantUML for enhanced diagrams
sudo apt-get install plantuml

# Step 2: Verify installation
doxygen --version
dot -V

# Step 3: Generate documentation
cd /path/to/derbent
./scripts/generate-graphviz-docs.sh --open

# Documentation will open automatically in your browser
# Location: docs/graphviz-output/html/index.html
```

### 2. Regular Update Workflow

```bash
# Clean previous documentation and regenerate
./scripts/generate-graphviz-docs.sh --clean

# Generate specific documentation
./scripts/generate-graphviz-docs.sh --gantt  # Gantt charts only

# Generate with auto-open
./scripts/generate-graphviz-docs.sh --clean --open
```

### 3. Configuration Updates

```bash
# Interactive configuration editor
./scripts/update-graphviz-config.sh

# Direct editing
nano Doxyfile                  # Full project config
nano Doxyfile.gantt           # Gantt config
```

## 🎯 Common Use Cases

### Use Case 1: Understanding a New Module

**Scenario**: You're new to the project and need to understand the Activities module.

```bash
# 1. Generate full documentation
./scripts/generate-graphviz-docs.sh

# 2. Open documentation
xdg-open docs/graphviz-output/html/index.html

# 3. Navigation path:
#    - Click "Classes" → "Class List"
#    - Search for "CActivity"
#    - View class diagram to see inheritance
#    - View collaboration diagram to see relationships
#    - Check call graphs to understand method flow
```

### Use Case 2: Finding All Usages of a Method

**Scenario**: You need to refactor `CActivity.save()` and want to see everywhere it's called.

```bash
# 1. Generate with call graphs enabled (default)
./scripts/generate-graphviz-docs.sh

# 2. Open documentation and navigate:
#    - Search for "CActivity"
#    - Find the save() method
#    - Look at the "Caller Graph" diagram
#    - See all methods that call save()
```

### Use Case 3: Architecture Review

**Scenario**: You need to review the overall architecture for a code review.

```bash
# 1. Generate complete documentation
./scripts/generate-graphviz-docs.sh --clean

# 2. Key pages to review:
#    - Main page: Project overview
#    - Class Hierarchy: Overall structure
#    - Directory Graph: Module organization
#    - Individual class pages: Specific implementations
```

### Use Case 4: Onboarding Documentation

**Scenario**: Create documentation package for new team members.

```bash
# 1. Generate comprehensive docs
./scripts/generate-graphviz-docs.sh --clean

# 2. Package for sharing
cd docs/graphviz-output
tar -czf derbent-docs-$(date +%Y%m%d).tar.gz html/

# 3. Share the archive or host on web server
python3 -m http.server 8000
# New team members can access at: http://your-server:8000
```

## ⚙️ Configuration Examples

### Example 1: Disable Call Graphs (Faster Generation)

Call graphs are expensive to generate. Disable for faster builds:

```bash
# Edit configuration
nano Doxyfile

# Change these lines:
CALL_GRAPH             = NO
CALLER_GRAPH           = NO

# Regenerate
./scripts/generate-graphviz-docs.sh --clean
```

### Example 2: Document Specific Modules Only

For targeted documentation of specific packages:

```bash
# Edit configuration
nano Doxyfile

# Change INPUT to specific directories:
INPUT                  = src/main/java/tech/derbent/app/activities \
                         src/main/java/tech/derbent/app/projects

# Regenerate
./scripts/generate-graphviz-docs.sh --clean
```

### Example 3: Generate PDF Documentation

For offline documentation or official releases:

```bash
# Edit configuration
nano Doxyfile

# Enable LaTeX:
GENERATE_LATEX         = YES

# Generate docs
./scripts/generate-graphviz-docs.sh --clean

# Build PDF
cd docs/graphviz-output/latex
make pdf

# PDF created: refman.pdf
```

### Example 4: Interactive SVG Diagrams

Enable clickable, zoomable diagrams:

```bash
# Edit configuration
nano Doxyfile

# Change these lines:
INTERACTIVE_SVG        = YES
DOT_IMAGE_FORMAT       = svg

# Regenerate
./scripts/generate-graphviz-docs.sh --clean
```

## 🔧 Performance Tuning

### For Large Codebases (Slow Generation)

```bash
# Edit configuration
nano Doxyfile

# Reduce complexity:
DOT_GRAPH_MAX_NODES    = 50          # Default: 100
MAX_DOT_GRAPH_DEPTH    = 2           # Default: 0 (unlimited)
CALL_GRAPH             = NO          # Disable expensive graphs
COLLABORATION_GRAPH    = NO          # Disable if not needed

# Use more threads:
DOT_NUM_THREADS        = 8           # Use available CPU cores

# Regenerate
./scripts/generate-graphviz-docs.sh --clean
```

### For Quick Updates (Development Mode)

```bash
# Create a minimal Doxyfile for specific module
cp Doxyfile Doxyfile.dev

# Edit Doxyfile.dev
nano Doxyfile.dev

# Minimal settings:
INPUT                  = src/main/java/tech/derbent/app/activities
CALL_GRAPH             = NO
COLLABORATION_GRAPH    = NO
OUTPUT_DIRECTORY       = docs/graphviz-output-dev

# Generate quickly
doxygen Doxyfile.dev
```

## 🔄 Automation Examples

### Weekly Scheduled Generation

Add to crontab for automatic weekly updates:

```bash
# Edit crontab
crontab -e

# Add line for Sunday 2 AM:
0 2 * * 0 cd /home/user/derbent && ./scripts/generate-graphviz-docs.sh --clean
```

### Pre-Commit Hook

Remind developers to update documentation:

```bash
# Create hook
nano .git/hooks/pre-commit

# Add content:
#!/bin/bash
if git diff --cached --name-only | grep -q "src/main/java"; then
    echo "⚠️  Java files modified!"
    echo "Consider regenerating documentation:"
    echo "  ./scripts/generate-graphviz-docs.sh"
fi

# Make executable
chmod +x .git/hooks/pre-commit
```

### CI/CD Integration (GitHub Actions)

```yaml
# .github/workflows/docs.yml
name: Generate Documentation

on:
  push:
    branches: [ main ]
    paths: [ 'src/main/java/**' ]

jobs:
  docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Install tools
        run: sudo apt-get install -y doxygen graphviz
      
      - name: Generate docs
        run: ./scripts/generate-graphviz-docs.sh --clean
      
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./docs/graphviz-output/html
```

## 📊 Output Examples

### What You'll Get

After running `./scripts/generate-graphviz-docs.sh`:

```
docs/graphviz-output/
├── html/
│   ├── index.html              # Main entry - start here
│   ├── annotated.html          # All classes listed
│   ├── hierarchy.html          # Class hierarchy tree
│   ├── classes.html            # Alphabetical class list
│   ├── files.html              # All source files
│   ├── dir_*.html              # Directory structures
│   ├── class_*.html            # Individual class pages
│   ├── *.png                   # Diagram images
│   └── search/                 # Search functionality
└── doxygen-warnings.log        # Warnings and issues
```

### Key Pages to Bookmark

1. **Main Page**: `index.html` - Project overview
2. **Class Hierarchy**: `hierarchy.html` - Inheritance tree
3. **All Classes**: `annotated.html` - Quick class access
4. **File List**: `files.html` - Browse by file
5. **Search**: Top-right corner of any page

## 🎓 Tips and Tricks

### Tip 1: Use Search Effectively

- Press `/` to focus search box
- Search for class names, method names, or keywords
- Use wildcards: `C*Activity` finds all activity classes

### Tip 2: Navigate Graphs

- Click nodes in diagrams to jump to class pages
- Use browser back button to return
- Zoom in/out on SVG diagrams (if enabled)

### Tip 3: Bookmark Important Classes

Create browser bookmarks for frequently accessed classes:
- `CActivity`
- `CProject`
- `CUser`
- `CAbstractService`
- `CEntityDB`

### Tip 4: Compare with PlantUML Diagrams

Use both documentation types together:
- **PlantUML** (`docs/diagrams/`): Design and architecture
- **Doxygen**: Implementation details and API reference

### Tip 5: Keep Documentation Updated

Set a reminder to regenerate after major changes:
- After adding new modules
- Before code reviews
- Before releases
- Monthly for active projects

## 📞 Troubleshooting Quick Reference

### Issue: Graphs not generating

```bash
# Check Graphviz installation
which dot
dot -V

# Verify configuration
grep "HAVE_DOT\|DOT_PATH" Doxyfile

# Solution
sudo apt-get install graphviz
./scripts/generate-graphviz-docs.sh --clean
```

### Issue: Generation too slow

```bash
# Disable call graphs
nano Doxyfile
# Set: CALL_GRAPH = NO, CALLER_GRAPH = NO

# Reduce complexity
# Set: DOT_GRAPH_MAX_NODES = 50

./scripts/generate-graphviz-docs.sh --clean
```

### Issue: Out of memory

```bash
# Reduce graph nodes
nano Doxyfile
# Set: DOT_GRAPH_MAX_NODES = 30

# Disable some graphs
# Set: COLLABORATION_GRAPH = NO

./scripts/generate-graphviz-docs.sh --clean
```

### Issue: Can't find generated docs

```bash
# Check output directory
ls -la docs/graphviz-output/html/

# If missing, check for errors
cat docs/graphviz-output/doxygen-warnings.log

# Regenerate with verbose output
./scripts/generate-graphviz-docs.sh --clean
```

## 📚 Additional Resources

- **Full Guide**: [docs/GRAPHVIZ_DOCUMENTATION_GUIDE.md](GRAPHVIZ_DOCUMENTATION_GUIDE.md)
- **Doxygen Manual**: https://www.doxygen.nl/manual/
- **Graphviz Docs**: https://graphviz.org/documentation/
- **Script Help**: `./scripts/generate-graphviz-docs.sh --help`

## 🎯 Next Steps

After reviewing these examples:

1. ✅ Install prerequisites
2. ✅ Generate your first documentation
3. ✅ Explore the output
4. ✅ Customize configuration
5. ✅ Set up automation
6. ✅ Share with your team

Happy documenting! 🚀
