# Gantt Chart Design Pattern

## Overview

The Gantt chart implementation in Derbent follows a sophisticated design pattern that dynamically fetches and displays project items (Activities and Meetings) in a timeline view. When users click on Gantt items, their full details are fetched from the database in real-time and displayed using dynamic screen building.

## Architecture Pattern

### Layer Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  (CProjectGanttView, CMasterViewSectionGannt)           │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────────┐
│                   Component Layer                        │
│  (CGanttGrid, CGanttTimelineHeader, CGanttTimelineBar)  │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────────┐
│                    Data Layer                            │
│          (CGanttDataProvider, CGanttItem)                │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────────┐
│                   Service Layer                          │
│      (CActivityService, CMeetingService)                 │
└─────────────────────────────────────────────────────────┘
```

![Component Structure Diagram](../diagrams/Gantt%20Component%20Structure.png)

*Full component structure diagram showing all classes, relationships, and key patterns.*

## Core Components

### 1. CGanttItem - Data Transfer Object

**Purpose**: Wraps project items (CActivity, CMeeting) to provide a unified interface for Gantt chart display.

**Key Responsibilities**:
- Wraps different entity types into a common interface
- Provides timeline-related properties (start date, end date, duration)
- Manages hierarchy levels for nested display
- **Fetches fresh entity data from database on demand**

**Key Methods**:
```java
public CProjectItem<?> getGanntItem(
    CEntityOfProjectService<?> activityService, 
    CEntityOfProjectService<?> meetingService
)
```
This method is critical for the dynamic fetch pattern. When a Gantt item is selected, it:
1. Identifies the entity type (Activity or Meeting)
2. Selects the appropriate service
3. Fetches the complete entity from database using `getById()`
4. Returns the fully populated entity with all relationships

### 2. CGanttGrid - Timeline Grid Component

**Purpose**: Displays Gantt items in a grid with visual timeline bars.

**Key Features**:
- Hierarchical item display with indentation
- Timeline visualization with color-coded bars
- Interactive timeline header with zoom/scroll controls
- Responsive column layout

**Columns**:
- ID, Type, Title (with hierarchy indentation)
- Responsible user, Start/End dates, Duration
- Description
- Visual timeline bar (synchronized with header)

### 3. CGanttTimelineHeader - Interactive Timeline Control

**Purpose**: Provides interactive timeline controls and synchronized time markers.

**Key Features**:
- Zoom in/out controls
- Pan left/right navigation
- Adjustable timeline width
- Adaptive scale (Years, Quarters, Months, Weeks)
- "Today" marker indicator
- Timeline range display

**Scale Adaptation**:
- Auto mode selects appropriate scale based on date range
- Manual scale selection available
- Multiple layers can be displayed simultaneously

### 4. CGanttTimelineBar - Visual Task Bar

**Purpose**: Renders visual timeline bars for each task.

**Key Features**:
- Color-coded by entity type
- Progress indicator overlay
- Responsive to timeline range
- Tooltips with full task information
- Text labels (name, responsible, progress)

### 5. CGanttDataProvider - Data Source

**Purpose**: Combines activities and meetings into a unified data stream.

**Key Responsibilities**:
- Fetches activities and meetings for current project
- Wraps entities in CGanttItem DTOs
- Assigns unique IDs to prevent collisions
- Sorts by timeline (start date → end date)
- Caches data until refresh

**Data Flow**:
```
Project → [Activities + Meetings] → CGanttItem[] → Grid Display
```

## The Dynamic Fetch Pattern

### Selection and Database Fetch Flow

This is the core pattern that makes the Gantt implementation work "as expected" - clicking items triggers real-time database fetch:

```
User Click → Grid Selection Event → onSelectionChanged() → 
updateDetailsComponent() → getGanntItem() → Database Fetch → 
Build Dynamic Screen → Display Details
```

### Detailed Call Hierarchy

![Call Hierarchy Diagram](../diagrams/Gantt%20Call%20Hierarchy%20-%20Selection%20and%20Database%20Fetch.png)

*Complete sequence diagram showing the selection and database fetch flow.*

#### 1. User Interaction Phase
```
User clicks on Gantt item in grid
    ↓
