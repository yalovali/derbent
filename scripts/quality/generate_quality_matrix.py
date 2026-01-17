#!/usr/bin/env python3
"""
Generate Code Quality Matrix for Derbent Project
This script creates a comprehensive Excel spreadsheet that tracks code quality
patterns, rules, and guidelines compliance across all classes in the codebase.
"""

import os
import re
import subprocess
from pathlib import Path
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter

# Base directory for the project
BASE_DIR = Path("/home/runner/work/derbent/derbent")
SRC_DIR = BASE_DIR / "src/main/java/tech/derbent"

# Quality dimensions to check
QUALITY_DIMENSIONS = [
    # Naming and Structure
    ("C-Prefix Naming", "Class name follows C-prefix convention"),
    ("Package Structure", "Correct package structure (domain/service/view)"),
    
    # Entity Patterns
    ("Entity Annotations", "Has @Entity, @Table, @AttributeOverride (for entities)"),
    ("Entity Constants", "Has all 5 required constants (DEFAULT_COLOR, DEFAULT_ICON, etc.)"),
    ("Extends Base Class", "Extends appropriate base class (CEntityDB, CProjectItem, etc.)"),
    ("Interface Implementation", "Implements required interfaces correctly"),
    
    # Field Annotations and Validation
    ("@AMetaData Annotations", "All fields have @AMetaData with proper attributes"),
    ("Validation Annotations", "Has @NotNull, @NotBlank, @Size where appropriate"),
    ("Column Annotations", "Proper @Column, @JoinColumn annotations"),
    ("Fetch Strategy", "Uses LAZY fetch for collections/relationships"),
    
    # Constructor and Initialization
    ("Default Constructor", "Has no-arg constructor for JPA"),
    ("Named Constructor", "Has constructor(name, project) for creation"),
    ("initializeDefaults()", "Implements initializeDefaults() method"),
    
    # Repository Patterns
    ("Repository Interface", "Has repository interface extending correct base"),
    ("findById Override", "Overrides findById with JOIN FETCH for lazy fields"),
    ("Query Patterns", "Uses triple-quoted queries with #{#entityName}"),
    ("ORDER BY Clause", "All list queries have ORDER BY"),
    
    # Service Patterns
    ("Service Annotations", "Has @Service, @PreAuthorize, @PermitAll"),
    ("Service Base Class", "Extends appropriate service base class"),
    ("Stateless Service", "No instance state (multi-user safe)"),
    ("getEntityClass()", "Implements getEntityClass() method"),
    ("getInitializerService()", "Implements getInitializerService() method"),
    
    # Initializer Patterns
    ("Initializer Structure", "Has InitializerService extending CInitializerServiceBase"),
    ("createBasicView()", "Implements createBasicView() method"),
    ("createGridEntity()", "Implements createGridEntity() method"),
    ("initialize()", "Implements initialize() method"),
    ("initializeSample()", "Implements initializeSample() method"),
    ("CDataInitializer Registration", "Registered in CDataInitializer"),
    
    # Page Service Patterns
    ("Page Service", "Has CPageService if needed (workflow/sprint)"),
    ("Page Service Interfaces", "Implements correct page service interfaces"),
    
    # Exception Handling
    ("Exception Pattern", "Uses Check.notNull, proper exception handling"),
    ("User Exception Handling", "UI handlers use CNotificationService.showException"),
    
    # Logging
    ("Logger Field", "Has static final Logger with correct class"),
    ("Logging Pattern", "Follows ANSI logging format standards"),
    ("Log Levels", "Appropriate log levels (DEBUG, INFO, WARN, ERROR)"),
    
    # Interface Implementations
    ("IHasAttachments", "Proper getAttachments/setAttachments if applicable"),
    ("IHasComments", "Proper getComments/setComments if applicable"),
    ("IHasStatusAndWorkflow", "Proper workflow methods if applicable"),
    
    # Code Quality
    ("Getter/Setter Pattern", "Setters call updateLastModified()"),
    ("No Raw Types", "All generic types properly parameterized"),
    ("Constants Naming", "Constants are static final SCREAMING_SNAKE_CASE"),
    
    # Testing
    ("Unit Tests", "Has corresponding *Test class"),
    ("Integration Tests", "Has service/repository tests"),
    ("UI Tests", "Has Playwright tests if has view"),
    
    # Documentation
    ("JavaDoc", "Has class-level JavaDoc"),
    ("Method Documentation", "Complex methods documented"),
    ("Implementation Doc", "Has implementation markdown if complex"),
    
    # Security
    ("Access Control", "Proper @PreAuthorize/@RolesAllowed annotations"),
    ("Tenant Context", "Uses session service for company/project context"),
    
    # Formatting
    ("Code Formatting", "Follows eclipse-formatter.xml (4 spaces)"),
    ("Import Organization", "Clean imports, no wildcards"),
]

