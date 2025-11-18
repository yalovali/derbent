# Component Callback Pattern - Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         COMPONENT CALLBACK PATTERN FLOW                      │
└─────────────────────────────────────────────────────────────────────────────┘

STEP 1: Define Callback Methods in CPageService Subclass
────────────────────────────────────────────────────────────
┌────────────────────────────────────────────────────────────┐
│ public class CPageServiceActivity                          │
│     extends CPageServiceWithWorkflow<CActivity> {          │
│                                                            │
│   // Old signature - backward compatible                   │
│   protected void on_name_changed() {                       │
│     LOGGER.debug("Name changed");                          │
│   }                                                        │
│                                                            │
│   // New signature - with value access                     │
│   protected void on_status_change(Component c, Object v) { │
│     LOGGER.info("Status: {}", v);                          │
│   }                                                        │
│ }                                                          │
└────────────────────────────────────────────────────────────┘
                           │
                           │
                           ▼
STEP 2: Activate Pattern in bind() Method
──────────────────────────────────────────
┌────────────────────────────────────────────────────────────┐
│ @Override                                                   │
│ public void bind() {                                        │
│   super.bind();                                             │
│   detailsBuilder = view.getDetailsBuilder();                │
│   if (detailsBuilder != null) {                            │
│     formBuilder = detailsBuilder.getFormBuilder();          │
│   }                                                        │
│   bindMethods(this); // ← Activates callback pattern       │
│ }                                                          │
└────────────────────────────────────────────────────────────┘
                           │
                           │
                           ▼
STEP 3: bindMethods() Scans and Binds Callbacks
────────────────────────────────────────────────
┌────────────────────────────────────────────────────────────┐
│ bindMethods(CPageService page) {                            │
│   1. Get all methods from page class                        │
│   2. Filter methods matching: on_[field]_[action]          │
│   3. For each matching method:                              │
│      a. Extract field name (e.g., "name", "status")        │
│      b. Extract action (e.g., "change", "click")           │
│      c. Find component in FormBuilder                       │
│      d. Bind method to component event                      │
│ }                                                          │
└────────────────────────────────────────────────────────────┘
                           │
                           │
                           ▼
STEP 4: bindComponent() Attaches Event Listeners
─────────────────────────────────────────────────
┌────────────────────────────────────────────────────────────┐
│ bindComponent(method, component, action) {                  │
│   1. Check method parameter count:                          │
│      • 0 params → old signature                            │
│      • 1 param  → component only                           │
│      • 2 params → component + value                        │
│                                                            │
│   2. Attach event listener based on action:                │
│      • "change"  → addValueChangeListener()                │
│      • "click"   → addClickListener()                      │
│      • "focus"   → addEventListener("focus")               │
│      • "blur"    → addEventListener("blur")                │
│                                                            │
│   3. Configure listener to invoke method with              │
│      appropriate parameters when event fires                │
│ }                                                          │
└────────────────────────────────────────────────────────────┘
                           │
                           │
                           ▼
STEP 5: Runtime - User Interacts with UI
─────────────────────────────────────────
┌─────────────────────┐
│   USER TYPES IN     │
│   "name" FIELD      │
│   Value: "John"     │
└─────────────────────┘
            │
            │ ValueChangeEvent
            ▼
┌─────────────────────────────────────────────────────────────┐
│ Event Listener (attached by bindComponent)                  │
│ • Captures new value: "John"                                │
│ • Invokes: on_name_change(component, "John")               │
└─────────────────────────────────────────────────────────────┘
            │
            │ Method invocation
            ▼
