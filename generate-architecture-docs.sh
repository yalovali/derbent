#!/bin/bash
# Generate architecture documentation using multiple tools
# Combines JavaDoc + jdeps + custom GraphViz generation

set -e

echo "📚 Derbent Architecture Documentation Generator"
echo "================================================"
echo ""

# Setup Java environment
echo "🔧 Setting up Java 21 environment..."
source ./setup-java-env.sh

# Compile project first
echo "🔨 Compiling project..."
mvn clean compile -q

# Generate JavaDoc
echo "📖 Generating JavaDoc..."
mvn javadoc:javadoc -q

# Generate dependency graphs with jdeps
echo "🔍 Analyzing dependencies with jdeps..."
mkdir -p target/architecture-docs

# Package dependency graph
echo "   - Package dependencies..."
jdeps -verbose:package -dotoutput target/architecture-docs target/classes 2>/dev/null || true

# Class-level dependencies for main packages
echo "   - Class dependencies..."
jdeps -verbose:class -filter:package -filter:archive \
    -e "tech.derbent.*" \
    target/classes 2>/dev/null > target/architecture-docs/class-dependencies.txt || true

# Generate inheritance graphs with custom script
echo "📊 Generating inheritance graphs..."

# Create Python script for inheritance visualization
cat > /tmp/generate_inheritance.py << 'PYTHON'
import os
import re
from pathlib import Path

def find_java_files(src_dir):
    """Find all Java files"""
    return list(Path(src_dir).rglob("*.java"))

