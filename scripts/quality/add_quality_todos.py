#!/usr/bin/env python3
"""
Add TODO Comments to Source Files for Quality Gate Violations
This script analyzes classes for quality gate violations and adds
TODO comments directly in the source code with suggested fixes.
"""

import os
import re
import sys
from pathlib import Path

# Base directory for the project
BASE_DIR = Path(__file__).parent.parent.parent.resolve()
SRC_DIR = BASE_DIR / "src/main/java/tech/derbent"

# Quality gates with fix suggestions
QUALITY_FIXES = {
    "C-Prefix Naming": "Rename class to start with 'C' prefix (e.g., CMyClass)",
    "Entity Annotations": "Add @Entity, @Table, and @AttributeOverride annotations",
    "Entity Constants": "Add missing constant: {constant}",
    "@AMetaData Annotations": "Add @AMetaData annotation to field: {field}",
    "Validation Annotations": "Add validation annotation (@NotNull, @NotBlank, @Size) to field: {field}",
    "Service Annotations": "Add @Service and @PreAuthorize annotations to class",
    "Logger Field": "Add static final Logger LOGGER = LoggerFactory.getLogger({class}.class)",
    "findById Override": "Override findById with JOIN FETCH for lazy relationships",
    "ORDER BY Clause": "Add ORDER BY clause to query: {query}",
    "Extends Base Class": "Extend appropriate base class (CEntityDB, CProjectItem, etc.)",
    "createBasicView()": "Implement createBasicView() method in initializer",
    "createGridEntity()": "Implement createGridEntity() method in initializer",
    "initializeSample()": "Implement initializeSample() method in initializer",
    "initializeDefaults()": "Implement initializeDefaults() method in entity",
    "getEntityClass()": "Implement getEntityClass() method in service",
    "Unit Tests": "Create corresponding *Test class with unit tests",
    "JavaDoc": "Add JavaDoc comments to class and public methods",
    "Calls super.copyEntityTo()": "Call super.copyEntityTo(target, options) at start of method",
}

def get_class_info(class_path):
    """Extract information about a class."""
    parts = class_path.split('.')
    module = parts[2] if len(parts) > 2 else ""
    layer = parts[-2] if len(parts) > 1 else ""
    class_name = parts[-1]
    
    rel_path = class_path.replace('.', '/') + ".java"
    file_path = BASE_DIR / "src/main/java" / rel_path
    
    return {
        'full_path': class_path,
        'module': module,
        'layer': layer,
        'class_name': class_name,
        'file_path': file_path if file_path.exists() else None,
    }

