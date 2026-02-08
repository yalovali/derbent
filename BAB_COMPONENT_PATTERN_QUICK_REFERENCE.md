# BAB Component Pattern - Quick Reference

## ✅ Correct Pattern (Current Implementation)

### Base Class
```java
// CComponentBabBase.java
protected CComponentBabBase(final ISessionService sessionService) {
    this.sessionService = sessionService;  // NO initializeComponents() call
}

protected final void initializeComponents() {  // FINAL - cannot override
    setId(getID_ROOT());
    configureComponent();
    add(createHeader());
    add(createStandardToolbar());
    createGrid();
}
```

### Concrete Class
```java
public CComponentSystemMetrics(final ISessionService sessionService) {
    super(sessionService);
    initializeComponents();  // ✅ EXPLICIT CALL REQUIRED
}

@Override
protected void configureComponent() {
    super.configureComponent();  // ✅ Call super first
    createCustomUI();             // ✅ Custom setup
}

@Override
protected void createGrid() {
    // Implement grid OR leave empty
}
```

## Mandatory Methods

| Method | Type | Must Override? | Purpose |
|--------|------|----------------|---------|
| `getID_ROOT()` | Abstract | ✅ YES | Component HTML ID |
| `getHeaderText()` | Abstract | ✅ YES | Header title |
| `createGrid()` | Abstract | ✅ YES | Grid setup (can be empty) |
| `createCalimeroClient()` | Abstract | ✅ YES | Calimero client factory |
| `configureComponent()` | Hook | ⚠️ If custom UI | Custom initialization |
| `refreshComponent()` | Hook | ⚠️ If data needed | Data loading |

## Component Categories

**Category A - Grid**: Standard data grid  
**Category B - Custom UI**: Cards, panels, no grid  
**Category C - Hybrid**: Grid + custom initialization  

## Checklist for New Component

- [ ] Constructor calls `super(sessionService)` then `initializeComponents()`
- [ ] Implements `getID_ROOT()`, `getHeaderText()`, `createGrid()`, `createCalimeroClient()`
- [ ] Overrides `configureComponent()` if custom UI needed
- [ ] Calls `super.configureComponent()` first
- [ ] Creates grid in `createGrid()` OR leaves empty
- [ ] Overrides `refreshComponent()` if data loading needed

## See Also

- **BAB_COMPONENT_PATTERN.md** - Complete documentation
- **INITIALIZE_COMPONENTS_PATTERN_FINAL.md** - Pattern history