CGanttGrid fires selection change event
    ↓
CMasterViewSectionGannt.onSelectionChange(ValueChangeEvent event)
    ↓
Fires SelectionChangeEvent to parent
```

#### 2. Selection Handling Phase
```
CGridViewBaseGannt.onSelectionChanged(SelectionChangeEvent event)
    ↓
Validates event.getSelectedItem() is CGanttItem
    ↓
setCurrentEntity(selectedItem)
    ↓
populateForm()
    ↓
updateDetailsComponent()
```

#### 3. Database Fetch Phase (THE KEY PATTERN)
```
updateDetailsComponent()
    ↓
Cast getCurrentEntity() to CGanttItem
    ↓
CGanttItem.getGanntItem(activityService, meetingService)
    ↓
    ├─→ If CActivity: activityService.getById(entityId)
    │       ↓
    │   Database query: SELECT * FROM activities WHERE id = ?
    │       ↓
    │   Returns fully populated CActivity
    │
    └─→ If CMeeting: meetingService.getById(entityId)
            ↓
        Database query: SELECT * FROM meetings WHERE id = ?
            ↓
        Returns fully populated CMeeting
```

#### 4. Dynamic Screen Building Phase
```
Fresh entity returned from database
    ↓
Create CEnhancedBinder<CProjectItem<?>>
    ↓
Get entity VIEW_NAME via reflection
    ↓
buildScreen(entityViewName, entityBinder)
    ↓
Dynamically construct form fields based on entity type
    ↓
entityBinder.readBean(ganttEntity)
    ↓
Display populated form in details section
```

### Why This Pattern?

The dynamic fetch pattern is necessary because:

1. **Memory Efficiency**: CGanttItem is a lightweight DTO. It only contains essential timeline data, not full entity graphs.

2. **Freshness**: When user clicks an item, we fetch the latest data from the database, ensuring no stale data is displayed.

3. **Lazy Loading**: Full entity relationships (assignees, dependencies, files, etc.) are only loaded when needed.

4. **Polymorphic Display**: Different entity types (Activity vs Meeting) require different detail forms. The pattern handles this dynamically.

5. **Performance**: Grid loads quickly with lightweight DTOs. Full entity data is only fetched for the selected item.

## View Hierarchy

### CProjectGanntView
- **Route**: `/cprojectganntview`
- **Menu**: "Project.Project Gannt Chart"
- **Base Class**: `CGridViewBaseGannt<CGanttViewEntity>`

**Responsibilities**:
- Provides route and navigation entry
- Manages current entity state
- Delegates to master/detail sections

### CGridViewBaseGannt
- **Type**: Abstract base class for Gantt views
- **Generic**: `<EntityClass extends CEntityOfProject<EntityClass>>`

**Key Methods**:
```java
protected void createMasterComponent() {
    // Creates CMasterViewSectionGannt with services
}

protected void onSelectionChanged(SelectionChangeEvent event) {
    // Handles CGanttItem selection
    // Triggers database fetch and form population
}

