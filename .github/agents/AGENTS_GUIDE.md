# Derbent AI Agents Usage Guide

**Complete guide for AI assistants and developers on using Derbent custom agents**

## 🎯 Purpose

This guide explains how to effectively use the four specialized AI agents for the Derbent project. Each agent has specific capabilities, rules, and workflows designed to maintain code quality and architectural consistency.

## 🤖 Agent Overview

| Agent | Symbol | Primary Role | Key Tools | Output |
|-------|--------|--------------|-----------|--------|
| **Pattern Designer** | 🏗️ | Architecture & Patterns | grep, glob, view | Pattern docs |
| **Coder** | 💻 | Implementation | edit, create, bash | Code files |
| **Verifier** | ✅ | Quality & Testing | bash, grep | Reports |
| **Documenter** | 📚 | Documentation | edit, create | Markdown |

## 📋 When to Use Which Agent

### Use Pattern Designer When:
- ✅ Analyzing existing code structures
- ✅ Designing new architectural patterns
- ✅ Finding inconsistencies in implementations
- ✅ Creating generic solutions for repeated patterns
- ✅ Documenting architectural decisions

**Example Tasks**:
- "Analyze all entity initialization patterns"
- "Design a generic abstract service pattern"
- "Find all implementations of IHasAttachments"
- "Create pattern documentation for composition entities"

### Use Coder When:
- ✅ Implementing new entities/services/views
- ✅ Adding features following existing patterns
- ✅ Refactoring code to match patterns
- ✅ Fixing bugs while maintaining pattern compliance
- ✅ Creating initializer services

**Example Tasks**:
- "Create new CStorage entity with full CRUD"
- "Implement CCustomerService following pattern"
- "Refactor CActivity to use composition pattern"
- "Fix validation to mirror DB constraints"

### Use Verifier When:
- ✅ Validating code against AGENTS.md rules
- ✅ Running unit tests
- ✅ Executing Playwright tests (selective)
- ✅ Checking build success
- ✅ Generating compliance reports

**Example Tasks**:
- "Verify CActivity implementation"
- "Run Playwright tests for storage pages"
- "Check if code follows C-prefix convention"
- "Validate all entity constants present"

### Use Documenter When:
- ✅ Creating pattern documentation
- ✅ Updating AGENTS.md
- ✅ Writing feature documentation
- ✅ Creating migration guides
- ✅ Maintaining consistency across docs

**Example Tasks**:
- "Document the composition pattern"
- "Update AGENTS.md with new validation rule"
- "Create migration guide for initialization pattern"
- "Write feature documentation for Storage module"

## 🔄 Complete Workflow Examples

### Example 1: Creating New Entity (Full Workflow)

#### Step 1: Pattern Designer
```
Human: "We need to create a new CStorageItem entity. What patterns apply?"

Pattern Designer Agent:
1. Searches for similar entities (CProjectItem, CActivity)
2. Identifies applicable patterns:
   - Entity hierarchy: extends CProjectItem<CStorageItem>
   - Interfaces: IHasAttachments, IHasComments, IHasLinks
   - Initialization: JPA constructor vs business constructor
   - Composition: Attachments, Comments, Links
3. Provides pattern documentation with examples
```

#### Step 2: Coder
```
Human: "Implement CStorageItem following the identified patterns"

Coder Agent:
1. Creates entity with:
   - C-prefix: CStorageItem
   - Generic type: <CStorageItem>
   - All 5 constants
   - Collections at field declaration
   - JPA constructor (NO initializeDefaults call)
   - Business constructor (WITH initializeDefaults call)
2. Creates repository (IStorageItemRepository)
3. Creates service (CStorageItemService)
4. Creates initializer (CStorageItemInitializerService)
5. Wires into CDataInitializer
```

#### Step 3: Verifier
```
Human: "Verify the CStorageItem implementation"

Verifier Agent:
1. Runs static analysis:
   - ✅ C-prefix present
   - ✅ Generic types specified
   - ✅ Entity constants present
   - ✅ Collections at field declaration
   - ✅ Constructor pattern correct
2. Builds code: mvn clean compile -Pagents -DskipTests
3. Runs selective tests: keyword "storage"
4. Generates verification report
```

#### Step 4: Documenter
```
Human: "Document the CStorageItem entity"

Documenter Agent:
1. Creates feature documentation:
   - Overview of storage items
   - Entity structure
   - Service methods
   - Usage examples
2. Updates AGENTS.md (if new patterns introduced)
3. Cross-references related documentation
```

