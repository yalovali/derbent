# GitHub Copilot CLI Configuration Guide

**Date**: 2026-01-15  
**Purpose**: How to configure allowed commands, paths, and tools for GitHub Copilot CLI

## Overview

GitHub Copilot CLI uses a **permissions-based system** rather than a traditional config file. You configure allowed commands through command-line flags, slash commands, and session-based approvals.

---

## ⚙️ Configuration Methods

### 1. Command-Line Flags (Startup Configuration)

Configure permissions when starting Copilot CLI:

```bash
# Allow all paths (disable path verification)
gh copilot --allow-all-paths

# Allow all URLs (disable URL verification)
gh copilot --allow-all-urls

# Allow specific domains
gh copilot --allow-url github.com --allow-url npmjs.com

# Combine multiple flags
gh copilot --allow-all-paths --allow-url github.com
```

### 2. Slash Commands (Runtime Configuration)

Modify permissions during an active session:

```bash
# Add a trusted directory
/add-dir /home/yasin/projects/my-app

# List all allowed directories
/list-dirs

# Change working directory
/cwd /path/to/project

# Reset allowed tools (requires re-approval)
/reset-allowed-tools
```

### 3. Interactive Approvals (Session-Based)

During a session, Copilot asks for approval when using potentially dangerous tools:

**Tool Approval Options**:
1. **Yes** - Allow once (asks again next time)
2. **Yes, and approve TOOL for the rest of the running session** - Allow for entire session
3. **No, and tell Copilot what to do differently (Esc)** - Deny and stop operation

---

## 📁 Path Permissions

### Default Behavior

By default, Copilot CLI can access:
- ✅ Current working directory
- ✅ Subdirectories of current working directory
- ✅ System temp directory

### Path Permission Applies To

- Shell commands (`bash`, `cp`, `mv`, `rm`, etc.)
- File operations (`view`, `create`, `edit`)
- Search tools (`grep`, `glob`, `find`)

### Adding Trusted Directories

**During Session**:
```bash
/add-dir /home/yasin/git/derbent
/add-dir /home/yasin/projects
```

**At Startup** (via working directory):
```bash
cd /home/yasin/git/derbent
gh copilot
```

**Disable Path Verification**:
```bash
gh copilot --allow-all-paths
```

⚠️ **Warning**: `--allow-all-paths` grants access to entire filesystem

### Path Detection Limitations

Be aware of these limitations:
- ❌ Paths in complex shell constructs may not be detected
- ❌ Custom environment variables (e.g., `$MY_PROJECT_DIR`) are not expanded
- ❌ Symlinks for new files are not resolved
- ✅ Standard variables (`$HOME`, `$TMPDIR`, `$PWD`) ARE expanded

---

## 🌐 URL Permissions

### Default Behavior

By default, **all URLs require approval** before access is granted.

### URL Permission Applies To

- `web_fetch` tool
- Shell commands that access network:
  - `curl`
  - `wget`
  - `fetch`
  - HTTP libraries

### Pre-Approving Domains

**At Startup**:
```bash
# Allow specific domains
gh copilot --allow-url github.com --allow-url api.github.com

# Allow all URLs
gh copilot --allow-all-urls
```

**Example Use Cases**:
```bash
# For GitHub-focused work
gh copilot --allow-url github.com --allow-url githubusercontent.com

# For npm/Node.js development
gh copilot --allow-url npmjs.com --allow-url nodejs.org

# For Maven/Java development
gh copilot --allow-url maven.apache.org --allow-url mvnrepository.com
```

### URL Detection Limitations

Be aware of these limitations:
- ❌ URLs in file contents are not detected
- ❌ URLs in config files are not detected
- ❌ URLs in environment variables are not detected
- ❌ Obfuscated URLs may not be detected
- ⚠️ HTTP and HTTPS are treated as **different protocols** (need separate approval)

---

## 🔧 Tool Permissions

### Tool Approval System

When Copilot wants to use a potentially dangerous tool, it asks for approval:

**Examples of Tools That Require Approval**:
- File modification: `touch`, `rm`, `mv`, `cp`, `chmod`
- Execution: `node`, `python`, `bash`, `sh`
- Text processing: `sed`, `awk`
- Network: `curl`, `wget`
- Build tools: `mvn`, `npm`, `gradle`

### Approval Strategies

