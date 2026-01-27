# Pattern Designer Configuration

## Agent Settings
- **Mode**: Analysis & Design
- **Primary Tools**: grep, glob, view, github-mcp-server-search_code
- **Output Format**: Markdown with code examples
- **Verification**: Pattern compliance checks

## Search Patterns

### Entity Pattern Searches
```bash
# Find all entity classes
grep -r "public class C.*extends.*<" src/main/java --include="*.java"

# Find entities with specific interfaces
grep -r "implements IHasAttachments" src/main/java --include="*.java"
grep -r "implements IHasStatusAndWorkflow" src/main/java --include="*.java"
grep -r "implements IHasComments" src/main/java --include="*.java"

# Find abstract entities
grep -r "@MappedSuperclass" src/main/java --include="*.java"
```

### Service Pattern Searches
```bash
# Find all services
grep -r "@Service" src/main/java --include="*.java"

# Find services with specific patterns
grep -r "extends CEntityOfProjectService" src/main/java --include="*.java"
grep -r "extends CEntityOfCompanyService" src/main/java --include="*.java"

# Find validation implementations
grep -r "protected void validateEntity" src/main/java --include="*.java"
```

### Repository Pattern Searches
```bash
# Find repositories
find src/main/java -name "I*Repository.java"

# Find abstract repositories
grep -r "@NoRepositoryBean" src/main/java --include="*.java"

# Find HQL queries
grep -r "@Query" src/main/java --include="*.java"
```

## Analysis Workflow

1. **Scan**: Use search patterns to find implementations
2. **Group**: Categorize by pattern type
3. **Extract**: Identify common structures
4. **Document**: Create pattern documentation
5. **Verify**: Check against AGENTS.md rules

## Output Templates

### Pattern Analysis Report
```markdown
# Pattern Analysis: [Pattern Name]

## Implementations Found
- [File 1] - [Brief description]
- [File 2] - [Brief description]

## Common Structure
[Shared code structure]

## Variations
1. [Variation 1] - [Context]
2. [Variation 2] - [Context]

## Recommendation
[Suggested generic pattern]
```

## Quality Checks

- [ ] Pattern aligns with AGENTS.md rules
- [ ] Examples include ✅ CORRECT and ❌ INCORRECT
- [ ] Verification checklist provided
- [ ] Related patterns cross-referenced
- [ ] Migration guide included (if applicable)
