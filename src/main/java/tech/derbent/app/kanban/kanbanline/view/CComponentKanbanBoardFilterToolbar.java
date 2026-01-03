package tech.derbent.app.kanban.kanbanline.view;

import java.util.List;
import java.util.Objects;
import tech.derbent.api.ui.component.filter.CEntityTypeFilter;
import tech.derbent.api.ui.component.filter.CResponsibleUserFilter;
import tech.derbent.api.ui.component.filter.CSprintFilter;
import tech.derbent.api.ui.component.filter.CUniversalFilterToolbar;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;

/**
 * CComponentKanbanBoardFilterToolbar - Filtering toolbar for Kanban board items.
 * <p>
 * Refactored to use the new universal filtering framework with composable filter components.
 * Automatically discovers all entity types (Activity, Meeting, Sprint, etc.) from sprint items.
 * </p>
 * 
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Sprint selection filter</li>
 * <li>Entity type filter (dynamically populated with Activity, Meeting, Sprint, etc.)</li>
 * <li>Responsible user filter (All items vs. My items)</li>
 * <li>Automatic value persistence across refreshes</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Design Benefits:</b>
 * <ul>
 * <li>Simple, clear code using composition</li>
 * <li>Automatic entity type discovery (fixes missing Meeting issue)</li>
 * <li>Reusable filter components</li>
 * <li>Easy to extend with new filters</li>
 * </ul>
 * </p>
 */
public class CComponentKanbanBoardFilterToolbar extends CUniversalFilterToolbar<CSprintItem> {

	private static final long serialVersionUID = 1L;

	private final CSprintFilter sprintFilter;
	private final CEntityTypeFilter entityTypeFilter;
	private final CResponsibleUserFilter responsibleUserFilter;

	/**
	 * Builds the filter toolbar and its components using composition.
	 */
	public CComponentKanbanBoardFilterToolbar() {
		super();
		// Set explicit ID for value persistence across component recreations
		setId("kanbanBoardFilterToolbar");

		// Create filter components
		sprintFilter = new CSprintFilter();
		entityTypeFilter = new CEntityTypeFilter();
		responsibleUserFilter = new CResponsibleUserFilter();

		// Add filters to toolbar (order matters for display)
		addFilterComponent(sprintFilter);
		addFilterComponent(entityTypeFilter);
		addFilterComponent(responsibleUserFilter);

		// Build clear button
		build();
	}

	/**
	 * Updates available sprint options and selects a default.
	 * 
	 * @param sprints Available sprints
	 * @param defaultSprint Default sprint to select
	 */
	public void setAvailableSprints(final List<CSprint> sprints, final CSprint defaultSprint) {
		sprintFilter.setAvailableSprints(sprints, defaultSprint);
	}

	/**
	 * Updates available entity type options based on sprint item list.
	 * <p>
	 * This method automatically discovers all unique entity types (Activity, Meeting, Sprint, etc.)
	 * from the provided sprint items and populates the entity type filter dropdown.
	 * Uses ISprintableItem interface for type-safe entity extraction.
	 * </p>
	 * 
	 * @param items Sprint items to analyze for entity types
	 */
	public void setAvailableItems(final List<CSprintItem> items) {
		Objects.requireNonNull(items, "Sprint items list cannot be null");
		
		// Extract actual ISprintableItem entities from sprint items
		// Use getItem() which returns ISprintableItem interface
		final List<Object> entities = items.stream()
				.filter(Objects::nonNull)
				.map(CSprintItem::getItem)
				.filter(Objects::nonNull)
				.map(item -> (Object) item)
				.toList();

		// Update entity type filter with discovered types
		entityTypeFilter.setAvailableEntityTypes(entities);
	}

	/**
	 * Gets the sprint filter component.
	 * 
	 * @return The sprint filter
	 */
	public CSprintFilter getSprintFilter() {
		return sprintFilter;
	}

	/**
	 * Gets the entity type filter component.
	 * 
	 * @return The entity type filter
	 */
	public CEntityTypeFilter getEntityTypeFilter() {
		return entityTypeFilter;
	}

	/**
	 * Gets the responsible user filter component.
	 * 
	 * @return The responsible user filter
	 */
	public CResponsibleUserFilter getResponsibleUserFilter() {
		return responsibleUserFilter;
	}
}