protected void updateDetailsComponent() {
    // Fetches fresh entity from database
    // Builds dynamic detail screen
}
```

### CMasterViewSectionGannt
- **Type**: Master view section for Gantt display
- **Implements**: `IProjectChangeListener`

**Responsibilities**:
- Creates and manages CGanttGrid
- Listens for grid selection events
- Responds to project changes
- Refreshes view when project changes

## UI Component Regions

![UI Regions Diagram](../diagrams/Gantt%20UI%20Regions.png)

*UI component layout showing all regions and their relationships.*

### Main Layout Structure

```
┌─────────────────────────────────────────────────────────────┐
│                      CProjectGanntView                       │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         Master Section (CMasterViewSectionGannt)      │  │
│  │  ┌─────────────────────────────────────────────────┐ │  │
│  │  │           CGanttGrid (Timeline Grid)            │ │  │
│  │  │  ┌──────────────────────────────────────────┐   │ │  │
│  │  │  │    CGanttTimelineHeader (Controls)       │   │ │  │
│  │  │  │  [◄] [►] [+] [-] [⟲] [⊕] Scale: [Auto]  │   │ │  │
│  │  │  │  2024-01-01 → 2024-12-31                 │   │ │  │
│  │  │  └──────────────────────────────────────────┘   │ │  │
│  │  │  ┌──────────────────────────────────────────┐   │ │  │
│  │  │  │    Timeline Header (Year/Month/Week)     │   │ │  │
│  │  │  │  [Jan] [Feb] [Mar] [Apr] [May] [Jun]...  │   │ │  │
│  │  │  │  [1][2][3][4][5][6][7][8][9][10][11]...  │   │ │  │
│  │  │  └──────────────────────────────────────────┘   │ │  │
│  │  │  ┌──────────────────────────────────────────┐   │ │  │
│  │  │  │         Grid Rows (Gantt Items)          │   │ │  │
│  │  │  │  ID│Type │Title      │[━━━━━bar━━━━━]   │   │ │  │
│  │  │  │  1 │Act. │Task 1     │  [████████░░]   │   │ │  │
│  │  │  │  2 │Meet │Meeting 1  │    [███████]     │   │ │  │
│  │  │  │  3 │Act. │  Sub-task │      [████]       │   │ │  │
│  │  │  └──────────────────────────────────────────┘   │ │  │
│  │  └─────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │       Detail Section (Dynamic Form Builder)           │  │
│  │  ┌─────────────────────────────────────────────────┐ │  │
│  │  │  Activity: Fix Database Bug                     │ │  │
│  │  │  ┌──────────────────────────────────────────┐   │ │  │
│  │  │  │  Name: [Fix Database Bug            ]    │   │ │  │
│  │  │  │  Description: [......................]   │   │ │  │
│  │  │  │  Start Date: [2024-01-15]                │   │ │  │
│  │  │  │  Due Date: [2024-01-30]                  │   │ │  │
│  │  │  │  Responsible: [John Doe ▼]               │   │ │  │
│  │  │  │  Status: [In Progress ▼]                 │   │ │  │
│  │  │  │  Priority: [High ▼]                      │   │ │  │
│  │  │  │  Progress: [75%]                         │   │ │  │
│  │  │  │                                           │   │ │  │
│  │  │  │  [Save] [Delete] [Cancel]                │   │ │  │
│  │  │  └──────────────────────────────────────────┘   │ │  │
│  │  └─────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Region Breakdown

#### 1. Timeline Controls Region
- **Component**: CGanttTimelineHeader control bar
- **Location**: Top of grid
- **Contains**:
  - Navigation buttons (scroll left/right)
  - Zoom controls (zoom in/out)
  - Reset button
  - Focus to middle button
  - Width adjustment buttons
  - Scale selector dropdown
  - Date range display

#### 2. Timeline Header Region
- **Component**: CGanttTimelineHeader timeline markers
- **Location**: Below controls, above grid rows
- **Contains**:
  - Year markers (when applicable)
  - Quarter markers (when applicable)
  - Month markers (when applicable)
  - Week markers (when applicable)
  - "Today" indicator line

#### 3. Grid Data Region
- **Component**: CGanttGrid rows
- **Location**: Main content area
- **Contains**:
  - Entity ID column
  - Entity type column
  - Title column (with hierarchy indentation)
  - Responsible user column
  - Start/End date columns
  - Duration column
  - Description column
  - Visual timeline bar column (synchronized with header)

#### 4. Detail Form Region
- **Component**: Dynamic form builder output
- **Location**: Below/beside grid (depends on layout)
- **Contains**:
  - Dynamically built form fields based on entity type
  - Bound to fresh database entity
  - Action buttons (Save, Delete, Cancel)

## Data Flow Patterns

![Data Flow Diagram](../diagrams/Gantt%20Data%20Flow.png)

*Data flow showing initial load and project change patterns.*