┌─────────────────────────────────────────────────────────────┐
│ protected void on_name_change(Component c, Object value) {  │
│   LOGGER.info("Name changed to: {}", value);                │
│   // Can access component:                                  │
│   TextField field = getTextField("name");                   │
│   // Can access other components:                           │
│   setComponentValue("fullName", value + " Doe");            │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘


COMPONENT ACCESSOR METHODS FLOW
─────────────────────────────────
┌──────────────────────────────────────────────────────────────┐
│                     CPageService API                          │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  getComponentByName("name")  ──────┐                         │
│                                    │                         │
│  getComponent("name", TextField)   │  Access FormBuilder    │
│                                    │  Component Map          │
│  getTextField("name")              │                         │
│                                    ▼                         │
│  getComboBox("status")   ────►  FormBuilder                  │
│                                  componentMap                 │
│  getCheckbox("active")           {"name": TextField,         │
│                                   "status": ComboBox,        │
│  getDatePicker("dueDate")         "active": Checkbox, ...}   │
│                                    │                         │
│                                    │                         │
│  getComponentValue("name")         │  Read value             │
│                                    ▼                         │
│  setComponentValue("name", val)  Component.getValue()        │
│                                  Component.setValue()        │
└──────────────────────────────────────────────────────────────┘


SUPPORTED EVENT TYPES
──────────────────────

┌─────────────┬──────────────────┬─────────────────────────────────┐
│ Event Type  │ Component Types  │ Use Case                        │
├─────────────┼──────────────────┼─────────────────────────────────┤
│ change      │ TextField        │ Monitor text input              │
│ changed     │ TextArea         │ Real-time validation            │
│             │ ComboBox         │ Update dependent fields         │
│             │ Checkbox         │                                 │
│             │ DatePicker       │                                 │
├─────────────┼──────────────────┼─────────────────────────────────┤
│ click       │ Button           │ Handle button clicks            │
├─────────────┼──────────────────┼─────────────────────────────────┤
│ focus       │ All focusable    │ Show help text                  │
│             │ components       │ Load suggestions                │
├─────────────┼──────────────────┼─────────────────────────────────┤
│ blur        │ All focusable    │ Validate on focus loss          │
│             │ components       │ Auto-save                       │
└─────────────┴──────────────────┴─────────────────────────────────┘


METHOD SIGNATURE DETECTION
───────────────────────────

┌──────────────────────────────────┬──────────────────────────────┐
│ Method Signature                 │ Invocation                   │
├──────────────────────────────────┼──────────────────────────────┤
│ on_name_change()                 │ method.invoke(this)          │
│ • No parameters                  │ • Old format                 │
├──────────────────────────────────┼──────────────────────────────┤
│ on_name_change(Component c)      │ method.invoke(this,          │
│ • 1 parameter                    │               component)     │
├──────────────────────────────────┼──────────────────────────────┤
│ on_name_change(Component c,      │ method.invoke(this,          │
│                Object value)     │               component,     │
│ • 2+ parameters                  │               newValue)      │
└──────────────────────────────────┴──────────────────────────────┘


EXAMPLE: Complete Workflow
───────────────────────────

1. Developer writes:
   protected void on_price_change(Component c, Object value) { ... }

2. bind() calls:
   bindMethods(this)

3. bindMethods() finds:
   • Method name: "on_price_change"
   • Field: "price"
   • Action: "change"
   • Component: NumberField (from FormBuilder)

4. bindComponent() attaches:
   • ValueChangeListener to the NumberField
   • Listener invokes method with (component, newValue)

5. User changes price in UI:
   • NumberField fires ValueChangeEvent
   • Listener captures event
   • Invokes: on_price_change(numberField, 99.99)
   • Developer's code executes with direct access to value

6. Developer's code can:
   • Log the value: LOGGER.info("Price: {}", value)
   • Access component: NumberField field = getComponent("price", ...)
   • Update other fields: setComponentValue("total", value * 1.08)
   • Validate: if (value < 0) showWarning(...)
   • Auto-save: actionSave()
```

## Key Benefits Illustrated

1. **Declarative**: Just write `on_[field]_[action]` - no manual event setup
2. **Flexible**: Choose signature based on needs (0, 1, or 2 params)
3. **Type-Safe**: Use type-specific getters for compile-time safety
4. **Real-Time**: Monitor values before entity save
5. **Easy Access**: Helper methods eliminate component lookup boilerplate
6. **Backward Compatible**: Old code continues to work unchanged
