# BAB Policybase Trigger, Action, and Filter System - Implementation Complete

**Date**: 2026-02-13  
**Status**: ✅ **IMPLEMENTATION COMPLETE**  
**Profile**: BAB Gateway (`@Profile("bab")`)

## 🎯 **Overview**

Successfully implemented a comprehensive BAB policybase system with database entities for:
- **Triggers** - When rules should execute (periodic, startup, manual, always, once)
- **Actions** - What should happen (forward, transform, store, notify, execute, filter, validate, log)
- **Filters** - How data should be processed (CSV, JSON, XML, regex, range, condition, transform, validate)

All entities follow Derbent patterns and integrate seamlessly with the existing CBabPolicyRule system.

## 🏗️ **Architecture**

### Entity Structure
```
CBabPolicyRule (existing)
├── triggers: Set<CBabPolicyTrigger>     ← NEW Many-to-Many
├── actions: Set<CBabPolicyAction>       ← NEW Many-to-Many  
└── filters: Set<CBabPolicyFilter>       ← NEW Many-to-Many
```

### Database Schema
- `cbab_policy_trigger` - Trigger configurations
- `cbab_policy_action` - Action configurations
- `cbab_policy_filter` - Filter configurations
- `cbab_policy_rule_triggers` - Join table (Rule ← → Triggers)
- `cbab_policy_rule_actions` - Join table (Rule ← → Actions)
- `cbab_policy_rule_filters` - Join table (Rule ← → Filters)

### Package Structure
```
src/main/java/tech/derbent/bab/policybase/
├── trigger/
│   ├── domain/CBabPolicyTrigger.java
│   └── service/
│       ├── IBabPolicyTriggerRepository.java
│       ├── CBabPolicyTriggerService.java
│       ├── CBabPolicyTriggerInitializerService.java
│       └── CPageServiceBabPolicyTrigger.java
├── action/
│   ├── domain/CBabPolicyAction.java
│   └── service/ (same structure as trigger)
├── filter/
│   ├── domain/CBabPolicyFilter.java
│   └── service/ (same structure as trigger)
└── service/CBabPolicybaseInitializerService.java
```

## 🚀 **Features Implemented**

### CBabPolicyTrigger Entity
- **Trigger Types**: `periodic`, `at_start`, `manual`, `always`, `once`
- **Cron Expression**: For periodic triggers (e.g., `"0 */5 * * * *"`)
- **Node Type Filtering**: Enable/disable for specific node types (CAN, Modbus, HTTP, File, Syslog, ROS)
- **Execution Settings**: Priority, order, timeout, retry count
- **Condition JSON**: Complex trigger logic configuration

**Example Trigger Types**:
- **Periodic**: `"0 */5 * * * *"` (every 5 minutes)
- **At Start**: Execute once on system startup
- **Manual**: User-initiated execution
- **Always**: Continuous monitoring
- **Once**: Single execution only

### CBabPolicyAction Entity  
- **Action Types**: `forward`, `transform`, `store`, `notify`, `execute`, `filter`, `validate`, `log`
- **Execution Modes**: Synchronous/Asynchronous with timeout and retry
- **Configuration JSON**: Action-specific parameters
- **Template JSON**: Data transformation templates
- **Logging Control**: Input/output/execution logging

**Example Action Types**:
- **Forward**: Route data to destination
- **Transform**: Modify data structure (CSV → JSON)
- **Store**: Persist to database/file
- **Notify**: Send email/SMS alerts
- **Execute**: Run external commands/scripts
- **Filter**: Apply data filtering rules
- **Validate**: Schema validation
- **Log**: System event logging

### CBabPolicyFilter Entity
- **Filter Types**: `csv`, `json`, `xml`, `regex`, `range`, `condition`, `transform`, `validate`
- **Logic Operations**: AND, OR, NOT conditions
- **Performance**: Caching, processing time limits
- **Configuration JSON**: Filter-specific settings
- **Transformation JSON**: Data transformation rules

**Example Filter Types**:
- **CSV**: Row/column filtering with delimiters
- **JSON**: Object path filtering (`$.data.sensors`)
- **XML**: Element/attribute filtering
- **Regex**: Pattern matching
- **Range**: Numeric range validation
- **Condition**: Business logic rules
- **Transform**: Data structure transformation
- **Validate**: Schema validation

### Node Type Compatibility
All entities support filtering by node types:
- ✅ **CAN Nodes** - Vehicle communication
- ✅ **Modbus Nodes** - Industrial protocols
- ✅ **HTTP Nodes** - Web services
- ✅ **File Nodes** - File processing
- ✅ **Syslog Nodes** - System logging
- ✅ **ROS Nodes** - Robot Operating System

## 📊 **Sample Data**

### Triggers (5 samples)
1. **Data Collection Periodic** - Every 5 minutes sensor data collection
2. **System Startup** - Initialize system on boot
3. **Emergency Stop** - Manual emergency trigger
4. **Continuous Monitor** - Always-on system monitoring
5. **Initial Configuration** - One-time setup

### Actions (8 samples)
1. **Forward to Database** - Route sensor data
2. **Transform JSON** - CSV to JSON conversion
3. **Store to File** - File system persistence
4. **Email Alert** - Critical event notifications
5. **Restart Service** - Auto-recovery commands
6. **Filter Invalid Data** - Data quality control
7. **Validate Schema** - Data validation
8. **System Logger** - Event logging