**1. Per-Use Approval** (Most Secure)
```
Copilot wants to use: rm old-file.txt
Your choice: 1. Yes
```
- Asks every time
- Maximum security
- Best for destructive commands (`rm`, `chmod 777`)

**2. Session-Wide Approval** (Balanced)
```
Copilot wants to use: chmod +x script.sh
Your choice: 2. Yes, and approve chmod for the rest of the running session
```
- Approved for current session only
- Good for repetitive tasks
- Use for safe tools (`ls`, `cat`, `grep`)

**3. Reset Tools** (Security Reset)
```bash
/reset-allowed-tools
```
- Clears all session approvals
- Forces re-approval for all tools
- Use when changing contexts or tasks

---

## 📋 Configuration Examples

### Example 1: Secure Development Environment

```bash
# Start Copilot in project directory
cd /home/yasin/git/derbent

# Launch with restricted permissions
gh copilot

# During session, add only necessary directories
/add-dir /home/yasin/git/derbent/src
/add-dir /home/yasin/git/derbent/docs

# Approve tools on a per-use basis
# No pre-approved URLs
```

**Use When**: Working on sensitive code, production systems

### Example 2: Relaxed Development Environment

```bash
# Start Copilot with broader permissions
gh copilot --allow-all-paths --allow-url github.com --allow-url npmjs.com

# During session, approve common tools for session
# When prompted for mvn: Choose option 2 (approve for session)
# When prompted for git: Choose option 2 (approve for session)
```

**Use When**: Personal projects, learning, prototyping

### Example 3: Testing Environment

```bash
# Navigate to test project
cd /home/yasin/git/derbent

# Start with test-specific permissions
gh copilot --allow-url localhost --allow-url 127.0.0.1

# Add test directories
/add-dir /home/yasin/git/derbent/src/test
/add-dir /home/yasin/git/derbent/target
```

**Use When**: Running automated tests, CI/CD workflows

### Example 4: Documentation Work

```bash
# Start in documentation directory
cd /home/yasin/git/derbent/docs

# Allow documentation sites
gh copilot --allow-url docs.github.com --allow-url stackoverflow.com

# Restrict to docs directory only (already in cwd)
# No need for additional /add-dir commands
```

**Use When**: Writing documentation, research

---

## 🚀 Quick Start Scripts

### Create Alias for Common Configurations

Add to `~/.bashrc` or `~/.zshrc`:

```bash
# Secure mode (default)
alias copilot-secure='gh copilot'

# Development mode (relaxed)
alias copilot-dev='gh copilot --allow-all-paths --allow-url github.com'

# Testing mode
alias copilot-test='gh copilot --allow-url localhost'

# Documentation mode
alias copilot-docs='gh copilot --allow-url docs.github.com'
```

### Project-Specific Wrapper Scripts

Create `scripts/start-copilot.sh`:

```bash
#!/bin/bash
# Start Copilot with project-specific configuration

# Navigate to project root
cd "$(git rev-parse --show-toplevel)"

# Start Copilot with project-appropriate permissions
gh copilot \
  --allow-url github.com \
  --allow-url api.github.com \
  --allow-url maven.apache.org \
  --allow-url mvnrepository.com

# Note: Path is automatically set to current directory
```

Make executable:
```bash
chmod +x scripts/start-copilot.sh
```

Use:
```bash
./scripts/start-copilot.sh
```

---

## 🔒 Security Best Practices

### DO ✅

1. **Start with minimal permissions**
   ```bash
   gh copilot  # No flags, default permissions
   ```

2. **Add directories as needed**
   ```bash
   /add-dir /path/to/needed/directory
   ```

3. **Approve destructive tools per-use**
   - `rm`, `chmod 777`, `mv` → Option 1 (Yes, once)

4. **Use session approval for safe tools**
   - `ls`, `cat`, `grep` → Option 2 (Yes, for session)

5. **Review prompted commands before approval**
   - Read the full command Copilot wants to run
   - Understand the flags and arguments

6. **Reset tools when changing tasks**
   ```bash
   /reset-allowed-tools
   ```

### DON'T ❌

1. **Don't use `--allow-all-paths` on shared systems**
   ```bash
   gh copilot --allow-all-paths  # ❌ Risky on multi-user systems
   ```

2. **Don't approve `rm` for entire session**
   ```
   Approve rm for session? → NO ❌
   ```

3. **Don't pre-approve unknown domains**
   ```bash
   gh copilot --allow-url random-site.com  # ❌ Verify domain first
   ```

