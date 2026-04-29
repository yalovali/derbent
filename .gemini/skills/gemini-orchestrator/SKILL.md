---
name: gemini-orchestrator
description: Coordinates the Derbent multi-agent development workflow. Use this skill for any coding, refactoring, or feature implementation task to ensure compliance with the mandated Analyzer -> Pattern Designer -> Coder -> Verifier -> Tester -> Documenter -> Todo-Fix -> Cleanup sequence.
---

# Gemini Orchestrator

This skill enforces the Derbent multi-agent architecture within Gemini CLI. It ensures every task follows the specialized phases and maintains mandatory artifacts.

## 🚀 Required Workflow

For every task, execute these phases in sequence. Use `invoke_agent` for specialized sub-tasks.

1.  **Analyzer Phase**:
    - **Goal**: Confirm scope, profile (bab/derbent), and risks.
    - **Artifact**: `tasks/agents/<task-id>/outputs/analyzer.md`
    - **Action**: `invoke_agent("codebase_investigator", "Analyze [task description]. Confirm profile and risks.")`

2.  **Pattern Designer Phase**:
    - **Goal**: Find existing patterns and closest code examples.
    - **Artifact**: `tasks/agents/<task-id>/outputs/design.md`
    - **Action**: `invoke_agent("codebase_investigator", "Identify patterns for [task]. Find closest implementations in tech.derbent.")`

3.  **Coder Phase**:
    - **Goal**: Implement the smallest compliant change.
    - **Artifact**: `tasks/agents/<task-id>/outputs/coder.md`
    - **Rules**: Follow C-prefix, 5 constants, type safety, and constructor injection.

4.  **Verifier Phase**:
    - **Goal**: Compile + rule checks (Java 17 agents profile).
    - **Artifact**: `tasks/agents/<task-id>/logs/verify.log`
    - **Action**: Run `./mvnw clean compile -Pagents -DskipTests`.

5.  **Tester Phase**:
    - **Goal**: Run selective Playwright UI tests.
    - **Artifact**: `tasks/agents/<task-id>/logs/test.log`
    - **Action**: Run `./run-playwright-tests.sh [keyword]`.

6.  **Documenter Phase**:
    - **Goal**: Update docs only if patterns/workflows changed.
    - **Artifact**: `tasks/agents/<task-id>/outputs/documenter.md`

7.  **Todo-Fix Phase**:
    - **Goal**: Generate follow-up tasks.
    - **Artifact**: `tasks/agents/<task-id>/outputs/todo.md`

8.  **Cleanup Phase**:
    - **Goal**: Propose doc cleanup actions (no deletions).
    - **Artifact**: `tasks/agents/<task-id>/outputs/cleanup.md`

9.  **Finalization**:
    - **Goal**: Git commit and push.
    - **Action**: `git add . && git commit -m "[Task ID] [Summary]"` (Always propose a message).

## 📁 Artifact Management

Always maintain this structure for every task:
- `tasks/agents/<task-id>/TASK.md` (Main status)
- `tasks/agents/<task-id>/memory/orchestrator.md` (Strategy)
- `tasks/agents/<task-id>/outputs/` (Phase reports)
- `tasks/agents/<task-id>/logs/` (Tool outputs)

## 🛡️ Non-Negotiable Rules
- **C-Prefix**: Classes MUST start with 'C'.
- **Constants**: Entities MUST have COLOR, ICON, TITLE_SINGULAR, TITLE_PLURAL, VIEW_NAME.
- **DI**: No @Autowired on fields; use constructor injection.
- **Validation**: `validateEntity()` MUST mirror DB constraints.