### Filters (8 samples)
1. **CSV Data Filter** - Sensor reading files
2. **JSON API Filter** - API endpoint data
3. **XML Config Filter** - Configuration files
4. **Text Pattern Filter** - Regex matching
5. **Numeric Range Filter** - Value validation
6. **Business Rule Filter** - Logic conditions
7. **Data Transform Filter** - Structure transformation
8. **Schema Validation Filter** - Format validation

## 🔄 **Integration with CBabPolicyRule**

Updated existing `CBabPolicyRule` entity with many-to-many relationships:

```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "cbab_policy_rule_triggers", ...)
private Set<CBabPolicyTrigger> triggers = new HashSet<>();

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "cbab_policy_rule_actions", ...)
private Set<CBabPolicyAction> actions = new HashSet<>();

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "cbab_policy_rule_filters", ...)
private Set<CBabPolicyFilter> filters = new HashSet<>();
```

**Business Methods**:
- `addTrigger()`, `removeTrigger()`
- `addAction()`, `removeAction()`
- `addFilter()`, `removeFilter()`
- `hasCompleteConfiguration()` - Check rule completeness
- `getComponentCount()` - Count all components

## ⚙️ **Configuration Examples**

### Periodic Trigger Configuration
```json
{
    "type": "periodic",
    "interval": 3600,
    "unit": "seconds",
    "enabled": true
}
```

### Transform Action Configuration
```json
{
    "type": "transform",
    "transformationType": "json",
    "template": "{{input}}",
    "enabled": true
}
```

### CSV Filter Configuration  
```json
{
    "type": "csv",
    "delimiter": ",",
    "startLineNumber": 2,
    "maxLineNumber": 0,
    "hasHeaders": true,
    "encoding": "UTF-8"
}
```

## 🎯 **Business Logic**

### Execution Flow
1. **Trigger Evaluation** - Check conditions (cron, startup, manual, etc.)
2. **Rule Activation** - Fire associated policy rules
3. **Filter Processing** - Apply data filters in execution order
4. **Action Execution** - Execute actions in priority/order sequence
5. **Result Handling** - Log outcomes, handle errors, retry as needed

### Node Type Filtering
- Each entity can be enabled/disabled for specific node types
- Allows fine-grained control over which nodes use which components
- Runtime compatibility checking via `isEnabledForNodeType(nodeType)`

### Execution Priority
- **Triggers**: Higher priority = earlier execution
- **Actions**: Higher priority = earlier execution  
- **Filters**: Execution order determines processing sequence
- **Async Support**: Actions can run synchronously or asynchronously

## 📝 **Validation Rules**

### Triggers
- Trigger type is required
- Periodic triggers require valid cron expression
- At least one node type must be enabled
- Execution order must be non-negative

### Actions  
- Action type is required
- Execute actions require command configuration
- Notify/Transform actions require templates
- Forward actions cannot be asynchronous
- At least one node type must be enabled

### Filters
- Filter type is required
- Logic operator must be AND, OR, or NOT
- Null handling must be ignore, reject, pass, or default
- Transform filters require transformation templates
- At least one node type must be enabled

## 🛠️ **Data Initialization**

Sample data is automatically created via `CBabPolicybaseInitializerService`:
- Integrated into `CBabDataInitializer`
- Creates realistic examples for all entity types
- Proper relationships and configurations
- Ready for testing and development

## ✅ **Implementation Checklist**

### Entities ✅
- [x] CBabPolicyTrigger - Complete with all trigger types
- [x] CBabPolicyAction - Complete with all action types  
- [x] CBabPolicyFilter - Complete with all filter types
- [x] Updated CBabPolicyRule with many-to-many relationships

### Services ✅
- [x] Repository interfaces with custom queries
- [x] Service classes with business logic validation
- [x] Initializer services for UI form generation
- [x] Page services for dynamic routing

### Validation ✅
- [x] String length validation with helpers
- [x] Unique name validation per project
- [x] Business rule validation
- [x] Node type configuration validation
- [x] Type-specific field validation

### Sample Data ✅
- [x] 5 sample triggers (all types)
- [x] 8 sample actions (all types)
- [x] 8 sample filters (all types)
- [x] Integrated into BAB data initializer

### Database Schema ✅
- [x] Entity tables with proper constraints
- [x] Many-to-many join tables
- [x] Foreign key relationships
- [x] Unique constraints per project

## 🔮 **Next Steps**

1. **UI Components** - Create specialized components for trigger/action/filter configuration
2. **Rule Builder** - Drag & drop interface for assembling rules
3. **Execution Engine** - Runtime engine for executing configured policies
4. **Monitoring Dashboard** - Real-time view of trigger/action execution
5. **API Integration** - REST endpoints for external system integration

## 📊 **Impact Summary**

- **3 New Entities** - Trigger, Action, Filter with full CRUD
- **21 New Files** - Complete service layer implementation  
- **Sample Data** - 21 sample entities with realistic configurations
- **Many-to-Many Integration** - Flexible policy rule composition
- **Node Type Support** - 6 node types with granular filtering
- **JSON Configuration** - Flexible, extensible configuration system

**🎯 The BAB policybase system is now ready for rule-based automation and policy management!** ✨