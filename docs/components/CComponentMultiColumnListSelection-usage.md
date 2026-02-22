# CComponentMultiColumnListSelection - Usage Guide

## Overview
`CComponentMultiColumnListSelection` is a grid-based multi-select component for structured rows (record model). It is designed for cases where users need to choose values while seeing supporting columns (type, source, description, etc.).

## When to Use
Use this component when:
- Your field persists a simple list (for example `List<String>`)
- Users need extra context columns before selecting
- One column should be the persisted value, while other columns are informational

Do not use this component when:
- You need ordered selection (use `CComponentFieldSelection`)
- A single text column is enough (use `CComponentListSelection`)

## Data Model
Rows are represented by:
- `CMultiColumnStringRow(icon, iconColor, columnValues)`

Columns are represented by:
- `CColumnDefinition(id, header)`

The persisted value column is configured by:
- `setReturnedColumnId("columnId")`

## CFormBuilder Pattern (Auto Selection)
For `@AMetaData(useGridSelection = true)` fields, `CFormBuilder` now selects this component when the data provider returns `List<CMultiColumnStringRow>`.

Binding pattern:
- UI value: `List<CMultiColumnStringRow>`
- Entity field value: `List<String>`
- Converter maps selected rows to the configured return column.

## Example
```java
@AMetaData(
    displayName = "Protocol Variables",
    useGridSelection = true,
    dataProviderBean = "pageservice",
    dataProviderMethod = "getComboValuesOfProtocolVariableNames",
    dataProviderParamBean = "context",
    dataProviderParamMethod = "getValue"
)
private List<String> protocolVariableNames = new ArrayList<>();
```

Provider output example:
```java
return List.of(new CMultiColumnStringRow(
    "vaadin:code",
    "#1C88FF",
    Map.of(
        "protocolVariableName", "EngineSpeed",
        "variableType", "UBYTE",
        "recordType", "UBYTE"
    )
));
```

## Selection Matrix Update
Use this rule when choosing list/set relation components:
- `CComponentMultiColumnListSelection`: choose when selection requires multiple descriptive columns but persistence should store one specific value column.