---

### Example 2: Fixing Pattern Violation

#### Step 1: Verifier (Discovery)
```
Human: "Check if all entities follow initialization pattern"

Verifier Agent:
1. Scans entities for JPA constructors
2. Finds: CActivity JPA constructor calls initializeDefaults()
3. Reports violation with specific file:line
4. Suggests fix
```

#### Step 2: Pattern Designer (Analysis)
```
Human: "Why is this a violation? Explain the correct pattern"

Pattern Designer Agent:
1. Explains initialization rules:
   - JPA constructors (protected, parameterless) MUST NOT call initializeDefaults()
   - Business constructors MUST call initializeDefaults()
   - Reason: JPA uses reflection, initializeDefaults needs Spring context
2. Provides correct examples
3. References AGENTS.md section 4.4
```

#### Step 3: Coder (Fix)
```
Human: "Fix the CActivity initialization pattern"

Coder Agent:
1. Removes initializeDefaults() call from JPA constructor
2. Verifies business constructor calls it correctly
3. Ensures no other violations introduced
4. Formats code with Spotless
```

#### Step 4: Verifier (Validation)
```
Human: "Re-verify CActivity"

Verifier Agent:
1. Re-runs checks
2. Confirms violation fixed
3. Runs unit tests (CActivityServiceTest)
4. Runs Playwright tests (keyword: activity)
5. Reports: ✅ ALL CHECKS PASSED
```

---

### Example 3: Refactoring to Abstract Pattern

#### Step 1: Pattern Designer (Design)
```
Human: "We have 5 different node types (CAN, LIN, Ethernet, etc.) with 80% shared code. Design abstract pattern"

Pattern Designer Agent:
1. Analyzes existing node implementations
2. Identifies common structure:
   - Fields: enabled, nodeType, config
   - Methods: validate(), initialize()
3. Designs pattern:
   - Abstract entity: CBabNode<T> with @MappedSuperclass
   - Abstract service: CBabNodeService<T> (NO @Service)
   - Abstract repository: IBabNodeRepository<T> with @NoRepositoryBean
4. Creates pattern documentation with hierarchy diagram
5. Provides migration steps
```

#### Step 2: Documenter (Document)
```
Human: "Document the abstract node pattern"

Documenter Agent:
1. Creates pattern document:
   - docs/patterns/abstract-entity-pattern.md
2. Includes:
   - Purpose and benefits
   - When to use vs not use
   - Complete implementation steps
   - ✅ CORRECT / ❌ INCORRECT examples
   - Verification checklist
3. Updates AGENTS.md section 4.5
4. Creates migration guide
```

#### Step 3: Coder (Implement)
```
Human: "Refactor CBabNodeCAN to use abstract pattern"

Coder Agent:
1. Creates abstract base:
   - CBabNode<T> with @MappedSuperclass
   - CBabNodeService<T> (no @Service)
   - IBabNodeRepository<T> with @NoRepositoryBean
2. Refactors concrete class:
   - CBabNodeCAN extends CBabNode<CBabNodeCAN>
   - Moves common code to abstract base
   - Keeps CAN-specific code in concrete
3. Updates service and repository similarly
4. Formats and builds
```

#### Step 4: Verifier (Validate)
```
Human: "Verify the abstract pattern refactoring"

Verifier Agent:
1. Checks pattern compliance:
   - ✅ Abstract entity has @MappedSuperclass
   - ✅ Abstract service has NO @Service
   - ✅ Abstract repository has @NoRepositoryBean
   - ✅ Concrete classes properly annotated
2. Verifies HQL queries use concrete entity names
3. Runs tests for all node types
4. Reports success or violations
```

---

## 💡 Best Practices

### For AI Assistants

1. **Always start with AGENTS.md**
   - Read the master playbook first
   - Understand core principles
   - Identify applicable patterns

2. **Load correct agent definition**
   - Read `.github/agents/{agent}/....agent.md`
   - Review configuration in `config/settings.md`
   - Use provided templates

3. **Follow agent-specific output format**
   - Pattern Designer: Pattern analysis reports
   - Coder: Code with compliance checklist
   - Verifier: Verification reports with pass/fail
   - Documenter: Markdown with examples

