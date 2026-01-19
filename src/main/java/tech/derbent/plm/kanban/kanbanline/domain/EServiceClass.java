package tech.derbent.plm.kanban.kanbanline.domain;

/**
 * Service Class enum based on Kanban Method (David J. Anderson, 2010).
 * Defines priority policies for work items to enable flow-based scheduling.
 * 
 * @see <a href="https://djaa.com/">Kanban Method - David J. Anderson</a>
 */
public enum EServiceClass {
	
	/** Critical items requiring immediate action, bypassing normal flow. Color: Red */
	EXPEDITE("Expedite", "Critical - Immediate action required", "#FF0000"),
	
	/** Time-sensitive items with hard deadlines. Color: Orange */
	FIXED_DATE("Fixed Date", "Hard deadline - Time-sensitive", "#FF9900"),
	
	/** Normal priority work items processed in FIFO order. Color: Blue */
	STANDARD("Standard", "Normal priority - FIFO processing", "#0099FF"),
	
	/** Low urgency background work, filler tasks. Color: Gray */
	INTANGIBLE("Intangible", "Low urgency - Background work", "#999999");
	
	private final String displayName;
	private final String description;
	private final String color;
	
	EServiceClass(final String displayName, final String description, final String color) {
		this.displayName = displayName;
		this.description = description;
		this.color = color;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
}
