# Derbent AI Agents - Quick Reference Card

**🎯 Simple Prompts = Autonomous Agents = Complete Solutions**

## 🚀 Ultra-Quick Start

**Just say what you need - agents activate and work independently!**

```
👤 "create CStorage entity"    → 💻 Coder creates 4 files automatically
👤 "test storage"              → ✅ Verifier runs selective tests (1-2 min)
👤 "analyze patterns"          → 🏗️ Pattern Designer scans and designs
👤 "document CStorage"         → 📚 Documenter creates complete docs
```

**No questions asked. No user intervention. Complete solutions delivered.**

---

## 🤖 Agent Auto-Triggers

| Your Simple Prompt | Agent | What Happens Automatically |
|-------------------|-------|---------------------------|
| create, implement, add | 💻 Coder | Creates entity + service + repo + initializer + wires |
| verify, test, check | ✅ Verifier | Runs 8 checks + build + selective tests + report |
| analyze, design, pattern | 🏗️ Pattern Designer | Scans code + identifies patterns + creates docs |
| document, explain, guide | 📚 Documenter | Creates docs with examples + updates AGENTS.md |

**See [AUTO_TRIGGER_GUIDE.md](AUTO_TRIGGER_GUIDE.md) for complete workflows.**

---

## 🏗️ Pattern Designer Agent

**Auto-Triggers**: "analyze", "pattern", "design", "find similar"  
**File**: `.github/agents/pattern-designer/pattern-designer.agent.md`

**What It Does Autonomously**:
- 🔍 Scans codebase automatically
- 📐 Identifies common structures
- 📝 Creates pattern documentation
- ✅ Provides ready-to-use solution

## 💻 Coder Agent

**Auto-Triggers**: "create", "implement", "add", "build", "refactor", "fix"  
**File**: `.github/agents/coder/coder.agent.md`

**What It Does Autonomously**:
1. ✅ Loads AGENTS.md rules
2. ✅ Creates entity + service + repository + initializer
3. ✅ Wires into CDataInitializer
4. ✅ Formats with Spotless
5. ✅ Zero TODOs, production-ready

**Mandatory Checklist** (Applied Automatically):
- [ ] C-prefix on class name
- [ ] Generic type parameter
- [ ] All 5 entity constants
- [ ] Collections at field declaration
- [ ] JPA constructor: NO initializeDefaults()
- [ ] Business constructor: WITH initializeDefaults()
- [ ] Constructor injection (no @Autowired fields)
- [ ] validateEntity() mirrors DB constraints

## ✅ Verifier Agent

**Auto-Triggers**: "verify", "test", "check", "validate", "find violations"  
**File**: `.github/agents/verifier/verifier.agent.md`

**What It Does Autonomously**:
1. ✅ Runs 8 static analysis checks
2. ✅ Executes build verification
3. ✅ Determines test keyword automatically
4. ✅ Runs selective Playwright tests (1-2 min)
5. ✅ Generates complete report
6. ✅ Lists violations with file:line
7. ✅ Suggests specific fixes

**Verification Checks** (Automatic):
1. ✅ C-Prefix convention
2. ✅ Generic types (no raw types)
3. ✅ Constructor injection
4. ✅ Entity constants
5. ✅ Collections initialization
6. ✅ JPA constructor pattern
7. ✅ Imports (no fully-qualified)
8. ✅ Build succeeds

## 📚 Documenter Agent

**Auto-Triggers**: "document", "explain", "guide", "update docs"  
**File**: `.github/agents/documenter/documenter.agent.md`

**What It Does Autonomously**:
1. ✅ Determines doc type (pattern, feature, guide)
2. ✅ Uses correct template
3. ✅ Includes ✅ CORRECT / ❌ INCORRECT examples
4. ✅ Adds verification checklist
5. ✅ Cross-references related docs
6. ✅ Updates AGENTS.md if new pattern
7. ✅ Places in correct directory

**Documentation Structure**:
```
docs/
├── architecture/      # Design patterns
├── development/       # Dev guides
├── implementation/    # Implementation details
└── patterns/          # Pattern library
```

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

## ⚠️ Fail-Fast Rule (MANDATORY)

- UI tests MUST fail immediately on exception dialogs or error toasts.
- Always use wait helpers that perform fail-fast checks after actions.

**Before Merge**:
- [ ] All quality gates passed
- [ ] Documentation updated
- [ ] Pattern compliance verified
- [ ] No TODOs left

---

**SSC WAS HERE!! 🌟**

**Remember**: Quality over speed. Use the right agent for each task. Follow patterns strictly.
