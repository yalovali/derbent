package tech.derbent.app.gannt.view.components;

import java.time.LocalDate;

/**
 * Example demonstrating the usage of CGanttTimelineHeader and CGanttTimelineBar components.
 * This class shows how to create and configure timeline components for a Gantt chart.
 * 
 * NOTE: This is a documentation/example file, not a runnable test class.
 */
public class GanttTimelineComponentExample {

    /**
     * Example 1: Short-duration project (< 90 days)
     * Expected: Week markers (W1, W2, W3, ...)
     */
    public static void exampleShortProject() {
        // Define a 6-week project timeline
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 2, 11);   // 42 days
        int width = 400;

        // Create timeline header - will automatically use week markers
        CGanttTimelineHeader header = new CGanttTimelineHeader(start, end, width);
        
        // Expected rendering:
        // [W1|W2|W3|W4|W5|W6]
        // Each week proportionally sized based on actual days in range
        
        System.out.println("Short project example:");
        System.out.println("Duration: " + java.time.temporal.ChronoUnit.DAYS.between(start, end) + " days");
        System.out.println("Expected scale: Weekly markers");
    }

    /**
     * Example 2: Medium-duration project (90-365 days)
     * Expected: Month markers with year boundaries highlighted
     */
    public static void exampleMediumProject() {
        // Define a 6-month project timeline
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 6, 30);   // ~180 days
        int width = 400;

        // Create timeline header - will automatically use month markers
        CGanttTimelineHeader header = new CGanttTimelineHeader(start, end, width);
        
        // Expected rendering:
        // [Jan 2024|Feb|Mar|Apr|May|Jun]
        // Each month proportionally sized (31 days = ~17%, 28/29 days = ~16%, etc.)
        
        System.out.println("Medium project example:");
        System.out.println("Duration: " + java.time.temporal.ChronoUnit.DAYS.between(start, end) + " days");
        System.out.println("Expected scale: Monthly markers");
    }

    /**
     * Example 3: Long-duration project (> 365 days)
     * Expected: Year markers with prominent boundaries
     */
    public static void exampleLongProject() {
        // Define a 2-year project timeline
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);  // 730 days
        int width = 400;

        // Create timeline header - will automatically use year markers
        CGanttTimelineHeader header = new CGanttTimelineHeader(start, end, width);
        
        // Expected rendering:
        // [2023|2024]
        // Each year proportionally sized, with year boundaries highlighted in blue
        
        System.out.println("Long project example:");
        System.out.println("Duration: " + java.time.temporal.ChronoUnit.DAYS.between(start, end) + " days");
        System.out.println("Expected scale: Yearly markers");
    }

    /**
     * Example 4: Creating synchronized timeline bars
     * Demonstrates how task bars align with the timeline header
     */
    public static void exampleSynchronizedBars() {
        // Define project timeline (3 months)
        LocalDate timelineStart = LocalDate.of(2024, 1, 1);
        LocalDate timelineEnd = LocalDate.of(2024, 3, 31);
        int width = 400;

        // Create timeline header
        CGanttTimelineHeader header = new CGanttTimelineHeader(timelineStart, timelineEnd, width);

        // Example task: Runs from Jan 15 to Feb 15 (32 days)
        LocalDate taskStart = LocalDate.of(2024, 1, 15);
        LocalDate taskEnd = LocalDate.of(2024, 2, 15);

        // Calculate positioning (same formula used in CGanttTimelineBar)
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(timelineStart, timelineEnd);
        long startOffset = java.time.temporal.ChronoUnit.DAYS.between(timelineStart, taskStart);
        long taskDuration = java.time.temporal.ChronoUnit.DAYS.between(taskStart, taskEnd) + 1;
        
        double leftPercent = (startOffset * 100.0) / totalDays;
        double widthPercent = (taskDuration * 100.0) / totalDays;

        System.out.println("Synchronized bar example:");
        System.out.println("Timeline: " + totalDays + " days");
        System.out.println("Task offset: " + startOffset + " days from start");
        System.out.println("Task duration: " + taskDuration + " days");
        System.out.println("Bar position: " + String.format("%.2f%%", leftPercent) + " from left");
        System.out.println("Bar width: " + String.format("%.2f%%", widthPercent) + " of timeline");
        
        // Note: The CGanttTimelineBar component will use the same calculation
        // to position itself, ensuring perfect alignment with the header
    }

    /**
     * Example 5: Edge case - Single day project
     * Demonstrates handling of minimal duration
     */
    public static void exampleSingleDay() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 1);
        int width = 400;

        CGanttTimelineHeader header = new CGanttTimelineHeader(start, end, width);
        
        // With padding added in CGanntGrid.calculateTimelineRange():
        // - 7 days added before
        // - 7 days added after
        // Actual timeline: Dec 25, 2023 - Jan 8, 2024 (15 days)
        
        System.out.println("Single day project example:");
        System.out.println("Original duration: 1 day");
        System.out.println("With padding: ~15 days (7 before + 1 actual + 7 after)");
        System.out.println("Expected scale: Weekly markers");
    }

    /**
     * Example 6: Cross-year boundary
     * Demonstrates year boundary highlighting
     */
    public static void exampleYearBoundary() {
        // Timeline crosses from 2023 to 2024
        LocalDate start = LocalDate.of(2023, 11, 1);
        LocalDate end = LocalDate.of(2024, 2, 29);   // ~120 days
        int width = 400;

        CGanttTimelineHeader header = new CGanttTimelineHeader(start, end, width);
        
        // Expected rendering:
        // [Nov|Dec|Jan 2024|Feb]
        //        ↑
        //   Year boundary highlighted in blue
        
        System.out.println("Year boundary example:");
        System.out.println("Timeline: Nov 2023 - Feb 2024");
        System.out.println("Expected: Month markers with Jan 2024 highlighted as year boundary");
    }

    /**
     * Example 7: Null safety
     * Demonstrates handling of null timeline dates
     */
    public static void exampleNullDates() {
        LocalDate start = null;
        LocalDate end = null;
        int width = 400;

        CGanttTimelineHeader header = new CGanttTimelineHeader(start, end, width);
        
        // Expected: Component renders with "no-timeline" class
        // No markers are displayed, graceful degradation
        
        System.out.println("Null dates example:");
        System.out.println("Timeline: null - null");
        System.out.println("Expected: Empty timeline with no-timeline class");
    }

    /**
     * Integration example: Complete workflow
     * Shows how components work together in CGanntGrid
     */
    public static void exampleCompleteWorkflow() {
        System.out.println("\n=== Complete Gantt Grid Integration ===\n");
        
        // Step 1: CGanntGrid calculates timeline range from all items
        // (In actual code, this iterates through all CGanttItem objects)
        LocalDate timelineStart = LocalDate.of(2024, 1, 1);
        LocalDate timelineEnd = LocalDate.of(2024, 12, 31);
        int width = 400;
        
        System.out.println("1. Timeline calculation:");
        System.out.println("   Range: " + timelineStart + " to " + timelineEnd);
        System.out.println("   Duration: " + java.time.temporal.ChronoUnit.DAYS.between(timelineStart, timelineEnd) + " days");
        
        // Step 2: Create timeline header with calculated range
        CGanttTimelineHeader header = new CGanttTimelineHeader(timelineStart, timelineEnd, width);
        System.out.println("\n2. Timeline header created with yearly scale");
        
        // Step 3: For each item, create a synchronized timeline bar
        // Example item 1: Q1 project
        System.out.println("\n3. Creating task bars:");
        LocalDate item1Start = LocalDate.of(2024, 1, 1);
        LocalDate item1End = LocalDate.of(2024, 3, 31);
        System.out.println("   Task 1: Q1 2024 (Jan 1 - Mar 31)");
        calculateBarPosition(timelineStart, timelineEnd, item1Start, item1End);
        
        // Example item 2: Q3-Q4 project
        LocalDate item2Start = LocalDate.of(2024, 7, 1);
        LocalDate item2End = LocalDate.of(2024, 12, 31);
        System.out.println("\n   Task 2: Q3-Q4 2024 (Jul 1 - Dec 31)");
        calculateBarPosition(timelineStart, timelineEnd, item2Start, item2End);
        
        System.out.println("\n4. Result: Header and bars are perfectly aligned");
        System.out.println("   All components use same calculation formula");
    }

    /**
     * Helper method to calculate and display bar position
     */
    private static void calculateBarPosition(LocalDate timelineStart, LocalDate timelineEnd,
                                            LocalDate taskStart, LocalDate taskEnd) {
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(timelineStart, timelineEnd);
        long startOffset = java.time.temporal.ChronoUnit.DAYS.between(timelineStart, taskStart);
        long taskDuration = java.time.temporal.ChronoUnit.DAYS.between(taskStart, taskEnd) + 1;
        
        double leftPercent = (startOffset * 100.0) / totalDays;
        double widthPercent = (taskDuration * 100.0) / totalDays;
        
        System.out.println("      Position: " + String.format("%.1f%%", leftPercent) + 
                          ", Width: " + String.format("%.1f%%", widthPercent));
    }

    /**
     * Main method for running examples
     */
    public static void main(String[] args) {
        System.out.println("=== Gantt Timeline Component Examples ===\n");
        
        exampleShortProject();
        System.out.println();
        
        exampleMediumProject();
        System.out.println();
        
        exampleLongProject();
        System.out.println();
        
        exampleSynchronizedBars();
        System.out.println();
        
        exampleSingleDay();
        System.out.println();
        
        exampleYearBoundary();
        System.out.println();
        
        exampleNullDates();
        
        exampleCompleteWorkflow();
    }
}
