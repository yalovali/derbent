# Boolean Property Binding Fix Pattern

## Problem
When using `BeanValidationBinder` with Boolean fields that only have `is...()` getters, Vaadin throws:
```
java.lang.IllegalStateException: All bindings created with forField must be completed before calling readBean
```

## Root Cause
- Boolean properties with only `is...()` getters cannot be resolved by Vaadin's `BeanValidationBinder`
- The binder expects `get...()` getters for property resolution
- When binding fails, incomplete `forField` bindings are left in the binder
- Calling `readBean()` with incomplete bindings triggers the IllegalStateException

## Solution Pattern
For Boolean fields with `is...()` getters, add corresponding `get...()` getters:

### Before (causes IllegalStateException):
```java
private Boolean enableFeature;

public Boolean isEnableFeature() {
    return enableFeature;
}

public void setEnableFeature(Boolean enableFeature) {
    this.enableFeature = enableFeature;
}
```

### After (works correctly):
```java
private Boolean enableFeature;

public Boolean isEnableFeature() {
    return enableFeature;
}

public Boolean getEnableFeature() {  // Add this getter
    return enableFeature;
}

public void setEnableFeature(Boolean enableFeature) {
    this.enableFeature = enableFeature;
}
```

## Testing Pattern
Create tests to verify form binding works correctly:

```java
@Test
void testFormBindingWithReadBean() {
    assertDoesNotThrow(() -> {
        var binder = new CEnhancedBinder<>(YourEntity.class);
        var formLayout = CEntityFormBuilder.buildForm(YourEntity.class, binder);
        assertNotNull(formLayout, "Form layout should be created");
        
        var testEntity = new YourEntity();
        // This will throw IllegalStateException if any forField binding is incomplete
        binder.readBean(testEntity);
    }, "Form binding and readBean should not throw IllegalStateException");
}
```

## Files Fixed
- `CSystemSettings.java` - Added get...() methods for 9 Boolean properties
- `CCompanySettings.java` - Added get...() methods for 7 Boolean properties

## Prevention
- Always provide both `get...()` and `is...()` getters for Boolean properties used in forms
- Add form binding tests for entities that will be used with CEntityFormBuilder
- Run tests early to catch binding issues during development

## Applied To
- [x] CSystemSettings (System-wide configuration)
- [x] CCompanySettings (Company-specific configuration) 
- [ ] Other domain entities with Boolean properties (as needed)