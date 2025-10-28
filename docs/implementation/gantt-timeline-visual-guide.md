# Gantt Timeline Header - Visual Design Guide

## Component Structure

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         GANTT CHART GRID                                     │
├──────┬────────┬────────┬─────────┬─────────┬───────────┬──────────────────────┤
│ Type │ Title  │ Resp.  │ Start   │ End     │ Duration  │ Timeline             │
├──────┼────────┼────────┼─────────┼─────────┼───────────┼──────────────────────┤
│      │        │        │         │         │           │  ┌─ TIMELINE HEADER ─┐│
│      │        │        │         │         │           │  │ Jan │ Feb │ Mar │  ││
│      │        │        │         │         │           │  │2024 │     │     │  ││
│      │        │        │         │         │           │  └───────────────────┘│
├──────┼────────┼────────┼─────────┼─────────┼───────────┼──────────────────────┤
│ Act  │ Task 1 │ Admin  │ 1/1/24  │ 1/31/24 │ 31 days   │ [████████░░░░░░░░]   │
├──────┼────────┼────────┼─────────┼─────────┼───────────┼──────────────────────┤
│ Act  │ Task 2 │ User1  │ 2/1/24  │ 3/15/24 │ 44 days   │ ░░░░░░[███████████]  │
└──────┴────────┴────────┴─────────┴─────────┴───────────┴──────────────────────┘
```

## Timeline Header Components

### Year Marker (Long Duration: > 365 days)
```
┌──────────────────────────────────────────────────────────────────┐
│              2023              │              2024              │
│   Jan│Feb│Mar│...│Dec         │   Jan│Feb│Mar│...│Dec         │
└──────────────────────────────────────────────────────────────────┘
```
**Features:**
- Year boundaries highlighted in blue (#007bff)
- Thicker border separating years (2px solid)
- Background: rgba(0, 123, 255, 0.08)

### Month Marker (Medium Duration: 90-365 days)
```
┌──────────────────────────────────────────────────────────────────┐
│  Jan  │  Feb  │  Mar  │  Apr  │  May  │  Jun  │  Jul  │  Aug  │
│ 2024  │       │       │       │       │       │       │       │
└──────────────────────────────────────────────────────────────────┘
```
**Features:**
- January shows "Jan 2024" (month + year)
- Other months show "Feb", "Mar", etc.
- Year boundaries get special highlighting
- Each month division is proportional to its actual days

### Week Marker (Short Duration: < 90 days)
```
┌──────────────────────────────────────────────────────────────────┐
│  W1  │  W2  │  W3  │  W4  │  W5  │  W6  │  W7  │  W8  │  W9  │
└──────────────────────────────────────────────────────────────────┘
```
**Features:**
- Weeks numbered sequentially (W1, W2, ...)
- Lighter background color
- Dashed borders between weeks
- Starts from first Monday in range

## Task Bar Styling

### Standard Task Bar
```
    ┌─[════════════════════════════════════]─┐
    │ │  Task Name (Owner) - 45%             │ │
    │ └──────────────────────────────────────┘ │
    └────────────────────────────────────────────┘
    ↑                                          ↑
  Start                                      End
  Marker                                   Marker
```

### Task Bar with Progress
```
    ┌─[█████████████░░░░░░░░░░░░░░░░░░░░]─┐
    │ │░░░░░░░░░░░░░░ Task (Owner) - 45% │ │
    │ └──────────────────────────────────┘ │
    └────────────────────────────────────────┘
       ↑─────────────↑
       Progress     Remaining
       Overlay      Work
```

### Task Bar States

**Normal State:**
- Height: 32px
- Border-radius: 6px
- Box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1)
- Border: 2px solid rgba(255, 255, 255, 0.3)

**Hover State:**
- Transform: translateY(-2px)
- Box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25)
- Filter: brightness(1.15)
- Border-color: rgba(255, 255, 255, 0.6)
- Animation: pulse effect

**Active/Click State:**
- Transform: translateY(0)
- Box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15)

## CSS Class Hierarchy

```
.gantt-timeline-header                   (Timeline header container)
├── .gantt-timeline-marker               (Base marker style)
│   ├── .gantt-timeline-year-marker      (Year boundaries)
│   ├── .gantt-timeline-month-marker     (Monthly divisions - implicit)
│   └── .gantt-timeline-week-marker      (Weekly divisions)
│       └── .gantt-timeline-label        (Text labels)
│
.gantt-timeline-bar-container            (Task bar container)
└── .gantt-timeline-bar                  (Individual task bar)
    ├── .gantt-bar-start-marker          (Start indicator)
    ├── .gantt-bar-end-marker            (End indicator)
    ├── .gantt-progress-overlay          (Progress indicator)
    ├── .gantt-task-info                 (Task text)
    └── ::before / ::after               (Visual highlights)
