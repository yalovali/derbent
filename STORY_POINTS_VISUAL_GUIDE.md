# Story Points Feature - Visual Guide

## Before and After Comparison

### Sprint Widget Display - BEFORE
```
┌──────────────────────────────────────┐
│ 📅 Sprint Planning Sprint            │
│ Type: Development Sprint              │
│                                       │
│ 📋 5 items                           │  ← Only shows count
│                                       │
│ Status: In Progress                   │
│ Dates: 2024-01-01 to 2024-01-15     │
└──────────────────────────────────────┘
```

### Sprint Widget Display - AFTER
```
┌──────────────────────────────────────┐
│ 📅 Sprint Planning Sprint            │
│ Type: Development Sprint              │
│                                       │
│ 📋 5 items (21 SP)                   │  ← Now shows total story points!
│                                       │
│ Status: In Progress                   │
│ Dates: 2024-01-01 to 2024-01-15     │
└──────────────────────────────────────┘
```

## Sprint Items Grid - BEFORE
```
┌────┬──────────┬──────────────────────────────┬────────────┐
│ ID │ Type     │ Name                         │ Status     │
├────┼──────────┼──────────────────────────────┼────────────┤
│ 45 │ Activity │ Implement user login         │ In Progress│
│ 46 │ Activity │ Create dashboard             │ To Do      │
│ 47 │ Meeting  │ Sprint Planning              │ Scheduled  │
│ 48 │ Activity │ Write API documentation      │ To Do      │
│ 49 │ Activity │ Fix security vulnerabilities │ In Progress│
└────┴──────────┴──────────────────────────────┴────────────┘
```

## Sprint Items Grid - AFTER
```
┌────┬──────────┬──────────────────────────────┬──────────────┬────────────┐
│ ID │ Type     │ Name                         │ Story Points │ Status     │
├────┼──────────┼──────────────────────────────┼──────────────┼────────────┤
│ 45 │ Activity │ Implement user login         │      5       │ In Progress│
│ 46 │ Activity │ Create dashboard             │      8       │ To Do      │
│ 47 │ Meeting  │ Sprint Planning              │      2       │ Scheduled  │
│ 48 │ Activity │ Write API documentation      │      3       │ To Do      │
│ 49 │ Activity │ Fix security vulnerabilities │      3       │ In Progress│
└────┴──────────┴──────────────────────────────┴──────────────┴────────────┘
                                                      Total: 21 SP shown in widget above!
```

## Activity Edit Form - NEW FIELD

```
┌─────────────────────────────────────────┐
│  Activity Details                       │
├─────────────────────────────────────────┤
│                                         │
│  Name: [Implement user login          ]│
│                                         │
│  Description:                           │
│  [Create a secure login system with    ]│
│  [password reset functionality         ]│
│                                         │
│  Story Points: [5]  ← NEW FIELD!       │
│                                         │
│  Priority: [High ▼]                     │
│                                         │
│  Status: [In Progress ▼]                │
│                                         │
│  Assigned To: [John Doe ▼]              │
│                                         │
│  [Save]  [Cancel]                       │
└─────────────────────────────────────────┘
```

## Database Schema Changes

```sql
-- CActivity table
ALTER TABLE cactivity 
ADD COLUMN story_point BIGINT NULL;

-- CMeeting table  
ALTER TABLE cmeeting
ADD COLUMN story_point BIGINT NULL;
```

## Code Architecture

```
┌─────────────────────────────────────────┐
│      ISprintableItem (Interface)        │
│  + getStoryPoint(): Long                │
│  + setStoryPoint(Long): void            │
└────────────────┬────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
┌───────▼──────┐  ┌──────▼───────┐
│  CActivity   │  │   CMeeting   │
│              │  │              │
│ - storyPoint │  │ - storyPoint │
│   : Long     │  │   : Long     │
└──────────────┘  └──────────────┘
        │                 │
        └────────┬────────┘
                 │
                 │ added to
                 ▼
        ┌────────────────┐
        │  CSprintItem   │
        │  (join entity) │
        └────────┬───────┘
                 │
                 │ many belong to
                 ▼
        ┌────────────────────┐
        │     CSprint        │
        │                    │
        │ + getTotalStory    │
        │   Points(): Long   │
        └────────────────────┘
```

## User Workflow

### 1. Create Activity with Story Points
```
User creates activity → Sets story points → Saves activity
                                ↓
                    Story points stored in database
```

### 2. Add Activity to Sprint
```
Activity with story points → Added to sprint → Sprint item created
                                    ↓
                    Sprint widget updates total automatically
```

### 3. View Sprint Summary
```
Open sprint → View widget → See total story points
                    ↓
          Expand items → See individual story points
```

## Example Calculation

```
Sprint: "Q4 Development Sprint"

Items:
┌────────────────────────────────────┬──────────────┐
│ Item Name                          │ Story Points │
├────────────────────────────────────┼──────────────┤
│ Implement user authentication      │      5       │
│ Create admin dashboard             │      8       │
│ Sprint planning meeting            │      2       │
│ Write API documentation            │      3       │
│ Fix critical security bugs         │      3       │
└────────────────────────────────────┴──────────────┘

Calculation: 5 + 8 + 2 + 3 + 3 = 21 SP

Widget Display: "5 items (21 SP)"
```

## Benefits

✓ **Team Velocity**: Track how many story points the team completes per sprint
✓ **Capacity Planning**: Plan future sprints based on story point capacity
✓ **Effort Estimation**: Estimate work complexity using a standardized metric
✓ **Sprint Progress**: Monitor sprint progress by story points completed
✓ **Workload Balance**: Ensure team members have balanced workloads

## Common Story Point Scales

### Fibonacci Sequence (Recommended)
```
1, 2, 3, 5, 8, 13, 21
```

### T-Shirt Sizes (with numeric equivalents)
```
XS = 1
S  = 2
M  = 3
L  = 5
XL = 8
```

### Linear Scale
```
1, 2, 3, 4, 5, 6, 7, 8, 9, 10
```

## Best Practices

1. **Be Consistent**: Use the same scale across all stories
2. **Relative Sizing**: Compare stories to each other, not absolute time
3. **Team Agreement**: Whole team should agree on story point values
4. **Don't Over-think**: Story points are estimates, not contracts
5. **Review and Adjust**: Refine estimates during sprint retrospectives
