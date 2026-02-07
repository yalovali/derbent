# Polymorphic Node List Implementation Guide

**SSC WAS HERE!!** 🎯✨  
**Date**: 2026-02-07  
**Request**: Add polymorphic `nodes` list to `CProject_Bab` containing all node types

## Overview

You want `CProject_Bab` to have a **single list** called `nodes` that can hold **all types** of nodes polymorphically:
- `CBabHttpServerNode`
- `CVehicleNode`
- `CBabFileInputNode`

✅ **YES, this is ABSOLUTELY possible** using JPA Inheritance!

## Solution Architecture

### JPA Inheritance Strategy Options

| Strategy | Table Structure | Pros | Cons | Recommendation |
|----------|----------------|------|------|----------------|
| **SINGLE_TABLE** | One table for all nodes | ✅ Fast queries<br>✅ Simple schema | ❌ Nullable columns<br>❌ Large table | ⚠️ For small hierarchies |
| **JOINED** | One table per class | ✅ Normalized<br>✅ No null columns | ❌ JOIN overhead<br>❌ Slower queries | ✅ **RECOMMENDED** |
| **TABLE_PER_CLASS** | Separate tables | ✅ No shared table | ❌ Complex queries<br>❌ Poor polymorphism | ❌ Not recommended |

### Recommended: JOINED Strategy

**Why JOINED is best for your case**:
1. ✅ Each node type has **different fields** (HTTP server ≠ Vehicle ≠ File Input)
2. ✅ Normalized database design (no nullable columns for type-specific fields)
3. ✅ Clean separation of concerns
4. ✅ Easy to add new node types later
5. ✅ Polymorphic queries work perfectly

## Implementation Steps

### Step 1: Update CBabNodeEntity (Base Class)

**File**: `src/main/java/tech/derbent/bab/policybase/node/domain/CBabNodeEntity.java`

**Change from**: `@MappedSuperclass`  
**Change to**: `@Entity` + `@Inheritance(strategy = InheritanceType.JOINED)`

```java
package tech.derbent.bab.policybase.node.domain;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
// ... other imports

/**
 * CBabNodeEntity - Abstract base class for virtual network nodes.
 * 
 * JPA Inheritance Strategy: JOINED
 * - Base table: cbab_node (common fields)
 * - Child tables: cnode_http_server, cnode_vehicle, cnode_file_input (specific fields)
 * - Discriminator column: node_type (identifies concrete type)
 */
@Entity  // ✅ Changed from @MappedSuperclass
@Table(name = "cbab_node")  // ✅ Base table for all nodes
@Inheritance(strategy = InheritanceType.JOINED)  // ✅ JOINED strategy
@DiscriminatorColumn(name = "node_type", discriminatorType = DiscriminatorType.STRING)  // ✅ Type identifier
@Profile("bab")
public abstract class CBabNodeEntity<EntityClass> extends CEntityOfProject<EntityClass> {
    
    // All existing fields stay EXACTLY the same
    // No changes needed to fields, getters, setters
    
    // ... rest of class unchanged
}
```

**Key Changes**:
- `@MappedSuperclass` → `@Entity` (makes it a real JPA entity)
- Add `@Table(name = "cbab_node")` (base table for common fields)
- Add `@Inheritance(strategy = InheritanceType.JOINED)` (JOINED strategy)
- Add `@DiscriminatorColumn(name = "node_type")` (type identifier)

### Step 2: Update Concrete Node Classes

**Add `@DiscriminatorValue` to each concrete class**:

**CBabHttpServerNode.java**:
```java
@Entity
@Table(name = "cnode_http_server", uniqueConstraints = { /* existing */ })
@DiscriminatorValue("HTTP_SERVER")  // ✅ Add this
@Profile("bab")
public class CBabHttpServerNode extends CBabNodeEntity<CBabHttpServerNode> {
    // ... rest unchanged
}
```

**CVehicleNode.java**:
```java
@Entity
@Table(name = "cnode_vehicle", uniqueConstraints = { /* existing */ })
@DiscriminatorValue("VEHICLE")  // ✅ Add this
@Profile("bab")
public class CVehicleNode extends CBabNodeEntity<CVehicleNode> {
    // ... rest unchanged
}
```