def extract_class_info(file_path):
    """Extract class name, package, extends, implements"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Extract package
    package_match = re.search(r'package\s+([\w.]+);', content)
    package = package_match.group(1) if package_match else ""
    
    # Extract class/interface declaration
    class_match = re.search(
        r'(public\s+)?(abstract\s+)?(class|interface|enum)\s+(\w+)(\s+extends\s+([\w<>,\s]+))?(\s+implements\s+([\w<>,\s]+))?',
        content
    )
    
    if not class_match:
        return None
    
    class_type = class_match.group(3)
    class_name = class_match.group(4)
    extends = class_match.group(6) if class_match.group(6) else None
    implements = class_match.group(8) if class_match.group(8) else None
    
    return {
        'package': package,
        'name': class_name,
        'fullname': f"{package}.{class_name}",
        'type': class_type,
        'extends': extends,
        'implements': implements,
        'file': str(file_path)
    }

def generate_dot_graph(classes, output_file, package_filter=None):
    """Generate GraphViz DOT file"""
    with open(output_file, 'w') as f:
        f.write('digraph G {\n')
        f.write('  rankdir=BT;\n')
        f.write('  node [shape=box, style=filled, fillcolor=lightblue];\n')
        f.write('  edge [arrowhead=empty];\n\n')
        
        for cls in classes:
            if package_filter and not cls['package'].startswith(package_filter):
                continue
            
            label = f"{cls['name']}"
            color = 'lightblue'
            if cls['type'] == 'interface':
                color = 'lightyellow'
            elif cls['type'] == 'abstract':
                color = 'lightgreen'
            elif cls['type'] == 'enum':
                color = 'lightcoral'
            
            f.write(f'  "{cls["fullname"]}" [label="{label}", fillcolor={color}];\n')
            
            # Add inheritance edges
            if cls['extends']:
                parent = cls['extends'].split('<')[0].strip()
                if '.' not in parent:
                    parent = f"{cls['package']}.{parent}"
                f.write(f'  "{cls["fullname"]}" -> "{parent}" [color=blue, label="extends"];\n')
            
            # Add implementation edges
            if cls['implements']:
                interfaces = [i.strip().split('<')[0] for i in cls['implements'].split(',')]
                for interface in interfaces:
                    if '.' not in interface:
                        interface = f"{cls['package']}.{interface}"
                    f.write(f'  "{cls["fullname"]}" -> "{interface}" [color=green, style=dashed, label="implements"];\n')
        
        f.write('}\n')

# Main execution
src_dir = "src/main/java"
output_dir = "target/architecture-docs"
os.makedirs(output_dir, exist_ok=True)

java_files = find_java_files(src_dir)
classes = []

for file_path in java_files:
    info = extract_class_info(file_path)
    if info:
        classes.append(info)

# Generate full inheritance graph
generate_dot_graph(classes, f"{output_dir}/full-inheritance.dot")

# Generate per-package graphs
packages = set(".".join(cls["package"].split(".")[0:3]) for cls in classes if cls["package"].startswith("tech.derbent"))
for pkg_parts in packages:
    pkg = '.'.join(pkg_parts)
    if pkg.startswith('tech.derbent'):
        generate_dot_graph(
            classes, 
            f"{output_dir}/inheritance-{pkg.replace('.', '-')}.dot",
            pkg
        )

print(f"✓ Analyzed {len(classes)} classes")
print(f"✓ Generated DOT files in {output_dir}")
PYTHON

python3 /tmp/generate_inheritance.py

# Convert DOT files to PNG
echo "🎨 Rendering graphs..."
for dotfile in target/architecture-docs/*.dot; do
    if [ -f "$dotfile" ]; then
        pngfile="${dotfile%.dot}.png"
        svgfile="${dotfile%.dot}.svg"
        # PNG with scaling for large graphs
        dot -Tpng "$dotfile" -o "$pngfile" 2>&1 | grep -v "too large" || true
        echo "   ✓ Generated: $(basename $pngfile)"
        # SVG is vector, handles any size
        dot -Tsvg "$dotfile" -o "$svgfile" 2>/dev/null || true
    fi
done

# Create index.html for architecture docs
cat > target/architecture-docs/index.html << 'HTML'
<!DOCTYPE html>
<html>
<head>
    <title>Derbent Architecture Documentation</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        h1 { color: #1e3a8a; }
        h2 { color: #3b82f6; margin-top: 30px; }
        .section { background: white; padding: 20px; margin: 20px 0; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        img { max-width: 100%; border: 1px solid #ddd; margin: 10px 0; }
        a { color: #3b82f6; text-decoration: none; }
        a:hover { text-decoration: underline; }
        .graph-container { margin: 20px 0; }
        pre { background: #f3f4f6; padding: 15px; border-radius: 4px; overflow-x: auto; }
    </style>
</head>
<body>
    <h1>🏗️ Derbent Architecture Documentation</h1>
    
    <div class="section">
        <h2>📚 Documentation Links</h2>
        <ul>
            <li><a href="../javadoc/index.html" target="_blank">JavaDoc API Documentation</a></li>
            <li><a href="class-dependencies.txt" target="_blank">Class Dependencies Analysis</a></li>
        </ul>
    </div>
    
    <div class="section">
        <h2>🔍 Inheritance Diagrams</h2>
        <p>Visual representation of class hierarchies and relationships.</p>
HTML

# Add all PNG graphs to index
for pngfile in target/architecture-docs/*.png; do
    if [ -f "$pngfile" ]; then
        basename=$(basename "$pngfile")
        title=$(echo "$basename" | sed 's/-/ /g' | sed 's/.png//g' | sed 's/\b\(.\)/\u\1/g')
        cat >> target/architecture-docs/index.html << HTML
        <div class="graph-container">
            <h3>$title</h3>
            <a href="$basename" target="_blank"><img src="$basename" alt="$title"></a>
        </div>
HTML
    fi
done

cat >> target/architecture-docs/index.html << 'HTML'
    </div>
    
    <div class="section">
        <h2>📊 Package Structure</h2>
        <p>See jdeps generated dependency graphs in the directory.</p>
    </div>
    
    <footer style="margin-top: 40px; padding-top: 20px; border-top: 2px solid #e5e7eb; text-align: center; color: #6b7280;">
        <p>Generated on $(date)</p>
        <p>© 2025 Derbent Project Management System - Lova.tech</p>
    </footer>
</body>
</html>
HTML

echo ""
echo "✅ Architecture documentation generated successfully!"
echo ""
echo "📊 Generated Files:"
find target/architecture-docs -type f -name "*.png" | wc -l | xargs echo "   Inheritance diagrams:"
find target/architecture-docs -type f -name "*.svg" | wc -l | xargs echo "   SVG diagrams:"
find target/javadoc -name "*.html" 2>/dev/null | wc -l | xargs echo "   JavaDoc pages:"
du -sh target/architecture-docs 2>/dev/null | awk '{print "   Architecture docs size: " $1}'
du -sh target/javadoc 2>/dev/null | awk '{print "   JavaDoc size: " $1}'
echo ""
echo "📖 View Documentation:"
echo "   Architecture: file://$(pwd)/target/architecture-docs/index.html"
echo "   JavaDoc:      file://$(pwd)/target/javadoc/index.html"
echo ""
echo "🚀 Quick open:"
echo "   xdg-open target/architecture-docs/index.html"
echo ""
