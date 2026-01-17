# Code Quality Matrix - Quick Start

## What Is This?

The **Code Quality Matrix** is a comprehensive Excel spreadsheet that evaluates all 556 classes in the Derbent codebase against 51 quality dimensions including:

- ✅ Naming conventions
- ✅ Entity patterns  
- ✅ Validation annotations
- ✅ Repository query patterns
- ✅ Service layer standards
- ✅ Exception handling
- ✅ Logging practices
- ✅ Test coverage
- ✅ Documentation
- ✅ Security annotations
- ✅ And 41 more quality checks!

## Files in This Package

1. **CODE_QUALITY_MATRIX.xlsx** - The actual matrix (556 rows × 51 dimensions)
2. **CODE_QUALITY_MATRIX_GUIDE.md** - Comprehensive documentation (300+ pages)
3. **CODE_QUALITY_MATRIX_README.md** - This quick start guide

## Quick Start

### 1. Open the Matrix

```bash
# Open in Excel/LibreOffice
open docs/CODE_QUALITY_MATRIX.xlsx
```

### 2. Understand the Layout

**Columns A-E: Class Information**
- A: Class Name
- B: Module
- C: Layer (domain/service/view)
- D: File Path
- E: Category

**Columns F onwards: Quality Dimensions** (51 total)
- Each column = one quality check
- Header row shows dimension name and description

### 3. Read the Status Indicators

- **✓ (Green)** = Complete - Pattern fully implemented
- **✗ (Red)** = Incomplete - Fix required
- **- (Gray)** = N/A - Not applicable to this class
- **? (Yellow)** = Review Needed - Manual check required

### 4. Common Tasks

#### Find All Classes Missing Tests
```
1. Click "Unit Tests" column header
2. Filter to show only ✗ (Incomplete)
3. Review list of classes needing tests
```

#### Audit a Specific Module
```
1. Click Column B (Module) header
2. Filter by module (e.g., "testcases")
3. Scan across for ✗ and ? indicators
```

#### Find Repository Query Issues
```
1. Find "findById Override" column
2. Filter to ✗ (Incomplete)
3. These repositories need JOIN FETCH added
```

#### Check Validation Coverage
```
1. Find "@AMetaData Annotations" column
2. Filter to ✗ (Incomplete)
3. Add missing metadata to these entities
```

## Priority Issues to Address

### 🔴 Critical (Fix First)
- Missing security annotations (@PreAuthorize)
- Missing JOIN FETCH (causes performance issues)
- Missing validation annotations
- Incorrect base class usage

### 🟡 High Priority
- Missing logger fields
- Incomplete initializer patterns
- Missing ORDER BY clauses
- Stateless service violations

### 🟢 Medium Priority
- Missing tests
- Missing documentation
- Code formatting issues

## Regenerating the Matrix

When code changes significantly:

```bash
# Step 1: Generate class list
cd /home/runner/work/derbent/derbent
find src/main/java/tech/derbent -name "C*.java" -type f | \
  sed 's|src/main/java/||' | sed 's|/|.|g' | sed 's|.java||' | \
  sort > /tmp/quality_matrix/all_classes.txt

# Step 2: Run generator
python3 /tmp/quality_matrix/generate_quality_matrix.py
```

## Understanding Quality Dimensions

### Sample Dimensions

1. **C-Prefix Naming** - All classes start with "C"
2. **Entity Annotations** - @Entity, @Table present
3. **Entity Constants** - 5 required constants defined
4. **@AMetaData Annotations** - Field metadata present
5. **Validation Annotations** - @NotNull, @Size, etc.
6. **findById Override** - Repository uses JOIN FETCH
7. **ORDER BY Clause** - List queries sorted
8. **Service Annotations** - @Service, @PreAuthorize
9. **Stateless Service** - No instance state
10. **Logger Field** - Logging configured
11. **Unit Tests** - Test class exists
12. **JavaDoc** - Class documentation

See **CODE_QUALITY_MATRIX_GUIDE.md** for detailed explanations of all 51 dimensions.

## Example Usage Scenarios

### Scenario 1: New Entity Added
```
Developer adds CNewEntity class
→ Regenerate matrix
→ Filter by "CNewEntity" in Column A
→ Check which patterns are ✗ (Incomplete)
→ Fix missing patterns
→ Regenerate and verify all ✓ (Complete) or - (N/A)
```