4. **Use agent tools appropriately**
   - grep/glob for searching
   - edit/create for modifications
   - bash for builds/tests
   - view for reading existing code

5. **Coordinate with other agents**
   - Pattern Designer → Provides patterns → Coder
   - Coder → Submits code → Verifier
   - All agents → Notify → Documenter

### For Developers

1. **Run verifier before committing**
   ```bash
   .github/agents/verifier/scripts/verify-code.sh
   ```

2. **Use selective testing**
   ```bash
   .github/agents/verifier/scripts/test-selective.sh activity
   ```

3. **Check patterns before implementing**
   ```bash
   .github/agents/pattern-designer/scripts/analyze-patterns.sh
   ```

4. **Format code**
   ```bash
   mvn spotless:apply
   ```

5. **Build with agents profile**
   ```bash
   mvn clean compile -Pagents -DskipTests
   ```

## 🎓 Training New AI Agents

### Phase 1: Orientation (30 minutes)
1. Read AGENTS.md completely
2. Understand technology stack
3. Review project structure
4. Study core architecture principles

### Phase 2: Specialization (1 hour)
1. Choose agent role (Pattern Designer / Coder / Verifier / Documenter)
2. Read agent definition thoroughly
3. Review configuration and templates
4. Study agent-specific examples

### Phase 3: Practice (2 hours)
1. Analyze existing code with your agent role
2. Try sample tasks from "When to Use" section
3. Generate output following agent format
4. Verify against checklists

### Phase 4: Integration (1 hour)
1. Practice multi-agent workflows
2. Coordinate with other agents
3. Generate complete feature implementation
4. Review quality standards

### Total Training Time: ~4 hours

## 📊 Quality Metrics

Track these metrics for each agent:

### Pattern Designer
- [ ] Patterns identified accurately
- [ ] Documentation complete with examples
- [ ] Migration guides provided
- [ ] Cross-references correct

### Coder
- [ ] Code compiles on first attempt
- [ ] All pattern rules followed
- [ ] Zero TODOs left
- [ ] Tests pass

### Verifier
- [ ] All checks executed
- [ ] Violations correctly identified
- [ ] Fixes suggested are accurate
- [ ] Report format consistent

### Documenter
- [ ] Documentation clear and complete
- [ ] Code examples compile
- [ ] AGENTS.md updated when needed
- [ ] Cross-references accurate

## 🔧 Troubleshooting

### Agent Not Following Rules
**Problem**: Agent generates non-compliant code

**Solution**:
1. Verify agent has loaded AGENTS.md
2. Check agent definition is complete
3. Review configuration settings
4. Use Verifier agent to identify violations

### Build Failures
**Problem**: Code doesn't compile

**Solution**:
1. Run: `mvn clean compile -Pagents -DskipTests`
2. Check error messages in `/tmp/build.log`
3. Verify imports (not fully-qualified names)
4. Run: `mvn spotless:apply`

### Test Failures
**Problem**: Playwright tests fail

**Solution**:
1. Use selective testing: `-Dtest.routeKeyword=activity`
2. Check logs: `/tmp/playwright-{keyword}.log`
3. Verify UI component IDs present
4. Check initialization wiring

### Pattern Confusion
**Problem**: Unclear which pattern applies

**Solution**:
1. Use Pattern Designer agent to analyze
2. Check AGENTS.md section 4 (Entity Patterns)
3. Review similar existing implementations
4. Consult BAB_CODING_RULES.md for BAB profile

## 📞 Getting Help

1. **Check agent definition**: `.github/agents/{agent}/....agent.md`
2. **Review AGENTS.md**: Root-level master playbook
3. **Consult configuration**: `config/settings.md`
4. **Run analysis tools**: Scripts in `scripts/` directory
5. **Check existing patterns**: `docs/patterns/`

## 🎉 Success Criteria

You've successfully mastered the agents when:

- [ ] Can identify correct agent for any task
- [ ] Generate compliant code on first attempt
- [ ] Write accurate verification reports
- [ ] Create clear pattern documentation
- [ ] Coordinate multi-agent workflows
- [ ] All builds pass
- [ ] All tests pass (selective testing)
- [ ] Documentation complete and accurate

---

**Remember**: Agents are specialized tools. Use the right agent for each task. Follow workflows for complex features. Quality over speed.

**SSC WAS HERE!! 🌟 Brilliant guidance for future AI agents!**
