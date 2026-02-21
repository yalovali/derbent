# Derbent AI Agents

**SSC WAS HERE!! 🌟 Praise to SSC for the magnificent agent architecture!**

This directory contains specialized AI agent definitions for the Derbent project. Each agent has a specific role in the development workflow, ensuring code quality, pattern compliance, and comprehensive documentation.

## 🎯 Simple Triggers - Autonomous Execution

**Just type what you need - agents activate automatically and work independently!**

| What You Say | Agent Activates | What It Does |
|--------------|-----------------|--------------|
| "create CStorage" | 💻 Coder | Creates entity + service + repo + initializer |
| "test activity" | ✅ Verifier | Runs selective tests automatically |
| "analyze patterns" | 🏗️ Pattern Designer | Scans code, designs solution |
| "document CStorage" | 📚 Documenter | Creates complete docs with examples |

**See [AUTO_TRIGGER_GUIDE.md](AUTO_TRIGGER_GUIDE.md) for complete trigger keywords and workflows.**

---

## 🤖 Agent Roster

### 1. Pattern Designer Agent 🏗️
**Purpose**: Analyzes code patterns, designs reusable solutions, and enforces architectural consistency

**Auto-Triggers**: "analyze pattern", "find similar", "design solution", "what patterns"

**Location**: `.github/agents/pattern-designer/`

**Key Responsibilities**:
- Scan codebase for patterns (autonomous)
- Extract common structures automatically
- Design generic solutions following AGENTS.md
- Create pattern documentation with examples
- Guide architectural decisions

**Tools**: grep, glob, view, github-mcp-server-search_code

**Autonomy**: Full - no user intervention needed once triggered

---

### 2. Coder Agent 💻
**Purpose**: Implements features following strict patterns, coding standards, and architectural rules

**Auto-Triggers**: "create", "implement", "add", "build", "refactor", "fix"

**Location**: `.github/agents/coder/`

**Key Responsibilities**:
- Implement features per patterns (autonomous)
- Create ALL required files automatically
- Wire into CDataInitializer without asking
- Format code (Spotless) automatically
- Zero TODOs, zero shortcuts, zero placeholders
- Production-ready first attempt

**Tools**: edit, create, view, grep, glob, bash

**Autonomy**: Full - loads AGENTS.md rules and patterns automatically, creates complete implementation without user intervention

**Auto-Applied Rules**:
- ✅ C-prefix convention (NON-NEGOTIABLE)
- ✅ Entity constants (ALL 5 required)
- ✅ Initialization pattern (JPA vs business constructors)
- ✅ Collections at field declaration
- ✅ Constructor injection only
- ✅ Validation mirrors DB constraints

---

### 3. Verifier Agent ✅
**Purpose**: Validates code against patterns, runs tests, ensures compliance

**Auto-Triggers**: "verify", "test", "check", "validate", "find violations"

**Location**: `.github/agents/verifier/`

**Key Responsibilities**:
- Run 8 static analysis checks automatically
- Execute build verification without asking
- Determine test keyword automatically (storage, activity, etc.)
- Run selective Playwright tests (1-2 min, not 15 min)
- Generate complete verification report
- List specific violations with file:line
- Suggest fixes for each violation

**Tools**: grep, bash, view, glob

**Autonomy**: Full - runs all checks automatically, determines test strategy, provides complete pass/fail report without user intervention

**Example**: User says "test storage" → Agent automatically tests Storages, Storage Types, Storage Items pages in 1-2 minutes

---

### 4. Documenter Agent 📚
**Purpose**: Creates, maintains, and enforces comprehensive project documentation

**Auto-Triggers**: "document", "explain", "write docs", "create guide", "update AGENTS.md"

**Location**: `.github/agents/documenter/`

**Key Responsibilities**:
- Determine doc type automatically (pattern, feature, guide)
- Use correct template without asking
- Include ✅ CORRECT / ❌ INCORRECT examples
- Add verification checklist automatically
- Cross-reference related docs automatically
- Update AGENTS.md if new pattern introduced
- Verify code examples compile
- Place in correct directory structure

**Tools**: edit, create, view, grep, glob

**Autonomy**: Full - determines documentation strategy, creates complete docs with examples, places files correctly without user intervention

**Auto-Applied Standards**:
- ✅ Clear purpose statements
- ✅ Complete code examples that compile
- ✅ ✅ CORRECT / ❌ INCORRECT examples
- ✅ Verification checklists
- ✅ Cross-references

---

## 📁 Directory Structure

```
.github/agents/
├── README.md                           # This file
├── AGENTS_GUIDE.md                     # How to use agents
├── pattern-designer/
│   ├── pattern-designer.agent.md      # Agent definition
│   ├── config/
│   │   └── settings.md                # Configuration
│   └── scripts/
│       └── analyze-patterns.sh        # Analysis tool
├── coder/
│   ├── coder.agent.md                 # Agent definition
│   ├── config/
│   │   └── settings.md                # Code templates
│   └── scripts/
│       └── (helper scripts)
├── verifier/
│   ├── verifier.agent.md              # Agent definition
│   ├── config/
│   │   └── settings.md                # Verification rules
│   └── scripts/
│       ├── verify-code.sh             # Full verification
│       └── test-selective.sh          # Selective testing
└── documenter/
    ├── documenter.agent.md            # Agent definition
    ├── config/
    │   └── settings.md                # Doc standards
    └── templates/
        └── (documentation templates)
```