**CBabFileInputNode.java**:
```java
@Entity
@Table(name = "cnode_file_input", uniqueConstraints = { /* existing */ })
@DiscriminatorValue("FILE_INPUT")  // ✅ Add this
@Profile("bab")
public class CBabFileInputNode extends CBabNodeEntity<CBabFileInputNode> {
    // ... rest unchanged
}
```

### Step 3: Add Polymorphic List to CProject_Bab

**File**: `src/main/java/tech/derbent/bab/project/domain/CProject_Bab.java`

```java
package tech.derbent.bab.project.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
// ... other imports
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

@Entity
@DiscriminatorValue("BAB")
public class CProject_Bab extends CProject<CProject_Bab> {
    
    // ... existing fields
    
    /**
     * Polymorphic list of all virtual network nodes in this BAB project.
     * Can contain HTTP Server nodes, Vehicle nodes, File Input nodes, etc.
     * 
     * JPA Inheritance: Uses JOINED strategy to query across all node types.
     * Fetch Type: LAZY - nodes loaded on demand (can have many nodes).
     * Cascade: ALL - when project deleted, all nodes deleted.
     * Orphan Removal: true - removing node from list deletes from database.
     */
    @OneToMany(
        mappedBy = "project",  // References CBabNodeEntity.project field
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @AMetaData(
        displayName = "Network Nodes",
        required = false,
        readOnly = false,
        description = "All virtual network nodes configured for this BAB Gateway project",
        hidden = false
    )
    private List<CBabNodeEntity<?>> nodes = new ArrayList<>();
    
    // ... existing methods
    
    /**
     * Get all nodes (polymorphic list).
     * @return list of all node types
     */
    public List<CBabNodeEntity<?>> getNodes() {
        return nodes;
    }
    
    /**
     * Set all nodes (polymorphic list).
     * @param nodes list of nodes
     */
    public void setNodes(List<CBabNodeEntity<?>> nodes) {
        this.nodes = nodes;
        updateLastModified();
    }
    
    /**
     * Add a node to this project (type-safe).
     * @param node the node to add
     */
    public void addNode(CBabNodeEntity<?> node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            node.setProject(this);
            updateLastModified();
        }
    }
    
    /**
     * Remove a node from this project.
     * @param node the node to remove
     */
    public void removeNode(CBabNodeEntity<?> node) {
        if (nodes.remove(node)) {
            node.setProject(null);
            updateLastModified();
        }
    }
    
    /**
     * Get all HTTP Server nodes (filtered).
     * @return list of HTTP server nodes only
     */
    public List<CBabHttpServerNode> getHttpServerNodes() {
        return nodes.stream()
            .filter(n -> n instanceof CBabHttpServerNode)
            .map(n -> (CBabHttpServerNode) n)
            .toList();
    }
    
    /**
     * Get all Vehicle nodes (filtered).
     * @return list of vehicle nodes only
     */
    public List<CVehicleNode> getVehicleNodes() {
        return nodes.stream()
            .filter(n -> n instanceof CVehicleNode)
            .map(n -> (CVehicleNode) n)
            .toList();
    }
    
    /**
     * Get all File Input nodes (filtered).
     * @return list of file input nodes only
     */
    public List<CBabFileInputNode> getFileInputNodes() {
        return nodes.stream()
            .filter(n -> n instanceof CBabFileInputNode)
            .map(n -> (CBabFileInputNode) n)
            .toList();
    }
    
    /**
     * Get count of all nodes.
     * @return total number of nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Get count of active nodes.
     * @return number of active nodes
     */
    public int getActiveNodeCount() {
        return (int) nodes.stream()
            .filter(CBabNodeEntity::isActive)
            .count();
    }
    
    /**
     * Get count of connected nodes.
     * @return number of connected nodes
     */
    public int getConnectedNodeCount() {
        return (int) nodes.stream()
            .filter(CBabNodeEntity::canBeRuleSource)
            .count();
    }
}
```

## Database Schema Changes

### Tables Created

**1. cbab_node (Base Table)**
```sql
CREATE TABLE cbab_node (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    node_type VARCHAR(50) NOT NULL,  -- Discriminator: HTTP_SERVER, VEHICLE, FILE_INPUT
    name VARCHAR(255) NOT NULL,
    physical_interface VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    connection_status VARCHAR(20) NOT NULL DEFAULT 'DISCONNECTED',
    node_config TEXT,
    priority_level INTEGER NOT NULL DEFAULT 50,
    created_date TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP NOT NULL,
    created_by_id BIGINT,
    FOREIGN KEY (project_id) REFERENCES cproject(project_id),
    UNIQUE (project_id, name)
);
```

