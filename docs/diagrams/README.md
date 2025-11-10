# Gantt Chart Architecture Diagrams

This directory contains PlantUML diagrams and generated images documenting the Gantt chart implementation architecture.

## Diagrams

### 1. Call Hierarchy - Selection and Database Fetch
**File**: `gantt-call-hierarchy.puml`  
**Image**: `Gantt Call Hierarchy - Selection and Database Fetch.png`

Shows the complete call flow from user clicking a Gantt item to displaying the detail form:
- User interaction phase
- Selection handling phase
- **Database fetch phase** (the key pattern)
- Dynamic screen building phase

This diagram illustrates how clicking an item triggers a fresh database fetch and dynamic form creation.

### 2. Component Structure
**File**: `gantt-component-structure.puml`  
**Image**: `Gantt Component Structure.png`

Displays the class structure and relationships:
- Presentation Layer (Views)
- Component Layer (Grid, Header, Bars)
- Data Layer (Items, Provider)
- Service Layer (Activity/Meeting services)
- Base Classes

Shows inheritance, composition, and dependency relationships between all Gantt components.

### 3. Data Flow
**File**: `gantt-data-flow.puml`  
**Image**: `Gantt Data Flow.png`

Documents the data flow patterns:
- Initial load flow
- Data loading (first fetch from database)
- Project change flow

Illustrates how data moves from database through services to display components.

### 4. UI Regions
**File**: `gantt-ui-regions.puml`  
**Image**: `Gantt UI Regions.png`

Maps out the UI component regions:
- Timeline Controls Region (zoom, pan, scale)
- Timeline Header Region (year/month/week markers)
- Grid Data Region (entity columns and timeline bars)
- Detail Form Region (dynamic form builder output)

Shows the visual layout and component relationships.

## Generating Diagrams

To regenerate the PNG images from PlantUML source files:

```bash
cd docs/diagrams
plantuml -tpng *.puml
```

Requirements:
- PlantUML installed (`apt-get install plantuml`)
- Graphviz installed (`apt-get install graphviz`)

## Using in Documentation

Reference these diagrams in documentation using:

```markdown
![Call Hierarchy](../diagrams/Gantt%20Call%20Hierarchy%20-%20Selection%20and%20Database%20Fetch.png)
```

Or use the PlantUML files directly in tools that support PlantUML rendering.

## Diagram Format

All diagrams are created using PlantUML syntax:
- Sequence diagrams for call flows
- Class diagrams for structure
- Component diagrams for UI layout

PlantUML provides:
- Clean, maintainable diagram-as-code
- Easy version control
- Automatic layout
- Professional appearance
- Easy to update

## Related Documentation

- `../implementation/GANTT_DESIGN_PATTERN.md` - Comprehensive design pattern documentation
- `../implementation/GANTT-TIMELINE-COMPLETE-SUMMARY.md` - Implementation summary
- `../implementation/gantt-timeline-header.md` - Timeline header guide
- `../implementation/gantt-timeline-visual-guide.md` - Visual design guide