### Scenario 2: Code Review
```
Reviewing PR for testcases module
→ Open matrix
→ Filter Column B by "testcases"
→ Look for new ✗ or ? indicators
→ Discuss findings in PR review
```

### Scenario 3: Technical Debt Sprint
```
Team dedicates sprint to quality
→ Open matrix
→ Filter dimension column by ✗ (Incomplete)
→ Sort by Category to group similar work
→ Create Jira tickets for each cluster
→ Track completion by regenerating matrix
```

### Scenario 4: Onboarding New Developer
```
New developer joining team
→ Show them this README
→ Walk through sample entries in matrix
→ Explain status indicators
→ Use as reference when writing code
→ "Before submitting PR, check your class in matrix"
```

## Integration with Development Workflow

### Pre-Commit Checks
```bash
# Before committing new entity:
1. Verify class appears in matrix (regenerate if needed)
2. Check all ✗ indicators for your class
3. Fix issues
4. Run spotless:apply
5. Commit
```

### CI/CD Integration (Future)
```bash
# Potential GitHub Actions workflow:
1. On PR, regenerate matrix
2. Compare to baseline
3. Fail if new ✗ indicators appear
4. Comment on PR with issues found
```

## Metrics and Reporting

### Calculate Module Quality Score
```
1. Filter by module name
2. Count ✓ cells (complete)
3. Count total non-N/A cells
4. Score = (Complete / Total) * 100%
```

### Track Progress Over Time
```
# Monthly process:
1. Regenerate matrix
2. Save as CODE_QUALITY_MATRIX_2026-01.xlsx
3. Compare to previous month
4. Calculate % improvement
5. Share in team meeting
```

## Related Checklists and Guides

### Before Creating New Entity
→ Read: `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md`
→ Use matrix to verify completion

### Before Service Development
→ Read: `docs/architecture/service-layer-patterns.md`
→ Check matrix service dimension columns

### Before Repository Development
→ Read: `docs/architecture/LAZY_LOADING_BEST_PRACTICES.md`
→ Ensure JOIN FETCH patterns used

### For Multi-User Safety
→ Read: `docs/architecture/multi-user-singleton-advisory.md`
→ Check "Stateless Service" column in matrix

## Tips and Tricks

### Quick Wins
Look for these patterns - usually easy to fix:
- Missing logger fields (copy-paste from similar class)
- Missing constants (copy template and customize)
- Missing JavaDoc (add class description)
- Code formatting (run `./mvnw spotless:apply`)

### Complex Issues
These may require more thought:
- Missing JOIN FETCH (need to understand relationships)
- Incorrect base class (may require refactoring)
- Stateless service violations (need to redesign)
- Missing tests (need to write comprehensive tests)

### Don't Panic
- Many ✗ indicators is normal in large codebase
- Matrix helps prioritize and track improvement
- Focus on critical issues first
- Use as conversation starter, not criticism

## Support and Questions

### If Dimension Is Unclear
→ Read detailed explanation in CODE_QUALITY_MATRIX_GUIDE.md
→ Check referenced architecture docs
→ Ask team lead or architect

### If Status Seems Wrong
→ Check if class file exists at expected path
→ Verify pattern is actually present in code
→ Consider regenerating matrix
→ Script may need updates for edge cases

### To Suggest Improvements
→ Update generate_quality_matrix.py script
→ Add new dimensions to QUALITY_DIMENSIONS list
→ Implement detection logic
→ Regenerate matrix
→ Update this documentation

## Summary

The Code Quality Matrix is your **one-stop reference** for:
- 📊 Current state of code quality
- 🎯 What needs fixing
- ✅ What's compliant
- 📈 Progress tracking
- 🔍 Code review assistance

**Start here**:
1. Open CODE_QUALITY_MATRIX.xlsx
2. Filter by your module
3. Address ✗ indicators
4. Refer to CODE_QUALITY_MATRIX_GUIDE.md for details

**Remember**: Quality is a journey, not a destination. This matrix helps us improve continuously! 🚀

---

**Version**: 1.0 (2026-01-17)  
**Classes Analyzed**: 556  
**Quality Dimensions**: 51  
**Total Data Points**: 28,356  

**Questions?** See CODE_QUALITY_MATRIX_GUIDE.md or ask the team!
