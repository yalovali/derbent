# Documenter Agent Configuration

## Agent Settings
- **Mode**: Documentation Creation & Maintenance
- **Primary Tools**: edit, create, view, grep, glob
- **Output Format**: Markdown documentation
- **Style Guide**: Derbent documentation standards

## Documentation Templates Location
- Pattern Template: `.github/agents/documenter/templates/pattern-template.md`
- Feature Template: `.github/agents/documenter/templates/feature-template.md`
- Migration Template: `.github/agents/documenter/templates/migration-template.md`

## Documentation Standards

### File Naming Conventions
- **Pattern Docs**: `{pattern-name}-pattern.md`
- **Implementation Docs**: `{feature-name}-implementation.md`
- **Guide Docs**: `{topic}-guide.md`
- **Summary Docs**: `{feature-name}-summary.md`

### Directory Structure
```
docs/
├── README.md                    # Project overview
├── architecture/                # Design patterns
├── development/                 # Development guides
├── implementation/              # Implementation details
├── testing/                     # Testing guides
└── patterns/                    # Pattern library
```

## Code Example Standards

### Entity Example Template
```java
/**
 * [Entity description]
 * 
 * @author [Author]
 * @since [Version]
 */
@Entity
@Table(name = "c{table}")
public class C{Entity} extends C{Parent}<C{Entity}> {
    // Constants (MANDATORY)
    public static final String DEFAULT_COLOR = "#{color}";
    public static final String DEFAULT_ICON = "vaadin:{icon}";
    public static final String ENTITY_TITLE_SINGULAR = "{Entity}";
    public static final String ENTITY_TITLE_PLURAL = "{Entities}";
    public static final String VIEW_NAME = "{Entities} View";
    
    // Implementation...
}
```

### Service Example Template
```java
/**
 * Service for [purpose].
 * 
 * @author [Author]
 * @since [Version]
 */
@Service
@PreAuthorize("isAuthenticated()")
public class C{Entity}Service extends C{Parent}Service<C{Entity}> {
    // Implementation...
}
```

## Markdown Formatting Rules

### Headers
- H1 (`#`) - Document title only
- H2 (`##`) - Major sections
- H3 (`###`) - Subsections
- H4 (`####`) - Details

### Code Blocks
- Always specify language: ```java, ```bash, ```markdown
- Include comments in code examples
- Use ✅ CORRECT and ❌ INCORRECT labels

### Lists
- Use `-` for unordered lists
- Use `1.` for ordered lists
- Use `- [ ]` for checklists

### Emphasis
- **Bold** for important terms (first use)
- *Italic* for emphasis
- `Code` for inline code/commands
- **RULE**: for mandatory rules

## Quality Checklist

Before publishing documentation:
- [ ] Purpose stated clearly
- [ ] Code examples compile
- [ ] All sections complete
- [ ] Links verified
- [ ] Consistent terminology
- [ ] Spelling/grammar checked
- [ ] Examples follow AGENTS.md rules

## AGENTS.md Update Process

1. Identify section to update
2. Draft change with examples
3. Add to appropriate section
4. Update table of contents if needed
5. Cross-reference related docs
6. Commit with clear message

## Documentation Metrics

Track these for each major feature:
- [ ] Entity documented
- [ ] Service documented
- [ ] UI components documented
- [ ] Usage examples provided
- [ ] Testing strategy documented
- [ ] Configuration documented

## Integration Workflow

1. **Pattern Designer** creates pattern → Document pattern
2. **Coder** implements feature → Document implementation
3. **Verifier** finds issues → Document common mistakes
4. **All agents** → Update AGENTS.md if new pattern

## Output Checklist

When creating documentation:
- [ ] File created in correct directory
- [ ] Follows template structure
- [ ] Code examples complete
- [ ] Cross-references added
- [ ] AGENTS.md updated (if needed)
- [ ] Commit message descriptive
