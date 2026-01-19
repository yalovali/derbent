# Standards Implementation Roadmap

**Created:** 2026-01-17  
**Status:** Planning Complete - Ready for Implementation

## Phase 1: Risk Management Standards (ISO 31000) ✅ PRIORITY

### Entity: CRisk
**Standard:** ISO 31000:2018 Risk Management

#### Changes Required (All Non-Breaking)
```java
// Add to CRisk.java

// 1. Probability Score (quantitative)
@Column(nullable = true)
@AMetaData(
    displayName = "Probability (1-10)",
    description = "Likelihood of risk occurrence - ISO 31000 quantitative scale",
    required = false,
    order = 50
)
private Integer probability; // 1-10 scale

// 2. Impact Score (quantitative)  
@Column(nullable = true)
@AMetaData(
    displayName = "Impact Score (1-10)",
    description = "Magnitude of consequences if risk occurs - ISO 31000",
    required = false,
    order = 51
)
private Integer impactScore; // 1-10 scale

// 3. Risk Response Strategy (ISO 31000 standard treatment options)
@Enumerated(EnumType.STRING)
@Column(name = "risk_response_strategy", length = 20)
@AMetaData(
    displayName = "Response Strategy",
    description = "Risk treatment approach per ISO 31000",
    required = false,
    order = 52
)
private ERiskResponseStrategy riskResponseStrategy;

// 4. Residual Risk Assessment
@Column(nullable = true, length = 2000)
@Size(max = 2000)
@AMetaData(
    displayName = "Residual Risk",
    description = "Remaining risk after mitigation measures - ISO 31000",
    required = false,
    maxLength = 2000,
    order = 53
)
private String residualRisk;

// 5. Calculated Risk Score (Probability × Impact)
@Transient
public Integer getRiskScore() {
    if (probability != null && impactScore != null) {
        return probability * impactScore; // 1-100 scale
    }
    return null;
}

// 6. Risk Matrix Category
@Transient  
public String getRiskMatrixCategory() {
    Integer score = getRiskScore();
    if (score == null) return "Not Assessed";
    if (score >= 75) return "Critical";
    if (score >= 50) return "High";
    if (score >= 25) return "Medium";
    return "Low";
}
```

#### New Enum: ERiskResponseStrategy
```java
package tech.derbent.plm.risks.risk.domain;

public enum ERiskResponseStrategy {
    AVOID("Avoid", "Eliminate the risk by changing plans"),
    TRANSFER("Transfer", "Shift risk impact to third party (insurance, contract)"),
    MITIGATE("Mitigate", "Reduce probability or impact"),
    ACCEPT("Accept", "Acknowledge risk, no proactive action"),
    ESCALATE("Escalate", "Escalate to higher authority");
    
    private final String displayName;
    private final String description;
    
    ERiskResponseStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
```

#### Display Name Updates (CRiskInitializerService)
```java
// Update existing field displays (non-breaking)
// In createBasicView() method:

detailSection.addScreenLine(CDetailLinesService.createSection("Risk Assessment - ISO 31000"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "probability"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "impactScore"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "riskScore")); // Transient
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "riskMatrixCategory")); // Transient

detailSection.addScreenLine(CDetailLinesService.createSection("Risk Treatment - ISO 31000"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "riskResponseStrategy"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "mitigation")); // Existing
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "residualRisk"));

// Update @AMetaData displayName for existing field "assignedTo"
// In CRisk.java @AMetaData annotation:
@AMetaData(
    displayName = "Risk Owner", // Changed from "Assigned To"
    // ... rest unchanged
)
private CUser assignedTo; // Field name unchanged!
```

#### Files to Modify
1. `/src/main/java/tech/derbent/app/risks/risk/domain/CRisk.java` - Add fields
2. `/src/main/java/tech/derbent/app/risks/risk/domain/ERiskResponseStrategy.java` - NEW file
3. `/src/main/java/tech/derbent/app/risks/risk/service/CRiskInitializerService.java` - Update view
4. `/src/main/java/tech/derbent/app/risks/risk/service/CRiskService.java` - Add any business logic

---

## Phase 2: Sprint Management Standards (Scrum Guide 2020)

### Entity: CSprint
**Standard:** Scrum Guide 2020 (Scrum.org)

