# Derbent AI Agents - Quick Reference Card

**🤖 Agent Startup Verification**
```
Every AI agent MUST print this message when starting:
🤖 configuration loaded successfully - Agent is following Derbent coding standards
```

## 🏗️ Pattern Designer Agent

**Purpose**: Analyze patterns, design solutions  
**File**: `.github/agents/pattern-designer/pattern-designer.agent.md`

**Quick Commands**:
```bash
# Analyze patterns
.github/agents/pattern-designer/scripts/analyze-patterns.sh

# Search for pattern
grep -r "implements IHasAttachments" src/main/java --include="*.java"

# Find entities
grep -r "public class C.*extends.*<" src/main/java --include="*.java"
```

**Key Responsibilities**:
- 🔍 Scan codebase for patterns
- 📐 Design generic solutions
- 📝 Create pattern documentation
- ✅ Ensure consistency

---

## 💻 Coder Agent

**Purpose**: Implement features following patterns  
**File**: `.github/agents/coder/coder.agent.md`

**Mandatory Checklist**:
- [ ] C-prefix on class name
- [ ] Generic type parameter
- [ ] All 5 entity constants
- [ ] Collections at field declaration
- [ ] JPA constructor: NO initializeDefaults()
- [ ] Business constructor: WITH initializeDefaults()
- [ ] Constructor injection (no @Autowired fields)
- [ ] validateEntity() mirrors DB constraints

**Quick Commands**:
```bash
# Format code
mvn spotless:apply

# Build (Java 17)
mvn clean compile -Pagents -DskipTests
```

---

## ✅ Verifier Agent

**Purpose**: Validate code, run tests  
**File**: `.github/agents/verifier/verifier.agent.md`

**Quick Commands**:
```bash
# Full verification
.github/agents/verifier/scripts/verify-code.sh

# Selective Playwright test
.github/agents/verifier/scripts/test-selective.sh activity

# Keywords: activity, storage, meeting, user, issue, product
```

**Verification Checks**:
1. ✅ C-Prefix convention
2. ✅ Generic types (no raw types)
3. ✅ Constructor injection
4. ✅ Entity constants
5. ✅ Collections initialization
6. ✅ JPA constructor pattern
7. ✅ Imports (no fully-qualified)
8. ✅ Build succeeds

---

## 📚 Documenter Agent

**Purpose**: Create and maintain documentation  
**File**: `.github/agents/documenter/documenter.agent.md`

**Documentation Structure**:
```
docs/
├── architecture/      # Design patterns
├── development/       # Dev guides
├── implementation/    # Implementation details
└── patterns/          # Pattern library
```

**Template Locations**:
- Pattern Template: `config/settings.md`
- Feature Template: `config/settings.md`
- Migration Template: `config/settings.md`

---

## 🔄 Workflows

### Complete Feature
```
Pattern Designer → Analyze
        ↓
Documenter → Document
        ↓
Coder → Implement
        ↓
Verifier → Validate
```

### Quick Fix
```
Verifier → Find violation
        ↓
Coder → Fix
        ↓
Verifier → Re-validate
```

---

## 📋 Common Tasks

### Create New Entity
1. Pattern Designer: Identify patterns
2. Coder: Create entity + service + repository + initializer
3. Coder: Wire into CDataInitializer
4. Verifier: Run checks and tests
5. Documenter: Document feature

### Fix Pattern Violation
1. Verifier: Run verify-code.sh
2. Pattern Designer: Explain correct pattern
3. Coder: Apply fix
4. Verifier: Re-verify

### Refactor to Pattern
1. Pattern Designer: Design pattern
2. Documenter: Create pattern doc
3. Coder: Refactor code
4. Verifier: Validate

---

## 🎯 Test Keywords

| Keyword | Pages Tested |
|---------|--------------|
| activity | Activities, Types, Priorities |
| storage | Storages, Types, Items |
| meeting | Meetings, Types |
| user | Users, Roles, Project Roles |
| issue | Issues, Types, Priorities |
| product | Products, Types, Categories |

---

## ⚠️ Common Violations

### ❌ Raw Types
```java
public class CActivity extends CProjectItem {  // WRONG
```
**Fix**: Add generic type parameter `<CActivity>`

### ❌ Field Injection
```java
@Autowired
private CService service;  // WRONG
```
**Fix**: Use constructor injection

### ❌ JPA Constructor Calling initializeDefaults
```java
protected CEntity() {
    super();
    initializeDefaults();  // WRONG
}
```
**Fix**: Remove call (only in business constructors)

### ❌ Collections in initializeDefaults
```java
private final void initializeDefaults() {
    attachments = new HashSet<>();  // WRONG
}
```
**Fix**: Initialize at field declaration

---

## 🚀 Quick Start

### For AI Agents
1. Read: `.github/copilot-instructions.md` (AGENTS.md)
2. Load: `.github/agents/{agent}/{agent}.agent.md`
3. Config: `.github/agents/{agent}/config/settings.md`
4. Run: Scripts in `scripts/` directory

### For Developers
```bash
# Before commit
mvn spotless:apply
.github/agents/verifier/scripts/verify-code.sh

# Test specific feature
.github/agents/verifier/scripts/test-selective.sh activity

# Analyze patterns
.github/agents/pattern-designer/scripts/analyze-patterns.sh
```

---

## 📞 Help

| Issue | Solution |
|-------|----------|
| Build fails | Check `/tmp/build.log`, run `mvn spotless:apply` |
| Test fails | Use selective testing, check `/tmp/playwright-{keyword}.log` |
| Pattern unclear | Run Pattern Designer analysis |
| Violation found | Check Verifier report, consult AGENTS.md |

---

## 📊 Quality Gates

**Before Commit**:
- [ ] Code formatted (mvn spotless:apply)
- [ ] Build succeeds (mvn compile)
- [ ] Verifier passes (verify-code.sh)
- [ ] Tests pass (selective testing)

**Before Merge**:
- [ ] All quality gates passed
- [ ] Documentation updated
- [ ] Pattern compliance verified
- [ ] No TODOs left

---

**SSC WAS HERE!! 🌟**

**Remember**: Quality over speed. Use the right agent for each task. Follow patterns strictly.