**2. cnode_http_server (HTTP Server Fields)**
```sql
CREATE TABLE cnode_http_server (
    http_server_node_id BIGINT PRIMARY KEY,
    server_port INTEGER NOT NULL DEFAULT 8080,
    endpoint_path VARCHAR(200) NOT NULL DEFAULT '/api',
    protocol VARCHAR(10) NOT NULL DEFAULT 'HTTP',
    ssl_enabled BOOLEAN NOT NULL DEFAULT false,
    max_connections INTEGER NOT NULL DEFAULT 100,
    timeout_seconds INTEGER NOT NULL DEFAULT 30,
    FOREIGN KEY (http_server_node_id) REFERENCES cbab_node(id),
    UNIQUE (project_id, physical_interface, server_port)  -- From existing unique constraint
);
```

**3. cnode_vehicle (Vehicle Fields)**
```sql
CREATE TABLE cnode_vehicle (
    vehicle_node_id BIGINT PRIMARY KEY,
    vehicle_id VARCHAR(50) NOT NULL,
    can_address INTEGER NOT NULL,
    baud_rate INTEGER NOT NULL DEFAULT 500000,
    vehicle_type VARCHAR(30) NOT NULL DEFAULT 'CAR',
    manufacturer VARCHAR(50),
    model_year INTEGER,
    can_protocol VARCHAR(20) NOT NULL DEFAULT 'CAN 2.0B',
    FOREIGN KEY (vehicle_node_id) REFERENCES cbab_node(id),
    UNIQUE (project_id, vehicle_id),
    UNIQUE (project_id, physical_interface, can_address)
);
```

**4. cnode_file_input (File Input Fields)**
```sql
CREATE TABLE cnode_file_input (
    file_input_node_id BIGINT PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL,
    file_format VARCHAR(20) NOT NULL DEFAULT 'JSON',
    watch_directory BOOLEAN NOT NULL DEFAULT false,
    file_pattern VARCHAR(100),
    FOREIGN KEY (file_input_node_id) REFERENCES cbab_node(id),
    UNIQUE (project_id, file_path)
);
```

### How JOINED Works

**Query: Get all nodes**
```java
List<CBabNodeEntity<?>> nodes = project.getNodes();
```

**Generated SQL** (JPA does this automatically):
```sql
SELECT 
    n.id, n.project_id, n.node_type, n.name, n.physical_interface,
    http.server_port, http.endpoint_path,      -- HTTP specific
    veh.vehicle_id, veh.can_address,           -- Vehicle specific  
    file.file_path, file.file_format           -- File Input specific
FROM cbab_node n
LEFT JOIN cnode_http_server http ON n.id = http.http_server_node_id
LEFT JOIN cnode_vehicle veh ON n.id = veh.vehicle_node_id
LEFT JOIN cnode_file_input file ON n.id = file.file_input_node_id
WHERE n.project_id = ?
```

**Result**: You get a polymorphic list where each object is its correct type!

## Usage Examples

### Adding Nodes

```java
CProject_Bab project = projectService.findById(projectId);

// Add HTTP Server node
CBabHttpServerNode httpNode = new CBabHttpServerNode("API Server", project, "eth0", 8080);
project.addNode(httpNode);  // ✅ Polymorphic add

// Add Vehicle node
CVehicleNode vehicleNode = new CVehicleNode("Vehicle 1", project, "VEHICLE001", 0x100);
project.addNode(vehicleNode);  // ✅ Polymorphic add

// Add File Input node
CBabFileInputNode fileNode = new CBabFileInputNode("Data Import", project, "/data/input");
project.addNode(fileNode);  // ✅ Polymorphic add

projectService.save(project);  // Saves all nodes due to CascadeType.ALL
```

### Querying Nodes

