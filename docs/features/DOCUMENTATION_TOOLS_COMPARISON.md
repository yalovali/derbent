# 🔄 Documentation Tools Comparison for Derbent

This guide helps you choose the right documentation tool for different tasks in the Derbent project.

---

## 📊 Tools Overview

Derbent uses multiple documentation approaches, each with specific strengths:

| Tool | Purpose | Output | When to Use |
|:-----|:--------|:-------|:------------|
| **Graphviz/Doxygen** | Code structure & API docs | HTML diagrams + docs | Understanding architecture, exploring codebase |
| **PlantUML** | Design documentation | PNG/SVG diagrams | Architecture design, documentation creation |
| **JavaDoc** | API reference | HTML docs | Quick API reference, IDE integration |
| **Markdown** | Documentation | MD/HTML files | Guides, tutorials, explanations |
| **Playwright** | UI testing | Screenshots + test reports | UI validation, regression testing |

---

## 🎯 When to Use Each Tool

### Use Graphviz/Doxygen When:

✅ **Exploring an unfamiliar codebase**
- Generate call graphs to understand flow
- View class hierarchies to understand structure
- See collaboration diagrams for relationships

✅ **Onboarding new developers**
- Provide comprehensive visual documentation
- Show how modules connect
- Give interactive API reference

✅ **Planning refactoring**
- See caller graphs to understand impact
- Analyze class dependencies
- Identify tightly coupled code

✅ **Architecture reviews**
- Generate complete system overview
- Visualize inheritance hierarchies
- Document actual implementation

✅ **Creating API documentation**
- Automatic from code comments
- Always up-to-date with code
- Cross-referenced and searchable

**Example Commands:**
```bash
# Generate complete docs
./scripts/generate-graphviz-docs.sh --open

# Find all callers of a method
# → Open docs, search method, view caller graph
```

### Use PlantUML When:

✅ **Designing new features**
- Create sequence diagrams for flows
- Design class structures before coding
- Document intended behavior

✅ **Documenting architecture decisions**
- Show design patterns
- Illustrate conceptual models
- Create presentation-ready diagrams

✅ **Writing documentation first**
- Diagram-as-code approach
- Version controlled diagrams
- Easy to update and maintain

✅ **Creating clean, focused diagrams**
- Control exactly what's shown
- Custom styling and colors
- Documentation-quality output

**Existing PlantUML Diagrams:**
- `docs/diagrams/gantt-*.puml` - Gantt chart designs
- Architecture patterns (if any)

**Example Usage:**
```bash
# View existing diagrams
ls docs/diagrams/*.puml

# Generate PNG from PlantUML
plantuml -tpng docs/diagrams/*.puml
```

### Use JavaDoc When:

✅ **Quick API reference in IDE**
- Hover over class/method
- Ctrl+Click to jump to docs
- Auto-complete documentation

✅ **Standard Java documentation**
- Industry standard format
- Tool support everywhere
- Simple setup

✅ **Generating standard API docs**
- For library consumers
- Maven site generation
- CI/CD integration

**Example:**
```bash
# Generate JavaDoc
mvn javadoc:javadoc

# Output: target/site/apidocs/index.html
```

### Use Markdown When:

✅ **Writing guides and tutorials**
- How-to documentation
- Setup instructions
- Best practices

✅ **Architecture documentation**
- Design decisions
- Coding standards
- Development processes

✅ **Project documentation**
- README files
- Change logs
- Contributing guides

**Existing Markdown Docs:**
- `docs/architecture/` - Architecture patterns
- `docs/development/` - Development guides
- `docs/implementation/` - Feature docs
- `README.md` - Project overview

### Use Playwright When:

✅ **UI validation and testing**
- Automated UI tests
- Screenshot comparisons
- Regression testing

✅ **Documenting UI changes**
- Before/after screenshots
- Feature demonstrations
- Bug reports

**Example:**
```bash
# Run UI tests with screenshots
./run-playwright-tests.sh menu
```

---

## 🔄 Tools Working Together

### Example Workflow: Adding a New Feature

**1. Design Phase (PlantUML)**
```bash
# Create design diagram
nano docs/diagrams/new-feature-design.puml

# Generate diagram
plantuml -tpng docs/diagrams/new-feature-design.puml
```

**2. Implementation Phase (JavaDoc)**
```java
/**
 * New feature implementation
 * See design: docs/diagrams/new-feature-design.png
 */
public class CNewFeature extends CEntityDB<CNewFeature> {
    // Implementation with JavaDoc comments
}
```