def get_class_info(class_path):
    """Extract information about a class."""
    parts = class_path.split('.')
    module = parts[2] if len(parts) > 2 else ""
    layer = parts[-2] if len(parts) > 1 else ""  # domain, service, view
    class_name = parts[-1]
    
    # Determine file path
    file_path = SRC_DIR / "/".join(parts[2:]) / f"{class_name}.java"
    if not file_path.exists():
        file_path = BASE_DIR / "src/main/java" / "/".join(parts) / f"{class_name}.java"
    
    return {
        'full_path': class_path,
        'module': module,
        'layer': layer,
        'class_name': class_name,
        'file_path': file_path if file_path.exists() else None,
    }

def analyze_class_file(file_path):
    """Analyze a Java file for quality patterns."""
    if not file_path or not file_path.exists():
        return {}
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
        return {}
    
    analysis = {}
    
    # Check naming conventions
    analysis['c_prefix'] = re.search(r'class\s+C\w+', content) is not None
    
    # Check entity annotations
    analysis['entity_annotation'] = '@Entity' in content
    analysis['table_annotation'] = '@Table' in content
    analysis['attribute_override'] = '@AttributeOverride' in content
    
    # Check constants
    analysis['default_color'] = 'DEFAULT_COLOR' in content
    analysis['default_icon'] = 'DEFAULT_ICON' in content
    analysis['entity_title_singular'] = 'ENTITY_TITLE_SINGULAR' in content
    analysis['entity_title_plural'] = 'ENTITY_TITLE_PLURAL' in content
    analysis['view_name'] = 'VIEW_NAME' in content
    
    # Check annotations
    analysis['ametadata'] = '@AMetaData' in content
    analysis['notnull'] = '@NotNull' in content or '@NotBlank' in content
    analysis['service_annotation'] = '@Service' in content
    analysis['preauthorize'] = '@PreAuthorize' in content
    
    # Check methods
    analysis['initialize_defaults'] = 'initializeDefaults()' in content
    analysis['get_entity_class'] = 'getEntityClass()' in content
    analysis['create_basic_view'] = 'createBasicView' in content
    analysis['create_grid_entity'] = 'createGridEntity' in content
    analysis['initialize_sample'] = 'initializeSample' in content
    
    # Check logging
    analysis['logger'] = re.search(r'Logger\s+LOGGER', content) is not None
    
    # Check base class extension
    analysis['extends'] = re.search(r'extends\s+\w+', content) is not None
    
    # Check interfaces
    analysis['implements'] = 'implements' in content
    
    # Check JOIN FETCH
    analysis['join_fetch'] = 'JOIN FETCH' in content
    
    # Check ORDER BY
    analysis['order_by'] = 'ORDER BY' in content
    
    # Check JavaDoc
    analysis['javadoc'] = '/**' in content
    
    # Check tests exist
    test_file = file_path.parent / f"{file_path.stem}Test.java"
    analysis['has_test'] = test_file.exists()
    
    return analysis