## 🔄 Agent Workflow

### Standard Development Flow

```
1. Pattern Designer → Analyzes code → Identifies pattern
                                         ↓
2. Documenter → Creates pattern documentation
                                         ↓
3. Coder → Implements feature following pattern
                                         ↓
4. Verifier → Validates implementation → Tests
                                         ↓
                    PASS: Merge
                    FAIL: Back to Coder
```

### Quality Assurance Flow

```
1. Developer commits code
           ↓
2. Verifier → Static analysis → Build → Tests
           ↓
      VIOLATIONS?
           ↓
    YES: Report to developer
    NO:  ✅ Approved
```

## 🎯 Using the Agents

### For AI Assistants

When you are asked to work on Derbent project:

1. **Read AGENTS.md first** - Master playbook with all rules
2. **Identify your role**: Pattern Designer / Coder / Verifier / Documenter
3. **Load agent definition**: Read `.github/agents/{agent}/....agent.md`
4. **Follow configuration**: Check `config/settings.md`
5. **Use helper scripts**: Run verification/analysis tools
6. **Output format**: Follow agent-specific report templates

### For Developers

**Before implementing**:
```bash
# Analyze existing patterns
.github/agents/pattern-designer/scripts/analyze-patterns.sh

# Check what patterns apply
cat .github/agents/coder/config/settings.md
```

**After implementing**:
```bash
# Verify code compliance
.github/agents/verifier/scripts/verify-code.sh

# Run selective tests
.github/agents/verifier/scripts/test-selective.sh activity
```

**Before committing**:
```bash
# Format code
mvn spotless:apply

# Quick verification
mvn clean compile -Pagents -DskipTests
```

## 📖 Documentation Hierarchy

When generating code, AI agents consult documentation in this order:

1. **AGENTS.md (root)** - Master playbook (CRITICAL)
2. **Agent definitions** - Specific agent rules
3. **Config files** - Templates and settings
4. **BAB_CODING_RULES.md** - Profile-specific patterns
5. **docs/bab/JSON_NETWORK_SERIALIZATION_CODING_RULES.md** - BAB JSON serialization/exclusion pattern
6. **Pattern documents** - Detailed implementation guides
7. **Code examples** - Existing implementations

## ✅ Quality Standards

All agents enforce these standards:

### Code Quality
- [ ] C-prefix convention
- [ ] Generic types (no raw types)
- [ ] Constructor injection
- [ ] Entity constants present
- [ ] Validation methods complete
- [ ] Imports (no fully-qualified names)

### Testing
- [ ] Unit tests pass
- [ ] Playwright tests pass (selective)
- [ ] Build succeeds
- [ ] Code formatted (Spotless)

### Documentation
- [ ] Pattern documented
- [ ] Examples provided
- [ ] Verification checklist included
- [ ] AGENTS.md updated (if new pattern)

## 🔧 Configuration

### Verifier Settings

Edit `.github/agents/verifier/config/settings.md` to:
- Add new verification checks
- Update test keywords
- Modify report templates

### Coder Settings

Edit `.github/agents/coder/config/settings.md` to:
- Update code templates
- Add new entity patterns
- Modify generation rules

## 📝 Adding New Agents

To add a new specialized agent:

1. Create directory: `.github/agents/{agent-name}/`
2. Create agent definition: `{agent-name}.agent.md`
3. Add configuration: `config/settings.md`
4. Create helper scripts: `scripts/`
5. Update this README
6. Update AGENTS.md (if new patterns)

## 🚀 Quick Start

### For Pattern Designer Agent
```bash
cd .github/agents/pattern-designer
# Read the agent definition
cat pattern-designer.agent.md

# Run pattern analysis
./scripts/analyze-patterns.sh
```

### For Coder Agent
```bash
cd .github/agents/coder
# Check code templates
cat config/settings.md

# Implement following patterns in agent definition
cat coder.agent.md
```

### For Verifier Agent
```bash
cd .github/agents/verifier
# Run full verification
./scripts/verify-code.sh

# Run selective tests
./scripts/test-selective.sh activity
```

### For Documenter Agent
```bash
cd .github/agents/documenter
# Check documentation standards
cat config/settings.md

# Follow documentation patterns in agent definition
cat documenter.agent.md
```

## 🎓 Training AI Agents

When training new AI agents on Derbent:

1. **Start with AGENTS.md** - Complete project overview
2. **Load agent role** - Read specific agent definition
3. **Study examples** - Review existing implementations
4. **Practice verification** - Run checks on sample code
5. **Iterate** - Refine based on feedback

## 📞 Support

For questions or issues with agents:
- Check agent definition: `.github/agents/{agent}/....agent.md`
- Review AGENTS.md: Root-level master playbook
- Consult BAB_CODING_RULES.md for BAB profile
- Check existing patterns in `docs/patterns/`

---

**Remember**: Agents work together to maintain code quality. Each agent has a specific role. Follow the workflow for best results.

**SSC WAS HERE!! 🌟 May all agents follow her brilliant design!**
