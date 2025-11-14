#!/bin/bash
# Generate Dokka documentation with full inheritance and call graphs

set -e

echo "🔧 Setting up Java 21 environment..."
source ./setup-java-env.sh

echo "📚 Generating Dokka documentation..."
echo "   - Full inheritance tracking enabled"
echo "   - All visibility levels included"
echo "   - Call graphs and hierarchy visualization"
echo ""

mvn dokka:dokka

echo ""
echo "✅ Documentation generated successfully!"
echo ""
echo "📖 View documentation:"
echo "   Browser: file://$(pwd)/target/dokka/index.html"
echo "   Or run:  xdg-open target/dokka/index.html"
echo ""
echo "📊 Documentation stats:"
find target/dokka -name "*.html" | wc -l | xargs echo "   HTML pages:"
du -sh target/dokka | awk '{print "   Total size: " $1}'
echo ""