4. **Don't work in system directories**
   ```bash
   cd /etc && gh copilot  # ❌ Dangerous
   ```

5. **Don't blindly approve without reading**
   - Always read the command before approving

---

## 📊 Permission Management Commands

### View Current Configuration

```bash
# List allowed directories
/list-dirs

# Show session information (includes permissions)
/session

# Show usage and statistics
/usage
```

### Modify Configuration

```bash
# Add directory
/add-dir /path/to/directory

# Change working directory
/cwd /new/path

# Reset tool approvals
/reset-allowed-tools
```

### Exit and Restart

```bash
# Exit current session
/exit

# Start new session with different config
gh copilot --allow-url github.com
```

---

## 🎯 Common Use Cases

### Use Case 1: Code Review

```bash
cd /home/yasin/git/derbent
gh copilot

# In session:
Review the changes in @src/main/java/tech/derbent/api/

# Approve 'git diff' for session (safe, read-only)
# Approve 'grep' for session (safe, read-only)
```

### Use Case 2: Bug Fixing

```bash
cd /home/yasin/git/derbent
gh copilot --allow-url github.com

# In session:
Fix the bug in @src/main/java/MyClass.java

# Approve file edit operations per-use
# Approve 'mvn compile' for session
# Approve 'git' for session
```

### Use Case 3: New Feature Development

```bash
cd /home/yasin/git/derbent
gh copilot --allow-all-paths --allow-url github.com

# In session:
Create a new REST API endpoint for user authentication

# Approve 'touch' for session (creating files)
# Approve 'mkdir' for session (creating directories)
# Approve 'mvn' for session (testing)
```

### Use Case 4: Refactoring

```bash
cd /home/yasin/git/derbent/src
gh copilot

# In session:
Refactor the service layer to use dependency injection

# Approve file edits per-use (verify each change)
# Approve 'grep' for session (searching)
# Review before approving 'rm' or 'mv'
```

---

## 🔍 Troubleshooting

### "Permission Denied" Errors

**Problem**: Copilot cannot access a file or directory

**Solutions**:
```bash
# Check current allowed directories
/list-dirs

# Add the needed directory
/add-dir /path/to/directory

# OR restart with broader permissions
/exit
gh copilot --allow-all-paths
```

### "URL Not Allowed" Errors

**Problem**: Copilot cannot fetch from a URL

**Solutions**:
```bash
# Exit and restart with URL allowed
/exit
gh copilot --allow-url example.com

# OR allow all URLs (less secure)
gh copilot --allow-all-urls
```

### Tool Keeps Asking for Approval

**Problem**: Same tool requires approval repeatedly

**Solutions**:
1. **Choose option 2** when prompted: "Yes, and approve TOOL for the rest of the running session"
2. **For permanent solution**: There is no persistent configuration; tools must be approved each session

### Lost Tool Approvals

**Problem**: Need to re-approve tools after restart

**Explanation**: This is by design. Tool approvals are **session-based only** for security.

**Workaround**: Consider creating a shell alias or wrapper script to start Copilot with your preferred flags.

---

## 📚 Related Resources

- **Official Docs**: https://docs.github.com/en/copilot/how-tos/use-copilot-agents/use-copilot-cli
- **Custom Instructions**: `.github/copilot-instructions.md`
- **Custom Agents**: `.github/agents/`
- **MCP Servers**: `/mcp` command for Model Context Protocol integration

---

## 💡 Tips

1. **Session Management**: Use `/session` to see current session info including permissions
2. **Quick Restart**: Use `--continue` flag to resume last session
3. **Custom Agents**: Create agent profiles in `.github/agents/` for specialized workflows
4. **Environment Variables**: Set `COPILOT_CUSTOM_INSTRUCTIONS_DIRS` for additional instruction directories

---

## Summary

GitHub Copilot CLI does **not** use a traditional configuration file. Instead, it uses:

1. **Command-line flags** for startup configuration (`--allow-all-paths`, `--allow-url`)
2. **Slash commands** for runtime configuration (`/add-dir`, `/reset-allowed-tools`)
3. **Interactive approvals** for session-based tool permissions

This design prioritizes **security** and **explicit consent** over convenience, ensuring you're always aware of what Copilot is doing.

For persistent configuration, create **shell aliases** or **wrapper scripts** with your preferred flags.

---

**Last Updated**: 2026-01-15  
**Copilot CLI Version**: Latest (as of documentation)