### Initial Load Flow
```
User navigates to Gantt view
    ↓
CProjectGanntView constructor
    ↓
createMasterComponent()
    ↓
new CMasterViewSectionGannt()
    ↓
createMasterView()
    ↓
new CGanttGrid(project, services...)
    ↓
CGanttDataProvider initialization
    ↓
loadItems()
    ↓
    ├─→ activityService.listByProject(project)
    │       ↓
    │   Database: SELECT * FROM activities WHERE project_id = ?
    │
    └─→ meetingService.listByProject(project)
            ↓
        Database: SELECT * FROM meetings WHERE project_id = ?
    ↓
Wrap entities in CGanttItem DTOs
    ↓
Sort by timeline
    ↓
Display in grid
```

### Selection and Fetch Flow
```
User clicks Gantt item
    ↓
Grid selection event
    ↓
onSelectionChanged()
    ↓
setCurrentEntity(ganttItem)
    ↓
populateForm()
    ↓
updateDetailsComponent()
    ↓
ganttItem.getGanntItem(services...)
    ↓
service.getById(entityId)
    ↓
Database query for full entity
    ↓
Build dynamic form with fresh data
    ↓
Display in detail section
```

### Project Change Flow
```
User changes active project
    ↓
SessionService fires project change event
    ↓
CMasterViewSectionGannt.onProjectChanged()
    ↓
refreshMasterView()
    ↓
CGanttDataProvider.refreshAll()
    ↓
Clear cache
    ↓
Reload items for new project
    ↓
Update grid display
```

### Timeline Interaction Flow
```
User clicks zoom/pan button
    ↓
CGanttTimelineHeader control handler
    ↓
Calculate new timeline range
    ↓
on_actioAapplyRange(newStart, newEnd)
    ↓
Update timeline markers
    ↓
Fire timeline range change event
    ↓
CGanttGrid updates timeline
    ↓
CGanttDataProvider.refreshAll()
    ↓
Re-render all timeline bars
```

## CSS Styling Classes

### Timeline Header Classes
- `.gantt-timeline-header` - Main header container
- `.gantt-timeline-controls` - Control bar
- `.gantt-timeline-control-button` - Individual control buttons
- `.gantt-timeline-scale-select` - Scale selection dropdown
- `.gantt-timeline-summary` - Date range display
- `.gantt-timeline-wrapper` - Timeline marker wrapper
- `.gantt-timeline-row` - Individual timeline row
- `.gantt-timeline-row-year` - Year marker row
- `.gantt-timeline-row-quarter` - Quarter marker row
- `.gantt-timeline-row-month` - Month marker row
- `.gantt-timeline-row-week` - Week marker row
- `.gantt-timeline-marker` - Individual time marker
- `.gantt-timeline-today-marker` - "Today" indicator line

### Timeline Bar Classes
- `.gantt-bar` - Base timeline bar
- `.gantt-bar-start-marker` - Start position marker
- `.gantt-bar-end-marker` - End position marker
- `.gantt-bar-progress` - Progress indicator overlay

## Color Scheme

### Entity Colors
Each entity type has a distinct color for visual identification:
- **Activities**: Color defined in CActivity.DEFAULT_COLOR
- **Meetings**: Color defined in CMeeting.DEFAULT_COLOR
- Timeline bars automatically use these colors via `CGanttItem.getColorCode()`