def analyze_violations(file_path, class_info):
    """Analyze a file and return list of violations with details."""
    if not file_path or not file_path.exists():
        return []
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return []
    
    violations = []
    class_name = class_info['class_name']
    layer = class_info['layer']
    
    is_entity = layer == 'domain'
    is_service = layer == 'service' and 'Service' in class_name
    is_repository = 'Repository' in class_name
    is_initializer = 'Initializer' in class_name
    is_exception = 'Exception' in class_name
    is_config = 'config' in class_info['module']
    
    # Check C-Prefix Naming
    if not re.search(r'(class|interface)\s+[CI]\w+', content):
        violations.append({
            'gate': 'C-Prefix Naming',
            'location': 'class declaration',
            'details': '',
            'line_hint': f'class {class_name}'
        })
    
    # Check Entity Constants (for entities only)
    if is_entity:
        required_constants = ['DEFAULT_COLOR', 'DEFAULT_ICON', 'ENTITY_TITLE_SINGULAR', 
                            'ENTITY_TITLE_PLURAL', 'VIEW_NAME']
        for const in required_constants:
            if const not in content:
                violations.append({
                    'gate': 'Entity Constants',
                    'location': 'class constants',
                    'details': const,
                    'line_hint': 'public static final'
                })
    
    # Check field annotations (for entities)
    if is_entity:
        # Find fields missing @AMetaData
        field_pattern = r'@\w+.*?\n\s*(?:private|protected|public)\s+(?!static)(?!final)(\w+(?:<[^>]+>)?)\s+(\w+)\s*[;=]'
        fields = re.finditer(field_pattern, content, re.MULTILINE)
        
        for match in fields:
            field_type = match.group(1)
            field_name = match.group(2)
            
            if field_name == 'LOGGER' or 'static' in content[max(0, match.start()-100):match.start()]:
                continue
            
            start = max(0, match.start() - 500)
            field_section = content[start:match.end()]
            
            if '@AMetaData' not in field_section:
                violations.append({
                    'gate': '@AMetaData Annotations',
                    'location': f'field: {field_name}',
                    'details': field_name,
                    'line_hint': f'{field_type} {field_name}'
                })
            
            # Check validation annotations
            if ('String' in field_type or 'Integer' in field_type or 'Long' in field_type):
                if not any(ann in field_section for ann in ['@NotNull', '@NotBlank', '@Size']):
                    violations.append({
                        'gate': 'Validation Annotations',
                        'location': f'field: {field_name}',
                        'details': field_name,
                        'line_hint': f'{field_type} {field_name}'
                    })
    
    # Check Service Annotations
    if is_service:
        if '@Service' not in content:
            violations.append({
                'gate': 'Service Annotations',
                'location': 'class declaration',
                'details': '@Service',
                'line_hint': f'class {class_name}'
            })
        if '@PreAuthorize' not in content and '@PermitAll' not in content:
            violations.append({
                'gate': 'Service Annotations',
                'location': 'class declaration',
                'details': '@PreAuthorize or @PermitAll',
                'line_hint': f'class {class_name}'
            })
    
    # Check Logger
    if not is_config and not is_exception:
        if not re.search(r'Logger\s+LOGGER', content):
            violations.append({
                'gate': 'Logger Field',
                'location': 'class fields',
                'details': class_name,
                'line_hint': 'private static final Logger'
            })
    
    # Check Repository patterns
    if is_repository:
        if 'JOIN FETCH' not in content:
            violations.append({
                'gate': 'findById Override',
                'location': 'repository methods',
                'details': '',
                'line_hint': '@Override'
            })
        
        # Check queries for ORDER BY
        query_pattern = r'@Query\s*\(\s*"""(.*?)"""'
        queries = re.findall(query_pattern, content, re.DOTALL)
        for idx, query in enumerate(queries, 1):
            if 'SELECT' in query.upper() and 'ORDER BY' not in query.upper():
                violations.append({
                    'gate': 'ORDER BY Clause',
                    'location': f'query #{idx}',
                    'details': query[:50] + '...',
                    'line_hint': '@Query'
                })
    
    # Check Initializer methods
    if is_initializer:
        if 'createBasicView' not in content:
            violations.append({
                'gate': 'createBasicView()',
                'location': 'initializer methods',
                'details': '',
                'line_hint': 'private static'
            })
        if 'createGridEntity' not in content:
            violations.append({
                'gate': 'createGridEntity()',
                'location': 'initializer methods',
                'details': '',
                'line_hint': 'private static'
            })
        if 'initializeSample' not in content:
            violations.append({
                'gate': 'initializeSample()',
                'location': 'initializer methods',
                'details': '',
                'line_hint': 'public'
            })
    
    # Check Entity methods
    if is_entity:
        if 'initializeDefaults()' not in content:
            violations.append({
                'gate': 'initializeDefaults()',
                'location': 'entity methods',
                'details': '',
                'line_hint': '@Override'
            })
    
    # Check Service methods
    if is_service:
        if 'getEntityClass()' not in content:
            violations.append({
                'gate': 'getEntityClass()',
                'location': 'service methods',
                'details': '',
                'line_hint': '@Override'
            })
    
    # Check tests
    if not is_config and not is_exception:
        test_file = file_path.parent / f"{file_path.stem}Test.java"
        if not test_file.exists():
            violations.append({
                'gate': 'Unit Tests',
                'location': 'test file',
                'details': f'{class_name}Test.java',
                'line_hint': 'end of file'
            })
    
    # Check JavaDoc
    if '/**' not in content:
        violations.append({
            'gate': 'JavaDoc',
            'location': 'class documentation',
            'details': '',
            'line_hint': 'class ' + class_name
        })
    
    # Check copy pattern
    if is_entity and 'copyEntityTo' in content:
        if 'super.copyEntityTo(target, options)' not in content and 'super.copyEntityTo(target,options)' not in content:
            violations.append({
                'gate': 'Calls super.copyEntityTo()',
                'location': 'copyEntityTo method',
                'details': '',
                'line_hint': 'protected void copyEntityTo'
            })
    
    return violations