```

## Color Scheme

### Timeline Header
- Background: Linear gradient (#f8f9fa → #e9ecef)
- Border: #dee2e6 (2px solid)
- Normal markers: #dee2e6 (1px solid)
- Year markers: #007bff (2px solid, blue background)
- Week markers: #adb5bd (1px dashed)

### Task Bars
The task bars use entity-specific colors from `CGanttItem.getColorCode()`:
- Activities: Entity-specific color
- Meetings: Entity-specific color
- Other entities: Entity-specific color or fallback #6c757d (gray)

**Overlays:**
- Progress: rgba(255, 255, 255, 0.2)
- Start marker: rgba(0, 0, 0, 0.3) with white border
- End marker: rgba(0, 0, 0, 0.3) with white border
- Internal highlights: rgba(255, 255, 255, 0.4) and rgba(0, 0, 0, 0.25)

## Responsive Behavior

### Narrow Bars (< 2% width)
```
[▓]  ← Label hidden, only colored bar visible
```

### Medium Bars (2-10% width)
```
[▓▓▓▓▓▓] Task...  ← Text truncated with ellipsis
```

### Wide Bars (> 10% width)
```
[▓▓▓▓▓▓▓▓▓▓▓▓▓▓] Task Name (Owner) - 45%  ← Full text
```

## Animations

### Bar Appearance (slideIn)
```
Duration: 0.4s
Easing: ease-out
Effect: Scale from 0 to 100% width (left-to-right)
```

### Hover Effect (pulse)
```
Duration: 1.5s
Easing: ease-in-out
Repeat: infinite
Effect: Opacity pulsing between 1.0 and 0.8
```

### Hover Transform
```
Duration: 0.3s
Easing: ease
Effect: Lift up by 2px with enhanced shadow
```

## Grid Integration

### Column Configuration
```java
Timeline Column:
- Width: 450px
- FlexGrow: 1 (expands to fill available space)
- Sortable: false
- Header: CGanttTimelineHeader component
- Renderer: ComponentRenderer<CGanttTimelineBar>
```

### Overflow Handling
```css
/* Allow timeline elements to overflow cell boundaries */
vaadin-grid-cell-content:has(.gantt-timeline-header),
vaadin-grid-cell-content:has(.gantt-timeline-bar-container) {
    overflow: visible !important;
}

/* Remove padding from header cell for perfect alignment */
vaadin-grid-cell-content:has(.gantt-timeline-header) {
    padding: 0 !important;
}
```

## Synchronization Formula

Both header and bars use the same calculation:

```java
// Calculate total timeline duration
long totalDays = ChronoUnit.DAYS.between(timelineStart, timelineEnd);

// For timeline markers
long markerStart = ChronoUnit.DAYS.between(timelineStart, markerDate);
double leftPercent = (markerStart * 100.0) / totalDays;
double widthPercent = (markerDuration * 100.0) / totalDays;

// For task bars  
long taskStart = ChronoUnit.DAYS.between(timelineStart, taskStartDate);
long taskDuration = ChronoUnit.DAYS.between(taskStartDate, taskEndDate) + 1;
double leftPercent = (taskStart * 100.0) / totalDays;
double widthPercent = (taskDuration * 100.0) / totalDays;

// Apply positioning
element.getStyle().set("left", String.format("%.2f%%", leftPercent));
element.getStyle().set("width", String.format("%.2f%%", widthPercent));
```

This ensures perfect alignment between timeline markers and task bars.

## Example Timeline Scenarios

### Scenario 1: 3-Month Project
```
Timeline: Jan 1, 2024 - Mar 31, 2024 (90 days)
Scale: Monthly markers
┌────────────────────────────────────────────────────────────┐
│     Jan 2024     │      Feb       │       Mar       │
│  (31 days/34%)  │   (29 days/32%)│   (31 days/34%)│
└────────────────────────────────────────────────────────────┘

Task 1: Jan 15 - Feb 15 (32 days)
Position: Starts at 14/90 = 15.6%, Width: 32/90 = 35.6%
[░░░░░░░░░░░░░░[███████████████████████]░░░░░░░░░░░░░]
```

### Scenario 2: 6-Week Sprint
```
Timeline: Apr 1, 2024 - May 12, 2024 (42 days)
Scale: Weekly markers
┌────────────────────────────────────────────────────────────┐
│  W1 │  W2 │  W3 │  W4 │  W5 │  W6 │
└────────────────────────────────────────────────────────────┘

Sprint Task: Week 2-5 (28 days)
Position: Starts at 7/42 = 16.7%, Width: 28/42 = 66.7%
[░░░░░░[███████████████████████████████████████]░░░░░]
```

### Scenario 3: Multi-Year Project
```
Timeline: Jan 1, 2023 - Dec 31, 2024 (730 days)
Scale: Yearly markers with months
┌────────────────────────────────────────────────────────────┐
│                  2023                │         2024         │
│ Jan│Feb│Mar│Apr│May│Jun│Jul│Aug│Sep│Oct│Nov│Dec│Jan│...   │
└────────────────────────────────────────────────────────────┘

Phase 1: Q1 2023 (90 days)
Position: Starts at 0%, Width: 90/730 = 12.3%
[██████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░]

Phase 2: Q3-Q4 2024 (184 days)
Position: Starts at 546/730 = 74.8%, Width: 184/730 = 25.2%
[░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██████████████]
```

## Accessibility Features

- **Tooltips**: All task bars have detailed tooltips with task info
- **Labels**: Timeline markers have readable text labels
- **Contrast**: High contrast between text and backgrounds
- **Hover States**: Clear visual feedback for interactive elements
- **No Reliance on Color**: Shape and position convey information

## Performance Optimization

- **CSS Transforms**: Used for animations (GPU-accelerated)
- **Component Reuse**: ComponentRenderer creates bars on-demand
- **Calculation Caching**: Timeline range calculated once
- **Efficient Rendering**: Only visible portions of timeline rendered
- **No JavaScript**: Pure CSS animations and transitions