#### Changes Required (All Non-Breaking)
```java
// Add to CSprint.java

// 1. Sprint Goal (Core Scrum Artifact)
@Column(nullable = true, length = 500)
@Size(max = 500)
@AMetaData(
    displayName = "Sprint Goal",
    description = "The single objective for the Sprint - Scrum Guide 2020",
    required = false,
    maxLength = 500,
    order = 15
)
private String sprintGoal;

// 2. Definition of Done
@Column(nullable = true, length = 2000)
@Size(max = 2000)
@AMetaData(
    displayName = "Definition of Done",
    description = "Shared understanding of what it means for work to be complete",
    required = false,
    maxLength = 2000,
    order = 16
)
private String definitionOfDone;

// 3. Velocity (Calculated)
@Column(nullable = true)
@AMetaData(
    displayName = "Velocity",
    description = "Story points completed in this sprint",
    required = false,
    readOnly = true,
    order = 17
)
private Integer velocity;

// 4. Retrospective Notes
@Column(nullable = true, length = 4000)
@Size(max = 4000)
@AMetaData(
    displayName = "Retrospective Notes",
    description = "What went well, what needs improvement, action items",
    required = false,
    maxLength = 4000,
    order = 18
)
private String retrospectiveNotes;

// 5. Calculate velocity from completed items
public void calculateVelocity() {
    if (sprintItems == null) {
        velocity = 0;
        return;
    }
    velocity = sprintItems.stream()
        .filter(item -> item.getParentItem() != null)
        .filter(item -> {
            ISprintableItem parent = item.getParentItem();
            if (parent.getStatus() != null && parent.getStatus().getFinalStatus()) {
                return true;
            }
            return false;
        })
        .map(CSprintItem::getStoryPoint)
        .filter(sp -> sp != null)
        .mapToInt(Long::intValue)
        .sum();
}
```

#### Files to Modify
1. `/src/main/java/tech/derbent/app/sprints/domain/CSprint.java` - Add fields
2. `/src/main/java/tech/derbent/app/sprints/service/CSprintInitializerService.java` - Update view
3. `/src/main/java/tech/derbent/app/sprints/service/CSprintService.java` - Add velocity calculation

---

## Phase 3: Kanban Method Standards

### Entity: CKanbanColumn
**Standard:** Kanban Method (David J. Anderson, 2010)

#### Changes Required (All Non-Breaking)
```java
// Add to CKanbanColumn.java

// 1. WIP Limit
@Column(nullable = true)
@AMetaData(
    displayName = "WIP Limit",
    description = "Work In Progress limit for this column - Kanban Method",
    required = false,
    order = 50
)
private Integer wipLimit;

// 2. WIP Limit Enabled Flag
@Column(nullable = false)
@AMetaData(
    displayName = "Enforce WIP Limit",
    description = "Block adding items when limit exceeded",
    required = false,
    defaultValue = "false",
    order = 51
)
private Boolean wipLimitEnabled = false;

// 3. Class of Service
@Enumerated(EnumType.STRING)
@Column(name = "service_class", length = 20)
@AMetaData(
    displayName = "Class of Service",
    description = "Priority policy for this column - Kanban Method",
    required = false,
    order = 52
)
private EServiceClass serviceClass;

// 4. Calculate current WIP
@Transient
public Integer getCurrentWIP() {
    // Query count of items in this column from service
    return null; // Implemented in CKanbanColumnService
}

// 5. Check if WIP limit exceeded
@Transient
public boolean isWIPLimitExceeded() {
    if (!wipLimitEnabled || wipLimit == null) return false;
    Integer currentWIP = getCurrentWIP();
    return currentWIP != null && currentWIP >= wipLimit;
}
```

#### New Enum: EServiceClass
```java
package tech.derbent.plm.kanban.kanbanline.domain;

public enum EServiceClass {
    EXPEDITE("Expedite", "Critical, immediate action", "#FF0000"),
    FIXED_DATE("Fixed Date", "Hard deadline", "#FF9900"),
    STANDARD("Standard", "Normal priority", "#0099FF"),
    INTANGIBLE("Intangible", "Low urgency, background", "#999999");
    
    private final String displayName;
    private final String description;
    private final String color;
    
    EServiceClass(String displayName, String description, String color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
}
```

#### Files to Modify
1. `/src/main/java/tech/derbent/app/kanban/kanbanline/domain/CKanbanColumn.java` - Add fields
2. `/src/main/java/tech/derbent/app/kanban/kanbanline/domain/EServiceClass.java` - NEW file
3. `/src/main/java/tech/derbent/app/kanban/kanbanline/service/CKanbanLineInitializerService.java` - Update view
4. `/src/main/java/tech/derbent/app/kanban/kanbanline/service/CKanbanColumnService.java` - WIP calculation

