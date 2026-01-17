# Code Quality Scripts

This directory contains scripts for maintaining and analyzing code quality in the Derbent project.

## Scripts

### `regenerate_matrix.sh`

Regenerates the Code Quality Matrix from the current codebase.

**Usage:**
```bash
./scripts/quality/regenerate_matrix.sh
```

**What it does:**
1. Scans all C-prefixed Java classes (556 classes)
2. Analyzes each class against 51 quality dimensions
3. Generates Excel matrix: `docs/CODE_QUALITY_MATRIX.xlsx`

**When to run:**
- After adding new classes
- Monthly for quality tracking
- Before major releases
- When investigating code quality issues

**Dependencies:**
- Python 3
- openpyxl (installed automatically)

---

### `generate_quality_matrix.py`

Python script that performs the actual analysis and Excel generation.

**Direct usage:**
```bash
python3 scripts/quality/generate_quality_matrix.py
```

**Input:**
- `/tmp/quality_matrix/all_classes.txt` - List of classes to analyze
- Source files in `src/main/java/tech/derbent/`

**Output:**
- `docs/CODE_QUALITY_MATRIX.xlsx` - Excel spreadsheet with analysis

**Quality Dimensions Analyzed:**

1. **Naming and Structure** (2 dimensions)
   - C-Prefix naming convention
   - Package structure compliance

2. **Entity Patterns** (4 dimensions)
   - Entity annotations
   - Entity constants (5 required)
   - Base class extension
   - Interface implementation

3. **Field Annotations and Validation** (4 dimensions)
   - @AMetaData annotations
   - Validation annotations
   - Column annotations
   - Fetch strategy (LAZY)

4. **Constructor and Initialization** (3 dimensions)
   - Default constructor
   - Named constructor
   - initializeDefaults() method

5. **Repository Patterns** (4 dimensions)
   - Repository interface
   - findById override with JOIN FETCH
   - Query patterns (triple quotes)
   - ORDER BY clauses

6. **Service Patterns** (5 dimensions)
   - Service annotations
   - Service base class
   - Stateless service (multi-user safe)
   - getEntityClass() method
   - getInitializerService() method

7. **Initializer Patterns** (6 dimensions)
   - Initializer structure
   - createBasicView() method
   - createGridEntity() method
   - initialize() method
   - initializeSample() method
   - CDataInitializer registration

8. **Page Service Patterns** (2 dimensions)
   - Page service existence
   - Page service interfaces

9. **Exception Handling** (2 dimensions)
   - Exception pattern (Check.notNull)
   - User exception handling

10. **Logging** (3 dimensions)
    - Logger field
    - Logging pattern (ANSI format)
    - Log levels

11. **Interface Implementations** (3 dimensions)
    - IHasAttachments
    - IHasComments
    - IHasStatusAndWorkflow

12. **Code Quality** (3 dimensions)
    - Getter/setter pattern
    - No raw types
    - Constants naming

13. **Testing** (3 dimensions)
    - Unit tests
    - Integration tests
    - UI tests (Playwright)

14. **Documentation** (3 dimensions)
    - JavaDoc
    - Method documentation
    - Implementation docs

15. **Security** (2 dimensions)
    - Access control annotations
    - Tenant context usage

16. **Formatting** (2 dimensions)
    - Code formatting (eclipse-formatter.xml)
    - Import organization

**Total: 51 quality dimensions**

---

## Extending the Analysis

### Adding New Quality Dimensions

1. Edit `generate_quality_matrix.py`
2. Add dimension to `QUALITY_DIMENSIONS` list:
   ```python
   ("Dimension Name", "Description of what it checks"),
   ```

3. Add detection logic in `analyze_class_file()`:
   ```python
   analysis['my_check'] = 'pattern' in content
   ```

4. Add status determination in `determine_status()`:
   ```python
   elif dimension_key == "Dimension Name":
       if not applicable:
           return "N/A"
       if analysis.get('my_check'):
           return "Complete"
       return "Incomplete"
   ```

5. Regenerate matrix

### Customizing Analysis

**Adjust N/A Rules:**
Edit `determine_status()` to change when patterns are not applicable.

**Add File-Level Checks:**
Modify `analyze_class_file()` to parse additional patterns.

**Change Status Colors:**
Edit Excel cell fill colors in `create_excel_matrix()`.

---

## Output Format

### Excel Structure

**Sheet 1: Code Quality Matrix**
- Columns A-E: Class metadata
- Columns F+: Quality dimension statuses
- 558 rows (2 headers + 556 classes)
- Frozen panes at F3

**Sheet 2: Summary**
- Total classes count
- Quality dimensions count
- Status legend

### Status Indicators

- **✓** (Green) - Complete
- **✗** (Red) - Incomplete
- **-** (Gray) - N/A
- **?** (Yellow) - Review Needed

---

## Performance

**Processing Time:**
- ~60 seconds for 556 classes
- ~0.1 seconds per class

**Memory Usage:**
- ~50 MB during generation
- ~100 KB final Excel file

**Disk Space:**
- Excel file: ~96 KB

---

## Troubleshooting

### "ModuleNotFoundError: No module named 'openpyxl'"
```bash
pip3 install --user openpyxl
```

### "Permission denied"
```bash
chmod +x scripts/quality/regenerate_matrix.sh
```

### "File not found" errors
Ensure you're running from project root:
```bash
cd /home/runner/work/derbent/derbent
./scripts/quality/regenerate_matrix.sh
```

### Matrix shows unexpected results
1. Check class file exists at expected path
2. Verify pattern syntax matches detection logic
3. Review `determine_status()` rules
4. Add debug prints to `analyze_class_file()`

---

## Integration Ideas

### Pre-Commit Hook
```bash
# .git/hooks/pre-commit
if git diff --cached --name-only | grep -q "src/main/java/tech/derbent/.*C.*\.java"; then
    echo "Java classes changed, consider regenerating quality matrix"
fi
```

### GitHub Actions Workflow
```yaml
name: Quality Matrix
on: [push, pull_request]
jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Generate Matrix
        run: ./scripts/quality/regenerate_matrix.sh
      - name: Upload Matrix
        uses: actions/upload-artifact@v2
        with:
          name: quality-matrix
          path: docs/CODE_QUALITY_MATRIX.xlsx
```

### Monthly Cron Job
```bash
# Add to crontab
0 0 1 * * cd /path/to/derbent && ./scripts/quality/regenerate_matrix.sh && git commit -am "chore: update quality matrix" && git push
```

---

## Related Documentation

- **Quick Start**: `docs/CODE_QUALITY_MATRIX_README.md`
- **Complete Guide**: `docs/CODE_QUALITY_MATRIX_GUIDE.md` (300+ pages)
- **Coding Standards**: `docs/architecture/coding-standards.md`
- **Entity Checklist**: `docs/architecture/NEW_ENTITY_COMPLETE_CHECKLIST.md`

---

## Version History

- **v1.0** (2026-01-17): Initial release
  - 51 quality dimensions
  - 556 classes analyzed
  - Automated Excel generation

---

**Maintained by**: Derbent Development Team  
**Last Updated**: 2026-01-17
