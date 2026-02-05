# Java Documentation & Architecture Visualization Solution

## Overview
This solution provides comprehensive documentation and visualization for Java projects, including:
- ✅ **Full inheritance hierarchies** - complete class hierarchy trees
- ✅ **Interface implementations** - shows all interface relationships
- ✅ **Package dependencies** - cross-package dependency analysis
- ✅ **Call graphs** - method invocation relationships via jdeps
- ✅ **JavaDoc API documentation** - standard Java documentation
- ✅ **UML diagrams** - visual class diagrams using GraphViz

## Solution Components

### 1. Maven JavaDoc Plugin
- Configured in `pom.xml` with lenient error handling
- Generates standard JavaDoc with source code links
- Shows all visibility levels (private, protected, public)
- Links to external documentation (Java 21, Spring, Vaadin)

### 2. Custom Architecture Analysis
- **Python-based class analyzer** - parses Java source files
- **GraphViz DOT generation** - creates inheritance diagrams
- **jdeps integration** - analyzes runtime dependencies
- **Multiple visualization formats** - PNG, SVG, DOT

### 3. Automated Generation Script
**File:** `generate-architecture-docs.sh`

**What it does:**
1. Compiles the project
2. Generates JavaDoc
3. Analyzes dependencies with jdeps
4. Extracts class hierarchies from source files
5. Generates GraphViz diagrams
6. Creates HTML index page with all visualizations

## Usage

### Generate Documentation
```bash
./generate-architecture-docs.sh
```

### View Documentation
```bash
# Architecture overview with diagrams
xdg-open target/architecture-docs/index.html

# JavaDoc API documentation
xdg-open target/javadoc/index.html
```

### Generated Files
```
target/
├── architecture-docs/
│   ├── index.html                          # Main architecture page
│   ├── full-inheritance.{png,svg,dot}      # Complete class hierarchy
│   ├── inheritance-tech-derbent-*.{png,svg} # Per-package hierarchies
│   ├── classes.dot                         # jdeps class dependencies
│   └── class-dependencies.txt              # Text dependency report
└── javadoc/
    └── index.html                          # Standard JavaDoc
```

## Features

### Inheritance Visualization
- **Blue solid arrows** - `extends` relationships
- **Green dashed arrows** - `implements` relationships
- **Color coding:**
  - Light blue - Regular classes
  - Light yellow - Interfaces
  - Light coral - Enums

### Package Analysis
- Cross-package dependencies
- Module boundaries
- Circular dependency detection

### Call Graph Analysis
- Class-level dependency mapping
- Method invocation tracking (via jdeps)
- External library usage

## Why This Solution?

### ❌ Why not Doxygen?
- Poor Java support (designed for C/C++)
- Weak inheritance graph generation
- Limited call graph capabilities for Java

### ❌ Why not Dokka?
- Kotlin-focused tool
- Compatibility issues with Java-only projects
- Requires Kotlin runtime

### ✅ Why This Solution?
- **Native Java tools** - JavaDoc, jdeps, standard JDK tools
- **Free and open-source** - No licenses required
- **GraphViz integration** - Professional quality diagrams
- **Comprehensive coverage** - Inheritance + dependencies + JavaDoc
- **Customizable** - Python script can be extended
- **Fast generation** - ~2-3 minutes for full documentation

## Technical Details

### Requirements
- Java 21
- Maven 3.9+
- GraphViz (dot command)
- Python 3 (for inheritance analysis)

### Configuration
See `pom.xml`:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.6.3</version>
    <configuration>
        <source>21</source>
        <show>private</show>
        <doclint>none</doclint>
        <failOnError>false</failOnError>
        ...
    </configuration>
</plugin>
```

### Customization

#### Add more diagram types:
Edit `generate-architecture-docs.sh` to add custom GraphViz visualizations.

#### Filter packages:
Modify the Python script's `package_filter` parameter to focus on specific packages.

#### Change diagram style:
Edit the DOT file generation in Python script:
- Node shapes: `shape=box|circle|diamond`
- Colors: `fillcolor=colorname`
- Layout: `rankdir=BT|TB|LR|RL`

## Diagram Examples

### Full Project Hierarchy
Shows all classes with their inheritance relationships across the entire codebase.

### Package-Specific Diagrams
Generated per major package:
- `tech.derbent.abstracts` - Base classes and utilities
- `tech.derbent.activities` - Activity management
- `tech.derbent.projects` - Project management
- etc.

## Troubleshooting

### "Graph is too large for cairo-renderer"
This is normal for large projects. The diagram is automatically scaled. Use SVG format for lossless viewing:
```bash
xdg-open target/architecture-docs/full-inheritance.svg
```

### JavaDoc errors
The configuration uses `doclint=none` and `failOnError=false` to be lenient. Documentation still generates with warnings.

### Missing GraphViz
```bash
sudo apt-get install graphviz
```

## Integration with CI/CD

Add to your CI pipeline:
```yaml
- name: Generate Documentation
  run: ./generate-architecture-docs.sh

- name: Publish Documentation
  uses: actions/upload-artifact@v3
  with:
    name: architecture-docs
    path: target/architecture-docs/
```

## Future Enhancements

Potential additions:
- [ ] Sequence diagrams for key workflows
- [ ] Metrics dashboard (cyclomatic complexity, etc.)
- [ ] Interactive diagrams with JavaScript
- [ ] Integration with SonarQube
- [ ] Automated documentation deployment

## Credits

- **JavaDoc** - Oracle/OpenJDK
- **jdeps** - Java Dependency Analysis Tool
- **GraphViz** - AT&T Research
- **Custom Python analyzer** - Derbent project

## License

This documentation solution is part of the Derbent project.
© 2025 Lova.tech

---

**Last Updated:** 2025-11-15
**Version:** 1.0