### Timeline Header Colors
- **Year markers**: Blue (#007bff)
- **Quarter markers**: Light blue
- **Month markers**: Neutral gray
- **Week markers**: Very light gray
- **Today marker**: Red (--lumo-error-color)

### Hover Effects
- Timeline bars: Brightness increase + shadow
- Control buttons: Background color change
- Timeline markers: Background highlight

## Performance Considerations

### Caching Strategy
- **CGanttDataProvider**: Caches full item list until refresh
- **Timeline Range**: Calculated once, reused for all bars
- **Date-to-Pixel Map**: Pre-calculated for efficient rendering

### Lazy Loading
- Grid items: Lightweight CGanttItem DTOs
- Full entities: Only loaded on selection
- Entity relationships: Loaded via JPA when needed

### Rendering Optimization
- Component renderer: Reuses bar components
- CSS transforms: GPU-accelerated animations
- Conditional rendering: Only visible timeline layers

## Extension Points

### Adding New Entity Types
To add support for new entity types in Gantt:

1. Ensure entity implements `CProjectItem<?>` or extends it
2. Add entity service to `CGridViewBaseGannt` constructor
3. Update `CGanttItem.getGanntItem()` to handle new type:
```java
} else if (selectedItem instanceof CNewEntity) {
    service = newEntityService;
}
```
4. Update `CGanttDataProvider.loadItems()` to fetch new entities:
```java
List<CNewEntity> newEntities = newEntityService.listByProject(project);
for (CNewEntity e : newEntities) {
    items.add(new CGanttItem(e, idCounter.incrementAndGet()));
}
```

### Customizing Timeline Display
To customize timeline appearance:

1. Modify `CGanttTimelineHeader.renderTimelineLayers()` for different scales
2. Update CSS in `gantt.css` for visual styling
3. Adjust `CGanttTimelineBar` component for bar appearance

### Adding Timeline Features
To add new timeline features:

1. Add control button to `CGanttTimelineHeader.configureControlBar()`
2. Implement control handler method
3. Update timeline state and trigger re-render
4. Fire change event if grid needs update

## Testing Strategy

### Unit Testing
- **CGanttItem**: Test DTO wrapping and entity identification
- **CGanttDataProvider**: Test data combination and sorting
- **Timeline Calculations**: Test date-to-pixel conversions

### Integration Testing
- **Selection Flow**: Test click → fetch → display flow
- **Project Change**: Test view refresh on project change
- **Timeline Controls**: Test zoom/pan functionality

### Manual Testing
- **Visual Alignment**: Verify timeline bars align with header
- **Entity Types**: Test both Activities and Meetings
- **Edge Cases**: Test empty projects, single items, large date ranges

## Common Pitfalls and Solutions

### Problem: Timeline Bars Not Aligned
**Cause**: Inconsistent width calculations between header and bars
**Solution**: Ensure both use same `totalWidth` and date range

### Problem: Selection Not Working
**Cause**: CGanttItem not properly cast or service not available
**Solution**: Verify `onSelectionChanged` properly validates type

### Problem: Stale Data in Details
**Cause**: Not fetching fresh entity from database
**Solution**: Always use `getGanntItem()` to fetch current data

### Problem: Entity Type Not Supported
**Cause**: Missing entity type check in `getGanntItem()`
**Solution**: Add new entity type handling in the method

## Best Practices

1. **Always Fetch Fresh Data**: Use `getGanntItem()` for selection
2. **Type Safety**: Use `Check.instanceOf()` for type validation
3. **Service Injection**: Pass all required services to constructors
4. **Event Handling**: Use proper event listeners and bubbling
5. **CSS Organization**: Keep Gantt styles in dedicated `gantt.css`
6. **Logging**: Use SLF4J logger for debugging
7. **Error Handling**: Catch and display exceptions to users
8. **Null Safety**: Check for null entities before operations

## Future Enhancements

### Planned Features
1. Drag-and-drop task rescheduling
2. Dependency lines between tasks
3. Critical path visualization
4. Resource allocation display
5. Baseline comparison
6. Export to PDF/image

### Architectural Improvements
1. Add command pattern for undo/redo
2. Implement observer pattern for cross-component updates
3. Add strategy pattern for different timeline scales
4. Create factory for entity-specific detail builders

## References

### Related Documentation
- `GANTT-TIMELINE-COMPLETE-SUMMARY.md` - Implementation summary
- `gantt-timeline-header.md` - Timeline header technical guide
- `gantt-timeline-visual-guide.md` - Visual design guide
- `coding-standards.md` - Project coding standards

### External Resources
- [Vaadin Grid Documentation](https://vaadin.com/docs/latest/components/grid)
- [Gantt Chart Principles](https://en.wikipedia.org/wiki/Gantt_chart)
- [Master-Detail Pattern](https://vaadin.com/docs/latest/application/master-detail)

## Conclusion

The Gantt chart implementation follows a robust design pattern that combines:
- Lightweight DTOs for grid display
- Dynamic database fetching on selection
- Real-time detail form building
- Interactive timeline controls
- Responsive visual components

This pattern ensures efficient memory usage, data freshness, and extensibility while providing a professional Gantt chart experience for project management.
