#!/bin/bash

echo "=== PLACEHOLDER PATTERN AUDIT ==="
echo ""
echo "Checking all @Transient placeholder fields with createComponentMethod..."
echo ""

# Find all placeholder fields
entities=$(grep -r "placeHolder_" src/main/java --include="*.java" -l | grep "/domain/" | sort -u)

total=0
compliant=0
issues=0

for entity_file in $entities; do
    entity_name=$(basename "$entity_file" .java)
    
    # Extract placeholder field names
    placeholders=$(grep "placeHolder_" "$entity_file" | grep "private" | grep -oP "placeHolder_\w+" | sort -u)
    
    if [ -z "$placeholders" ]; then
        continue
    fi
    
    echo "Entity: $entity_name"
    echo "File: $entity_file"
    
    for placeholder in $placeholders; do
        total=$((total + 1))
        
        # Check @AMetaData
        metadata=$(grep -B 10 "$placeholder" "$entity_file" | grep "@AMetaData")
        
        # Check createComponentMethod
        method_name=$(echo "$metadata" | grep -oP 'createComponentMethod = "\K[^"]+' || echo "")
        
        # Check getter
        getter="get${placeholder:0:1}$(echo ${placeholder:1} | sed 's/^./\u&/')"
        has_getter=$(grep -c "public.*$getter()" "$entity_file")
        
        # Check @Transient
        is_transient=$(grep -B 1 "$placeholder" "$entity_file" | grep -c "@Transient")
        
        # Check if field is final
        is_final=$(grep "$placeholder" "$entity_file" | grep "private" | grep -c "final")
        
        # Check initializer
        initializer_file=$(echo "$entity_file" | sed 's|/domain/|/service/|' | sed 's|\.java|InitializerService.java|')
        has_initializer=0
        if [ -f "$initializer_file" ]; then
            has_initializer=$(grep -c "$placeholder" "$initializer_file")
        fi
        
        echo "  Placeholder: $placeholder"
        echo "    @Transient: $([ $is_transient -eq 1 ] && echo "✅" || echo "❌")"
        echo "    createComponentMethod: ${method_name:-❌ MISSING}"
        echo "    Getter: $([ $has_getter -gt 0 ] && echo "✅" || echo "❌")"
        echo "    Is final: $([ $is_final -eq 1 ] && echo "✅ YES" || echo "⚠️ NO")"
        echo "    Initializer: $([ $has_initializer -gt 0 ] && echo "✅" || echo "❌")"
        
        # Check compliance
        if [ $is_transient -eq 1 ] && [ -n "$method_name" ] && [ $has_getter -gt 0 ] && [ $has_initializer -gt 0 ]; then
            echo "    Status: ✅ COMPLIANT"
            compliant=$((compliant + 1))
        else
            echo "    Status: ❌ ISSUES FOUND"
            issues=$((issues + 1))
        fi
        echo ""
    done
    echo ""
done

echo "=== SUMMARY ==="
echo "Total placeholders: $total"
echo "Compliant: $compliant"
echo "Issues: $issues"
echo "Compliance rate: $((compliant * 100 / total))%"
