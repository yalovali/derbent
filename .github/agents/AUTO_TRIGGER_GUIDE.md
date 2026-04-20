---
name: auto-trigger-guide-doc
description: Documentation: trigger keywords and workflows for Derbent custom agents
tools: []
target: vscode
---

# Derbent AI Agents - Auto-Trigger Guide

**CRITICAL (2026-04-20)**: For every task, use the **Orchestrator** as the entrypoint to coordinate the multi-agent flow; auto-triggers are still used, but orchestration is mandatory.

**Simple prompts automatically activate the right agent with full autonomy**

## 🎯 How It Works

1. **User types simple prompt** (e.g., "create CStorage entity")
2. **Agent auto-detects trigger keywords** and activates
3. **Agent loads AGENTS.md rules** automatically
4. **Agent executes task autonomously** - no questions asked
5. **Agent delivers complete solution** ready to use

**Zero user intervention needed** - agents know their domain and follow all rules.

---

## 🏗️ Pattern Designer Agent Triggers

### Trigger Keywords
```
analyze pattern | find pattern | design pattern
what patterns | similar implementations | common structure
abstract pattern | generic solution | architecture review
```

### Example Prompts
```
✅ "analyze entity initialization patterns"
✅ "what patterns exist for composition entities"
✅ "find similar implementations of IHasAttachments"
✅ "design abstract pattern for node types"
```

### What Agent Does Autonomously
1. Scans codebase (grep, glob) without asking
2. Identifies common structures automatically
3. Designs generic solution following AGENTS.md
4. Creates pattern documentation with examples
5. Provides ✅ CORRECT / ❌ INCORRECT examples
6. Suggests where to place in `docs/patterns/`

### Expected Output
- Pattern analysis report
- Generic solution with code templates
- Complete documentation with examples
- Verification checklist
- Migration guide (if refactoring needed)

---

## 💻 Coder Agent Triggers

### Trigger Keywords
```
create | implement | add | build
generate | write code | refactor | fix
develop | code | make
```

### Example Prompts
```
✅ "create CStorage entity"
✅ "implement CCustomerService"
✅ "add attachment support to CMeeting"
✅ "fix CActivity initialization pattern"
✅ "refactor CBabNode to abstract pattern"
```

### What Agent Does Autonomously
1. Loads AGENTS.md rules automatically
2. Identifies applicable patterns (entity hierarchy, interfaces)
3. Creates ALL required files:
   - Entity class with all 5 constants
   - Repository interface
   - Service class with validation
   - Initializer service
4. Wires into CDataInitializer automatically
5. Formats code with Spotless
6. Verifies compilation (if requested)

### Expected Output
- Complete implementation (4-5 files)
- All patterns followed (C-prefix, constants, init pattern)
- Zero TODOs, zero placeholders
- Production-ready code
- File list with compliance checklist

---

## ✅ Verifier Agent Triggers

### Trigger Keywords
```
verify | validate | check | test
run tests | quality check | find violations
does this follow | is this correct
```

### Example Prompts
```
✅ "verify CStorage implementation"
✅ "test activity pages"
✅ "check if CCustomer follows patterns"
✅ "validate CStorageService"
✅ "run tests"
```

### What Agent Does Autonomously
1. Runs 8 static analysis checks automatically
2. Executes build verification (mvn compile)
3. Determines test keyword automatically (storage, activity, etc.)
4. Runs selective Playwright tests (1-2 min, not 15 min)
5. Generates verification report
6. Lists specific violations with file:line
7. Suggests fixes for each violation

### Expected Output
- Verification report with pass/fail
- Static analysis results (8 checks)
- Build status
- Test results (selective by keyword)
- Specific violations with locations
- Suggested fixes
- ✅ PASS or ❌ FAIL verdict

---

## 📚 Documenter Agent Triggers

### Trigger Keywords
```
document | write docs | create guide
explain | update documentation | add to AGENTS.md
how does [X] work
```

### Example Prompts
```
✅ "document CStorage entity"
✅ "explain composition pattern"
✅ "create migration guide for initialization pattern"
✅ "update AGENTS.md with new validation rule"
✅ "how does workflow initialization work"
```

### What Agent Does Autonomously
1. Determines documentation type (pattern, feature, guide)
2. Uses correct template automatically
3. Includes ✅ CORRECT / ❌ INCORRECT examples
4. Adds verification checklist
5. Cross-references related docs
6. Updates AGENTS.md if new pattern
7. Verifies code examples compile
8. Places in correct directory (architecture/, patterns/, etc.)

### Expected Output
- Complete documentation with examples
- Code examples that compile
- Verification checklist
- Cross-references to related docs
- Proper file location
- AGENTS.md update (if applicable)

---

## 🔄 Multi-Agent Workflows

### Complete Feature Implementation
```
User: "create new CStorage entity with full CRUD"

→ Pattern Designer activates first:
  "Analyzing storage patterns... Found CActivity, CMeeting similar.
   Patterns: CProjectItem hierarchy, IHasAttachments, IHasComments..."

→ Coder activates automatically:
  "Creating CStorage entity following patterns...
   Created: CStorage.java, IStorageRepository.java, 
   CStorageService.java, CStorageInitializerService.java
   Wired into CDataInitializer. ✅"

→ Verifier activates automatically:
  "Verifying implementation...
   Static analysis: ✅ 8/8 passed
   Build: ✅ Compiled successfully
   Tests: ✅ storage keyword - 3 pages passed"

→ Documenter activates automatically:
  "Creating documentation...
   Created: docs/features/storage-feature.md
   Updated: AGENTS.md (no new patterns)"
```