def determine_status(class_info, analysis, dimension_key):
    """Determine status for a quality dimension."""
    class_name = class_info['class_name']
    layer = class_info['layer']
    
    # Special handling for different class types
    is_entity = layer == 'domain'
    is_service = layer == 'service' and 'Service' in class_name
    is_repository = 'Repository' in class_name
    is_initializer = 'Initializer' in class_name
    is_page_service = 'PageService' in class_name
    is_view = layer == 'view'
    is_exception = 'Exception' in class_name
    is_config = 'config' in class_info['module']
    
    # Map dimension to checks
    if dimension_key == "C-Prefix Naming":
        if analysis.get('c_prefix'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Entity Annotations":
        if not is_entity:
            return "N/A"
        if analysis.get('entity_annotation') and analysis.get('table_annotation'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Entity Constants":
        if not is_entity:
            return "N/A"
        if (analysis.get('default_color') and analysis.get('default_icon') and
            analysis.get('entity_title_singular') and analysis.get('entity_title_plural')):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "@AMetaData Annotations":
        if not is_entity:
            return "N/A"
        if analysis.get('ametadata'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Validation Annotations":
        if not is_entity:
            return "N/A"
        if analysis.get('notnull'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Service Annotations":
        if not is_service:
            return "N/A"
        if analysis.get('service_annotation') and analysis.get('preauthorize'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Repository Interface":
        if not is_repository:
            return "N/A"
        return "Complete" if analysis.get('file_path') else "Incomplete"
    
    elif dimension_key == "findById Override":
        if not is_repository:
            return "N/A"
        if analysis.get('join_fetch'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "ORDER BY Clause":
        if not is_repository:
            return "N/A"
        if analysis.get('order_by'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Logger Field":
        if is_config or is_exception:
            return "N/A"
        if analysis.get('logger'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Initializer Structure":
        if not is_initializer:
            return "N/A"
        return "Complete" if analysis.get('file_path') else "Incomplete"
    
    elif dimension_key == "createBasicView()":
        if not is_initializer:
            return "N/A"
        if analysis.get('create_basic_view'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "createGridEntity()":
        if not is_initializer:
            return "N/A"
        if analysis.get('create_grid_entity'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "initializeSample()":
        if not is_initializer:
            return "N/A"
        if analysis.get('initialize_sample'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Unit Tests":
        if is_config or is_exception:
            return "N/A"
        if analysis.get('has_test'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "JavaDoc":
        if analysis.get('javadoc'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Extends Base Class":
        if is_exception or is_config:
            return "N/A"
        if analysis.get('extends'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "Interface Implementation":
        if not (is_entity or is_service or is_page_service):
            return "N/A"
        if analysis.get('implements'):
            return "Complete"
        return "Review Needed"
    
    elif dimension_key == "initializeDefaults()":
        if not is_entity:
            return "N/A"
        if analysis.get('initialize_defaults'):
            return "Complete"
        return "Incomplete"
    
    elif dimension_key == "getEntityClass()":
        if not is_service:
            return "N/A"
        if analysis.get('get_entity_class'):
            return "Complete"
        return "Incomplete"
    
    else:
        # For dimensions we can't automatically detect, mark as Review Needed
        return "Review Needed"

def create_excel_matrix(classes_file, output_file):
    """Create comprehensive Excel quality matrix."""
    
    # Read all classes
    with open(classes_file, 'r') as f:
        class_paths = [line.strip() for line in f if line.strip()]
    
    print(f"Found {len(class_paths)} classes to analyze")
    
    # Create workbook
    wb = Workbook()
    ws = wb.active
    ws.title = "Code Quality Matrix"
    
    # Define colors
    header_fill = PatternFill(start_color="366092", end_color="366092", fill_type="solid")
    complete_fill = PatternFill(start_color="C6EFCE", end_color="C6EFCE", fill_type="solid")
    incomplete_fill = PatternFill(start_color="FFC7CE", end_color="FFC7CE", fill_type="solid")
    na_fill = PatternFill(start_color="F0F0F0", end_color="F0F0F0", fill_type="solid")
    review_fill = PatternFill(start_color="FFEB9C", end_color="FFEB9C", fill_type="solid")
    
    header_font = Font(bold=True, color="FFFFFF")
    border = Border(
        left=Side(style='thin'),
        right=Side(style='thin'),
        top=Side(style='thin'),
        bottom=Side(style='thin')
    )
    
    # Header row 1: Main categories
    ws.merge_cells('A1:E1')
    ws['A1'] = "Class Information"
    ws['A1'].fill = header_fill
    ws['A1'].font = header_font
    ws['A1'].alignment = Alignment(horizontal='center', vertical='center')
    
    col_offset = 6  # Start quality dimensions at column F
    
    # Header row 2: Column names
    headers = ["Class Name", "Module", "Layer", "File Path", "Category"]
    for idx, header in enumerate(headers, 1):
        cell = ws.cell(row=2, column=idx)
        cell.value = header
        cell.fill = header_fill
        cell.font = header_font
        cell.alignment = Alignment(horizontal='center', vertical='center', wrap_text=True)
        cell.border = border
    
    # Add quality dimension headers
    for idx, (dim_name, dim_desc) in enumerate(QUALITY_DIMENSIONS, col_offset):
        cell = ws.cell(row=1, column=idx)
        cell.value = dim_name
        cell.fill = header_fill
        cell.font = header_font
        cell.alignment = Alignment(horizontal='center', vertical='center', wrap_text=True, text_rotation=90)
        cell.border = border
        
        # Add description in row 2
        cell2 = ws.cell(row=2, column=idx)
        cell2.value = dim_desc
        cell2.fill = header_fill
        cell2.font = Font(size=8, color="FFFFFF")
        cell2.alignment = Alignment(horizontal='center', vertical='center', wrap_text=True)
        cell2.border = border
    
    # Set column widths
    ws.column_dimensions['A'].width = 40
    ws.column_dimensions['B'].width = 20
    ws.column_dimensions['C'].width = 12
    ws.column_dimensions['D'].width = 50
    ws.column_dimensions['E'].width = 20
    
    for idx in range(col_offset, col_offset + len(QUALITY_DIMENSIONS)):
        ws.column_dimensions[get_column_letter(idx)].width = 4
    
    # Freeze panes
    ws.freeze_panes = 'F3'
    
    # Process each class
    row = 3
    for class_path in class_paths:
        if row % 50 == 0:
            print(f"Processing row {row}/{len(class_paths) + 2}...")
        
        class_info = get_class_info(class_path)
        analysis = analyze_class_file(class_info['file_path'])
        
        # Determine category
        category = "Other"
        if class_info['layer'] == 'domain':
            category = "Entity"
        elif 'Service' in class_info['class_name']:
            if 'Initializer' in class_info['class_name']:
                category = "Initializer"
            elif 'PageService' in class_info['class_name']:
                category = "Page Service"
            else:
                category = "Service"
        elif 'Repository' in class_info['class_name']:
            category = "Repository"
        elif class_info['layer'] == 'view':
            category = "View"
        elif 'Exception' in class_info['class_name']:
            category = "Exception"
        elif 'config' in class_info['module']:
            category = "Configuration"
        
        # Write class info
        ws.cell(row=row, column=1, value=class_info['class_name']).border = border
        ws.cell(row=row, column=2, value=class_info['module']).border = border
        ws.cell(row=row, column=3, value=class_info['layer']).border = border
        ws.cell(row=row, column=4, value=str(class_info['file_path']) if class_info['file_path'] else 'N/A').border = border
        ws.cell(row=row, column=5, value=category).border = border
        
        # Write quality dimension statuses
        for idx, (dim_name, _) in enumerate(QUALITY_DIMENSIONS, col_offset):
            status = determine_status(class_info, analysis, dim_name)
            cell = ws.cell(row=row, column=idx)
            cell.value = status[0] if len(status) == 1 else status[:2]  # Abbreviate
            cell.alignment = Alignment(horizontal='center', vertical='center')
            cell.border = border
            
            if status == "Complete":
                cell.fill = complete_fill
                cell.value = "✓"
            elif status == "Incomplete":
                cell.fill = incomplete_fill
                cell.value = "✗"
            elif status == "N/A":
                cell.fill = na_fill
                cell.value = "-"
            else:  # Review Needed
                cell.fill = review_fill
                cell.value = "?"
        
        row += 1
    
    # Add summary sheet
    summary = wb.create_sheet("Summary")
    summary.cell(1, 1, "Code Quality Matrix Summary").font = Font(size=16, bold=True)
    summary.cell(3, 1, "Total Classes:").font = Font(bold=True)
    summary.cell(3, 2, len(class_paths))
    summary.cell(4, 1, "Quality Dimensions:").font = Font(bold=True)
    summary.cell(4, 2, len(QUALITY_DIMENSIONS))
    
    summary.cell(6, 1, "Legend:").font = Font(bold=True)
    summary.cell(7, 1, "✓").fill = complete_fill
    summary.cell(7, 2, "Complete - Pattern fully implemented")
    summary.cell(8, 1, "✗").fill = incomplete_fill
    summary.cell(8, 2, "Incomplete - Pattern missing or partially implemented")
    summary.cell(9, 1, "-").fill = na_fill
    summary.cell(9, 2, "N/A - Not applicable to this class")
    summary.cell(10, 1, "?").fill = review_fill
    summary.cell(10, 2, "Review Needed - Manual review required")
    
    summary.column_dimensions['A'].width = 15
    summary.column_dimensions['B'].width = 50
    
    # Save workbook
    wb.save(output_file)
    print(f"\nExcel matrix saved to: {output_file}")
    print(f"Total rows: {row - 1}")
    print(f"Total quality dimensions: {len(QUALITY_DIMENSIONS)}")

if __name__ == "__main__":
    classes_file = "/tmp/quality_matrix/all_classes.txt"
    output_file = "/home/runner/work/derbent/derbent/docs/CODE_QUALITY_MATRIX.xlsx"
    
    create_excel_matrix(classes_file, output_file)