---

## Phase 4: Budget EVM Standards (PMBOK)

### Entity: CBudget
**Standard:** PMI PMBOK Earned Value Management

#### Changes Required (All Non-Breaking)
```java
// Add to CBudget.java

// 1. Planned Value (Budget Baseline)
@Column(nullable = true, precision = 15, scale = 2)
@DecimalMin("0.0")
@AMetaData(
    displayName = "Planned Value (PV)",
    description = "Budget baseline for scheduled work - PMBOK EVM",
    required = false,
    order = 50
)
private BigDecimal plannedValue;

// 2. Earned Value
@Column(nullable = true, precision = 15, scale = 2)
@DecimalMin("0.0")
@AMetaData(
    displayName = "Earned Value (EV)",
    description = "Budgeted cost of work performed - PMBOK EVM",
    required = false,
    order = 51
)
private BigDecimal earnedValue;

// 3. Cost Variance (CV = EV - AC)
public BigDecimal getCostVariance() {
    if (earnedValue == null || actualCost == null) return BigDecimal.ZERO;
    return earnedValue.subtract(actualCost);
}

// 4. Schedule Variance (SV = EV - PV)
public BigDecimal getScheduleVariance() {
    if (earnedValue == null || plannedValue == null) return BigDecimal.ZERO;
    return earnedValue.subtract(plannedValue);
}

// 5. Cost Performance Index (CPI = EV / AC)
public BigDecimal getCostPerformanceIndex() {
    if (actualCost == null || actualCost.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE;
    if (earnedValue == null) return BigDecimal.ZERO;
    return earnedValue.divide(actualCost, 2, RoundingMode.HALF_UP);
}

// 6. Schedule Performance Index (SPI = EV / PV)
public BigDecimal getSchedulePerformanceIndex() {
    if (plannedValue == null || plannedValue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE;
    if (earnedValue == null) return BigDecimal.ZERO;
    return earnedValue.divide(plannedValue, 2, RoundingMode.HALF_UP);
}
```

#### Files to Modify
1. `/src/main/java/tech/derbent/app/budgets/budget/domain/CBudget.java` - Add fields & methods
2. `/src/main/java/tech/derbent/app/budgets/budget/service/CBudgetInitializerService.java` - Update view

---

## Implementation Priority

### Week 1: Risk Management (ISO 31000) ⭐ HIGH PRIORITY
- [ ] Add probability, impactScore fields to CRisk
- [ ] Create ERiskResponseStrategy enum
- [ ] Add residualRisk field
- [ ] Update CRiskInitializerService view
- [ ] Add calculated riskScore method
- [ ] Test: Create risk with probability=7, impact=8, verify score=56

### Week 2: Sprint Standards (Scrum Guide)
- [ ] Add sprintGoal field to CSprint
- [ ] Add definitionOfDone field
- [ ] Add velocity calculation
- [ ] Add retrospectiveNotes field
- [ ] Update CSprintInitializerService view
- [ ] Test: Create sprint with goal, calculate velocity

### Week 3: Kanban Method
- [ ] Add wipLimit, wipLimitEnabled to CKanbanColumn
- [ ] Create EServiceClass enum
- [ ] Add WIP calculation logic
- [ ] Update kanban board to show WIP limits
- [ ] Test: Set WIP limit=3, verify enforcement

### Week 4: Budget EVM
- [ ] Add plannedValue, earnedValue to CBudget
- [ ] Add EVM calculation methods (CV, SV, CPI, SPI)
- [ ] Update budget view with EVM metrics
- [ ] Test: Calculate CPI, SPI for sample budget

---

## Testing Checklist

For each phase:
- [ ] Existing tests pass unchanged
- [ ] New fields are nullable/optional
- [ ] Default values provided where needed
- [ ] UI displays correctly
- [ ] Calculated fields work properly
- [ ] No breaking changes to APIs

---

## Documentation Updates

For each phase:
- [ ] Update entity Javadoc with standard references
- [ ] Add inline comments with standard abbreviations
- [ ] Update initializer service with new sections
- [ ] Add examples in service tests
- [ ] Update user documentation

---

## Backlog Excel Updates

After implementation:
- [ ] Add "Standard" column to Epics sheet with references
- [ ] Add "Compliance Status" column
- [ ] Mark completed features as "Standards Compliant"
- [ ] Add standard abbreviations (ISO 31000, PMBOK, etc.)

---

**Next Step:** Begin Phase 1 - Risk Management Standards Implementation