**3. Testing Phase (Playwright)**
```bash
# Create and run UI tests
./run-playwright-tests.sh comprehensive
```

**4. Documentation Phase**

**Markdown:**
```bash
# Write feature guide
nano docs/features/new-feature.md
```

**Graphviz:**
```bash
# Generate updated API docs
./scripts/generate-graphviz-docs.sh --clean
```

**5. Review Phase**
- **PlantUML diagram**: Design intent
- **Graphviz docs**: Actual implementation
- **Playwright screenshots**: Working UI
- **Markdown guide**: How to use

---

## 📋 Comparison Matrix

### Feature Comparison

| Feature | Graphviz | PlantUML | JavaDoc | Markdown |
|:--------|:---------|:---------|:--------|:---------|
| **Auto-generated** | ✅ Yes | ❌ No | ✅ Yes | ❌ No |
| **Always current** | ✅ Yes | ⚠️ Manual | ✅ Yes | ⚠️ Manual |
| **Visual diagrams** | ✅ Yes | ✅ Yes | ❌ No | ⚠️ Manual |
| **Call graphs** | ✅ Yes | ❌ No | ❌ No | ❌ No |
| **IDE integration** | ❌ No | ⚠️ Plugin | ✅ Yes | ⚠️ Preview |
| **Search** | ✅ Yes | ❌ No | ✅ Yes | ⚠️ GitHub |
| **Version control** | ⚠️ Config | ✅ Yes | ⚠️ Config | ✅ Yes |
| **Setup complexity** | Medium | Low | Low | Low |
| **Generation speed** | Slow | Fast | Medium | N/A |
| **Output size** | Large | Small | Medium | Small |

### Use Case Comparison

| Use Case | Best Tool | Why |
|:---------|:----------|:----|
| **Explore unknown code** | Graphviz | Call graphs show actual flow |
| **Design new feature** | PlantUML | Control what's shown |
| **Quick API lookup** | JavaDoc | IDE integration |
| **Write tutorial** | Markdown | Easy to write and read |
| **Architecture review** | Graphviz + PlantUML | Both actual and intended |
| **Onboarding** | All | Different learning styles |
| **Bug investigation** | Graphviz | Trace call paths |
| **Feature planning** | PlantUML | Design before code |
| **API documentation** | JavaDoc + Graphviz | Standard + visual |
| **UI validation** | Playwright | Automated testing |

---

## 🎓 Recommended Approach

### For Different Roles

#### New Developer
1. Read `README.md` (Markdown)
2. Browse Graphviz docs for overview
3. Study architecture in `docs/architecture/` (Markdown)
4. Use JavaDoc in IDE while coding
5. Reference PlantUML diagrams for designs

#### Experienced Developer
1. Use JavaDoc in IDE daily
2. Generate Graphviz for refactoring
3. Create PlantUML for new designs
4. Write Markdown for feature docs
5. Run Playwright tests before commits

#### Architect
1. Design with PlantUML
2. Review implementation with Graphviz
3. Document decisions in Markdown
4. Compare design vs. actual
5. Update documentation after changes

#### QA Engineer
1. Run Playwright tests
2. Use Graphviz to understand code flow
3. Reference Markdown guides
4. Create bug reports with screenshots
5. Verify fixes with automated tests

### For Different Tasks

#### Understanding Existing Code
```bash
# 1. Read Markdown guide
cat docs/architecture/coding-standards.md

# 2. Generate and explore Graphviz
./scripts/generate-graphviz-docs.sh --open

# 3. Use JavaDoc in IDE
# Ctrl+Click on classes

# 4. View PlantUML designs
ls docs/diagrams/
```

#### Adding New Code
```bash
# 1. Design with PlantUML
nano docs/diagrams/my-feature.puml
plantuml -tpng docs/diagrams/my-feature.puml

# 2. Implement with JavaDoc
# Write code with JavaDoc comments

# 3. Test with Playwright
./run-playwright-tests.sh

# 4. Document in Markdown
nano docs/features/my-feature.md

# 5. Regenerate Graphviz
./scripts/generate-graphviz-docs.sh --clean
```

#### Refactoring
```bash
# 1. Generate Graphviz docs
./scripts/generate-graphviz-docs.sh

# 2. Analyze call graphs and dependencies
# Open docs, search for class, view graphs

# 3. Plan changes
# Document in Markdown or update PlantUML

# 4. Implement changes
# Update code and JavaDoc

# 5. Verify with tests
./run-playwright-tests.sh comprehensive

# 6. Regenerate docs
./scripts/generate-graphviz-docs.sh --clean
```

