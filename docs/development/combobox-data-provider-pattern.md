# DataProvider Method Naming Pattern

## Purpose

Define one enforced naming standard for all methods referenced by `@AMetaData(dataProviderBean, dataProviderMethod)`.

## Naming Convention

Use exactly one of these method prefixes based on purpose:

- `getComboValuesOf{FieldConcept}`
- `buildDataProviderComponent{ComponentConcept}`
- `getCalculatedValueOf{MetricConcept}`
- `getDataProviderValuesOf{ScopeConcept}`

### Prefix Meanings

- `getComboValuesOf...`: bounded lists for ComboBox/selection fields.
- `buildDataProviderComponent...`: dynamic component providers (widgets/components).
- `getCalculatedValueOf...`: calculated values used with `autoCalculate = true`.
- `getDataProviderValuesOf...`: non-combobox list providers scoped by context (for example current user).

## Combo Supplier Rules

Examples:

- `getComboValuesOfProtocolType()`
- `getComboValuesOfSeverityLevel()`
- `getComboValuesOfBaudRate()`
- `getComboValuesOfServerPort()`
- `getComboValuesOfTimeoutSeconds()`

## Return Type Rules

Use plain values for obvious bounded domains:

- `List<Integer>` for numeric choices (`baudRate`, `bitrate`, `dataBit`, `stopBit`, ports, timeouts)
- `List<String>` for simple obvious text choices

Use rich options only when visual semantics are needed (icon/color):

- `List<CComboBoxOption>` for meaningful categorical values (for example protocol classes, syslog severity/facility)

`CComboBoxOption` fields:

- `name` (display text)
- `value` (persisted string)
- `color` (icon color)
- `icon` (vaadin icon name)

## UI Behavior Rule

For option-based string ComboBoxes:

- Keep default Vaadin backgrounds.
- Apply option color only to icons.
- Do not introduce custom background painting unless explicitly requested.

## How To Implement

1. Choose prefix by purpose from this document.
2. In domain metadata, set `dataProviderMethod` to a method using the correct prefix.
3. Implement that method in the target bean (`pageservice`, `context`, concrete service bean, etc.).
4. Keep provider values bounded and deterministic (`List.of(...)`) for constrained domains.
5. Keep metadata field type and provider return type compatible.

## Example: Simple Numeric Supplier

```java
public List<Integer> getComboValuesOfBaudRate() {
    return List.of(1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200);
}
```

## Example: Rich Option Supplier

```java
public List<CComboBoxOption> getComboValuesOfSeverityLevel() {
    return List.of(
        new CComboBoxOption("DEBUG", "#78909C", "vaadin:bug"),
        new CComboBoxOption("ERROR", "#E53935", "vaadin:close-circle")
    );
}
```

## Example: Calculated Value Supplier

```java
public static Integer getCalculatedValueOfItemCount(final CSprint sprint) {
    return sprint.getCalculatedValueOfItemCount();
}
```

## Example: Component Provider

```java
public CComponentWidgetEntity<CActivity> buildDataProviderComponentWidget(final CActivity entity) {
    return new CComponentWidgetActivity(entity);
}
```

## Enforcement Checklist

- No new `dataProviderMethod` names outside the four prefixes above.
- For bounded choices, always use `getComboValuesOf...`.
- Do not use generic names like `getAvailable...`, `list...`, `getAll...` for new dataProvider methods.
- If refactoring old code, migrate legacy names to this convention.

## BAB Scope

BAB ComboBox provider methods should follow this document for:

- Node configuration page services
- Policy Action / Filter / Trigger page services
- Other bounded selection fields in BAB profile