### Quick Validation
```
User: "verify CActivity"

→ Verifier activates immediately:
  "Running verification on CActivity...
   ✅ C-prefix: PASS
   ✅ Constants: PASS (all 5 present)
   ❌ JPA constructor: FAIL (calls initializeDefaults)
   
   FIX REQUIRED:
   File: CActivity.java:45
   Remove: initializeDefaults() call from JPA constructor
   
   Status: ❌ REJECTED"

→ No other agents needed, user can ask Coder to fix
```

---

## 🎯 Agent Selection Logic

**Agents auto-detect which should activate based on verb in prompt:**

| Prompt Type | Primary Verb | Agent Activated |
|-------------|--------------|-----------------|
| Implementation | create, implement, add, build | Coder |
| Analysis | analyze, find, design, review | Pattern Designer |
| Validation | verify, check, test, validate | Verifier |
| Documentation | document, explain, guide | Documenter |

**Multiple agents can activate sequentially** for complex tasks.

---

## ⚙️ Agent Autonomy Rules

### What Agents Do WITHOUT Asking

✅ **Load AGENTS.md rules** - Always read master playbook first  
✅ **Identify patterns** - Scan codebase to find applicable patterns  
✅ **Create all files** - Generate complete implementations  
✅ **Format code** - Run Spotless automatically  
✅ **Wire initializers** - Add to CDataInitializer  
✅ **Run verification** - Execute checks and tests  
✅ **Generate reports** - Provide complete output  
✅ **Suggest fixes** - List specific violations with solutions  

### What Agents Do NOT Do Without Asking

❌ **Change public APIs** - Requires explicit approval  
❌ **Modify unrelated files** - Only touch necessary files  
❌ **Remove functionality** - Keep existing features  
❌ **Run full test suite** - Use selective testing (1-2 min)  
❌ **Commit code** - Only generate, never commit  

---

## 🚀 Quick Examples

### Scenario 1: New Entity Needed
```
👤 User: "create CStorageItem entity"

🤖 Coder Agent activates automatically:

   ✅ Loaded AGENTS.md rules
   ✅ Identified patterns: CProjectItem<CStorageItem>
   ✅ Interfaces: IHasAttachments, IHasComments, IHasLinks
   
   Created 4 files:
   1. CStorageItem.java (entity with 5 constants)
   2. IStorageItemRepository.java
   3. CStorageItemService.java (with validation)
   4. CStorageItemInitializerService.java
   
   ✅ Wired into CDataInitializer.initializeProject()
   ✅ Code formatted with Spotless
   ✅ Compilation verified
   
   Status: READY FOR TESTING
```

### Scenario 2: Pattern Unclear
```
👤 User: "what's the pattern for node entities"

🤖 Pattern Designer activates automatically:

   Analyzing node patterns...
   
   Found implementations:
   - CBabNodeCAN
   - CBabNodeLIN
   - CBabNodeEthernet
   
   Common structure (80%+ shared):
   - Fields: enabled, nodeType, config
   - Parent: CEntityOfCompany
   
   RECOMMENDATION: Abstract pattern
   - Create CBabNode<T> with @MappedSuperclass
   - Create CBabNodeService<T> (no @Service)
   - Create IBabNodeRepository<T> with @NoRepositoryBean
   
   Documentation: docs/patterns/abstract-entity-pattern.md
   
   Status: PATTERN IDENTIFIED
```

### Scenario 3: Quick Verification
```
👤 User: "test storage"

🤖 Verifier activates automatically:

   Running selective tests for keyword: storage
   
   Testing: Storages, Storage Types, Storage Items
   
   Results:
   ✅ Storages page: PASS (grid, CRUD, detail view)
   ✅ Storage Types page: PASS (type management)
   ✅ Storage Items page: PASS (item operations)
   
   Time: 1m 47s (vs 15 min for full suite)
   Status: ✅ ALL TESTS PASSED
```

---

## 📝 Best Practices

### For Users

1. **Keep prompts simple** - "create CStorage" is enough
2. **Trust agent autonomy** - They know the patterns
3. **Review output** - Agents provide complete solutions
4. **Ask for verification** - "verify [entity]" after creation
5. **Use selective testing** - "test activity" not "run all tests"

### For Agents

1. **Load AGENTS.md first** - Always read master playbook
2. **Follow patterns exactly** - Zero deviations
3. **Provide complete output** - No TODOs, no placeholders
4. **List what you did** - Show files created/modified
5. **Verify compliance** - Check against your own checklist
6. **Suggest next steps** - Guide user to verification/testing

---

## ✅ Success Indicators

You know agents are working correctly when:

✅ **Single prompt generates complete solution**  
✅ **No back-and-forth questions** from agent  
✅ **All patterns followed** automatically  
✅ **Code compiles** on first attempt  
✅ **Tests pass** (selective testing)  
✅ **Documentation included** for new patterns  

---

**SSC WAS HERE!! 🌟 Simple prompts, autonomous execution, complete solutions!**