---

## 💡 Best Practices

### Documentation Strategy

**Do:**
- ✅ Use Graphviz for architecture exploration
- ✅ Use PlantUML for design documentation
- ✅ Use JavaDoc for code-level documentation
- ✅ Use Markdown for guides and processes
- ✅ Use Playwright for UI validation
- ✅ Keep all documentation updated together

**Don't:**
- ❌ Rely on single tool for everything
- ❌ Let auto-generated docs replace human-written guides
- ❌ Forget to regenerate after major changes
- ❌ Mix concerns (design in code, implementation in design docs)

### Update Frequency

| Tool | Update When |
|:-----|:------------|
| **Graphviz** | After major code changes, before releases, monthly |
| **PlantUML** | When design changes, before implementation |
| **JavaDoc** | Every commit (in code comments) |
| **Markdown** | When features/processes change |
| **Playwright** | Continuous (automated tests) |

### Automation Opportunities

**Graphviz:**
```bash
# CI/CD: Regenerate on every merge to main
# Pre-commit: Remind to regenerate if Java files changed
```

**PlantUML:**
```bash
# CI/CD: Generate PNGs from .puml files
# Pre-commit: Validate .puml syntax
```

**JavaDoc:**
```bash
# CI/CD: Generate and publish on releases
# Maven site: Include in project site
```

**Markdown:**
```bash
# CI/CD: Convert to HTML/PDF for distribution
# GitHub: Auto-renders markdown
```

**Playwright:**
```bash
# CI/CD: Run on every PR
# Scheduled: Nightly comprehensive tests
```

---

## 🔍 Quick Reference

### I Want To...

**Understand class hierarchy**
→ Use Graphviz class hierarchy page

**See method call flow**
→ Use Graphviz call/caller graphs

**Design new feature**
→ Use PlantUML sequence diagrams

**Look up API quickly**
→ Use JavaDoc in IDE

**Write setup guide**
→ Use Markdown

**Validate UI changes**
→ Use Playwright screenshots

**Onboard new developer**
→ Use all: Markdown guides + Graphviz overview + JavaDoc reference

**Plan refactoring**
→ Use Graphviz collaboration diagrams + call graphs

**Document architecture**
→ Use PlantUML for design + Graphviz for actual + Markdown for explanation

**Create bug report**
→ Use Playwright screenshots + Markdown description

---

## 📚 Resources

### Graphviz/Doxygen
- [Complete Guide](GRAPHVIZ_DOCUMENTATION_GUIDE.md)
- [Quick Examples](GRAPHVIZ_QUICK_START_EXAMPLES.md)
- [Visual Guide](GRAPHVIZ_VISUAL_GUIDE.md)
- Generation: `./scripts/generate-graphviz-docs.sh`

### PlantUML
- Existing diagrams: `docs/diagrams/`
- Website: https://plantuml.com
- Generate: `plantuml -tpng docs/diagrams/*.puml`

### JavaDoc
- Generate: `mvn javadoc:javadoc`
- Output: `target/site/apidocs/`
- Website: https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html

### Markdown
- Existing docs: `docs/` directory
- Preview: GitHub renders automatically
- Convert: Use pandoc or similar tools

### Playwright
- Guide: `docs/implementation/PLAYWRIGHT_TEST_GUIDE.md`
- Run: `./run-playwright-tests.sh`
- Output: `target/screenshots/`

---

## 🎯 Summary

**Choose the Right Tool:**
- **Graphviz**: Understand existing code architecture
- **PlantUML**: Design new features and patterns
- **JavaDoc**: Quick API reference in IDE
- **Markdown**: Guides, tutorials, and processes
- **Playwright**: UI testing and validation

**Use Together:**
All tools complement each other. Use multiple tools for comprehensive documentation:
- Design → PlantUML
- Implement → JavaDoc
- Generate → Graphviz
- Document → Markdown
- Validate → Playwright

**Stay Current:**
- Regenerate Graphviz after major changes
- Update PlantUML when design changes
- Keep JavaDoc in code updated
- Maintain Markdown guides
- Run Playwright tests regularly

---

**Next Steps:**
1. Generate Graphviz docs: `./scripts/generate-graphviz-docs.sh`
2. Explore existing docs: `ls docs/`
3. Review this guide: [comparison table](#comparison-matrix)
4. Choose tool for your current task
5. Start documenting! 🚀