def add_todo_comments(file_path, violations, dry_run=True):
    """Add TODO comments to file for violations (max 10 per gate)."""
    if not file_path or not file_path.exists() or not violations:
        return 0
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return 0
    
    # Group violations by gate and limit to 10 per gate
    gate_violations = {}
    for v in violations:
        gate = v['gate']
        if gate not in gate_violations:
            gate_violations[gate] = []
        if len(gate_violations[gate]) < 10:
            gate_violations[gate].append(v)
    
    # Find appropriate insertion points and create TODO comments
    todos_added = 0
    modified_lines = lines.copy()
    
    for gate, gate_viols in gate_violations.items():
        for violation in gate_viols:
            # Get fix suggestion
            fix_template = QUALITY_FIXES.get(gate, "Review and fix this quality gate violation")
            # Replace placeholders in fix template
            fix = fix_template.replace('{constant}', violation['details'])
            fix = fix.replace('{field}', violation['details'])
            fix = fix.replace('{class}', file_path.stem)
            fix = fix.replace('{query}', violation['details'][:50] if violation['details'] else '')
            
            # Create TODO comment
            todo = f"// TODO: [{gate}] - {violation['location']}"
            if violation['details']:
                todo += f" ({violation['details']})"
            todo += f" - {fix}\n"
            
            # Find insertion point (near the relevant code)
            line_hint = violation['line_hint']
            inserted = False
            
            for i, line in enumerate(modified_lines):
                if line_hint in line and f"TODO: [{gate}]" not in modified_lines[max(0, i-5):i+1]:
                    # Insert TODO comment before this line
                    indent = len(line) - len(line.lstrip())
                    indented_todo = " " * indent + todo
                    modified_lines.insert(i, indented_todo)
                    todos_added += 1
                    inserted = True
                    break
            
            # If no specific location found, add at class level
            if not inserted:
                for i, line in enumerate(modified_lines):
                    if 'class ' in line and '{' in line and f"TODO: [{gate}]" not in modified_lines[max(0, i-5):i+1]:
                        indent = len(line) - len(line.lstrip())
                        indented_todo = " " * indent + todo
                        modified_lines.insert(i, indented_todo)
                        todos_added += 1
                        break
    
    # Write back to file
    if todos_added > 0 and not dry_run:
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.writelines(modified_lines)
            print(f"  ✓ Added {todos_added} TODO comments to {file_path.name}")
        except Exception as e:
            print(f"  ✗ Error writing {file_path}: {e}")
            return 0
    
    return todos_added

def process_classes(classes_file, dry_run=True, max_files=None):
    """Process all classes and add TODO comments for violations."""
    
    with open(classes_file, 'r') as f:
        class_paths = [line.strip() for line in f if line.strip()]
    
    if max_files:
        class_paths = class_paths[:max_files]
    
    print(f"\n{'DRY RUN: ' if dry_run else ''}Processing {len(class_paths)} classes...")
    print(f"Adding TODO comments for quality gate violations...\n")
    
    total_violations = 0
    total_todos = 0
    files_with_todos = 0
    
    for idx, class_path in enumerate(class_paths, 1):
        class_info = get_class_info(class_path)
        
        if not class_info['file_path']:
            continue
        
        violations = analyze_violations(class_info['file_path'], class_info)
        
        if violations:
            print(f"\n[{idx}/{len(class_paths)}] {class_info['class_name']}")
            print(f"  Found {len(violations)} violations:")
            
            # Show violations summary
            gate_counts = {}
            for v in violations:
                gate_counts[v['gate']] = gate_counts.get(v['gate'], 0) + 1
            
            for gate, count in sorted(gate_counts.items()):
                print(f"    • {gate}: {count}")
            
            total_violations += len(violations)
            
            # Add TODO comments (limited to 10 per gate)
            todos_added = add_todo_comments(class_info['file_path'], violations, dry_run)
            if todos_added > 0:
                total_todos += todos_added
                files_with_todos += 1
        
        # Progress indicator
        if idx % 50 == 0:
            print(f"\n... Processed {idx}/{len(class_paths)} classes ...")
    
    print(f"\n{'='*60}")
    print(f"Summary:")
    print(f"  Total violations found: {total_violations}")
    print(f"  TODO comments {'would be' if dry_run else ''} added: {total_todos}")
    print(f"  Files {'would be' if dry_run else ''} modified: {files_with_todos}")
    
    if dry_run:
        print(f"\n  Run with --apply to actually modify source files")
    print(f"{'='*60}\n")

if __name__ == "__main__":
    classes_file = "/tmp/quality_matrix/all_classes.txt"
    
    # Parse arguments
    dry_run = True
    max_files = None
    
    if len(sys.argv) > 1:
        if "--apply" in sys.argv:
            dry_run = False
        if "--sample" in sys.argv:
            max_files = 10
    
    if not Path(classes_file).exists():
        print(f"Error: Classes file not found: {classes_file}")
        print(f"Run: ./scripts/quality/regenerate_matrix.sh first")
        sys.exit(1)
    
    process_classes(classes_file, dry_run, max_files)