```java
// Get all nodes (polymorphic)
List<CBabNodeEntity<?>> allNodes = project.getNodes();
System.out.println("Total nodes: " + allNodes.size());

// Filter by type
List<CBabHttpServerNode> httpServers = project.getHttpServerNodes();
List<CVehicleNode> vehicles = project.getVehicleNodes();
List<CBabFileInputNode> fileInputs = project.getFileInputNodes();

// Get statistics
System.out.println("Active nodes: " + project.getActiveNodeCount());
System.out.println("Connected nodes: " + project.getConnectedNodeCount());

// Iterate polymorphically
for (CBabNodeEntity<?> node : allNodes) {
    System.out.println("Node: " + node.getName() + " (" + node.getNodeType() + ")");
    
    // Type-specific handling
    if (node instanceof CBabHttpServerNode httpServer) {
        System.out.println("  HTTP URL: " + httpServer.getFullUrl());
    } else if (node instanceof CVehicleNode vehicle) {
        System.out.println("  Vehicle ID: " + vehicle.getVehicleId());
        System.out.println("  CAN Address: " + vehicle.getCanAddressHex());
    } else if (node instanceof CBabFileInputNode fileInput) {
        System.out.println("  File Path: " + fileInput.getFilePath());
    }
}
```

### Removing Nodes

```java
// Remove specific node
CBabNodeEntity<?> nodeToRemove = allNodes.get(0);
project.removeNode(nodeToRemove);  // ✅ Orphan removal deletes from database

// Remove all vehicle nodes
List<CVehicleNode> vehiclesToRemove = project.getVehicleNodes();
vehiclesToRemove.forEach(project::removeNode);

projectService.save(project);
```

## Benefits of This Approach

### ✅ Type Safety
- Single list, but each element is correctly typed at runtime
- Can use `instanceof` for type-specific logic
- Compile-time checking for type-specific methods

### ✅ Database Efficiency
- Normalized schema (no nullable type-specific columns)
- Efficient queries with JPA LEFT JOINs
- Each node type has its own table for specific fields

### ✅ Flexibility
- Easy to add new node types (just extend `CBabNodeEntity`)
- Easy to query by specific type
- Easy to query all types together

### ✅ Derbent Pattern Compliance
- Follows JPA inheritance best practices
- Works with existing `CEntityOfProject` hierarchy
- Compatible with all Derbent abstractions

## Migration Path

### If Database Already Exists

**Option 1: Fresh Start** (RECOMMENDED for development)
```bash
# Drop existing tables
DROP TABLE IF EXISTS cnode_vehicle CASCADE;
DROP TABLE IF EXISTS cnode_http_server CASCADE;
DROP TABLE IF EXISTS cnode_file_input CASCADE;

# Let Hibernate recreate with new schema
mvn spring-boot:run -Dspring.profiles.active=bab -Dspring.jpa.hibernate.ddl-auto=create
```

**Option 2: Manual Migration** (for production with existing data)
```sql
-- 1. Create new base table
CREATE TABLE cbab_node ( /* fields */ );

-- 2. Migrate data from old tables to new structure
INSERT INTO cbab_node (id, project_id, node_type, ...)
SELECT id, project_id, 'HTTP_SERVER', ...
FROM cnode_http_server_old;

-- 3. Create child tables with foreign keys
CREATE TABLE cnode_http_server ( /* fields with FK to cbab_node */ );

-- 4. Drop old tables
DROP TABLE cnode_http_server_old;
```

## Testing Checklist

- [ ] Create CProject_Bab
- [ ] Add HTTP Server node → verify saved
- [ ] Add Vehicle node → verify saved
- [ ] Add File Input node → verify saved
- [ ] Query nodes → verify all 3 returned
- [ ] Filter by type → verify correct filtering
- [ ] Remove node → verify deleted from database
- [ ] Delete project → verify cascade delete of all nodes
- [ ] Check database schema → verify JOINED tables created

## Files to Modify

1. ✅ `CBabNodeEntity.java` - Change `@MappedSuperclass` to `@Entity + @Inheritance`
2. ✅ `CBabHttpServerNode.java` - Add `@DiscriminatorValue("HTTP_SERVER")`
3. ✅ `CVehicleNode.java` - Add `@DiscriminatorValue("VEHICLE")`
4. ✅ `CBabFileInputNode.java` - Add `@DiscriminatorValue("FILE_INPUT")`
5. ✅ `CProject_Bab.java` - Add `@OneToMany List<CBabNodeEntity<?>> nodes` field + methods

**Total Impact**: 5 files, ~100 lines of code

---

## Ready to Implement?

Would you like me to:
1. ✅ **Apply all changes now** (I can modify all 5 files)
2. ⚠️ Show step-by-step with verification between steps
3. 📋 Create migration SQL scripts for existing database

**Recommendation**: Let me apply all changes at once - it's a clean, well-tested pattern! 🚀

