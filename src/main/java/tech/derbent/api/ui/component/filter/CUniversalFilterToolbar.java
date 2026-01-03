package tech.derbent.api.ui.component.filter;

/**
 * CUniversalFilterToolbar - Universal filter toolbar for all use cases.
 * <p>
 * A concrete implementation of CAbstractFilterToolbar that can be used throughout
 * the application by composing different filter components:
 * <ul>
 * <li>Kanban board filtering (sprint, entity type, responsible user)</li>
 * <li>Grid filtering (search, status, date range)</li>
 * <li>Master-detail filtering</li>
 * <li>Asset and budget filtering</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Usage Examples:</b>
 * <pre>
 * // Kanban board filtering
 * CUniversalFilterToolbar&lt;CSprintItem&gt; toolbar = new CUniversalFilterToolbar&lt;&gt;();
 * toolbar.setId("kanbanBoardFilterToolbar");
 * 
 * CSprintFilter sprintFilter = new CSprintFilter();
 * CEntityTypeFilter entityTypeFilter = new CEntityTypeFilter();
 * CResponsibleUserFilter responsibleFilter = new CResponsibleUserFilter();
 * 
 * toolbar
 *     .addFilterComponent(sprintFilter)
 *     .addFilterComponent(entityTypeFilter)
 *     .addFilterComponent(responsibleFilter)
 *     .addFilterChangeListener(criteria -> refreshBoard(criteria));
 * 
 * toolbar.buildClearButton();
 * toolbar.valuePersist_enable();
 * 
 * // Later, update available options
 * sprintFilter.setAvailableSprints(sprints, defaultSprint);
 * entityTypeFilter.setAvailableEntityTypes(sprintItems);
 * </pre>
 * </p>
 * 
 * <p>
 * <b>Design Benefits:</b>
 * <ul>
 * <li>Composition: Build toolbars from reusable filter components</li>
 * <li>Flexibility: Add any combination of filters</li>
 * <li>Consistency: All toolbars use the same pattern</li>
 * <li>Maintainability: Changes to one filter affect all uses</li>
 * <li>Testability: Each filter component can be tested independently</li>
 * </ul>
 * </p>
 * 
 * @param <T> The entity type being filtered
 */
public class CUniversalFilterToolbar<T> extends CAbstractFilterToolbar<T> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a universal filter toolbar.
	 * <p>
	 * After creation, add filter components using {@link #addFilterComponent(IFilterComponent)},
	 * then call {@link #buildClearButton()} to add the clear button.
	 * </p>
	 */
	public CUniversalFilterToolbar() {
		super();
	}

	/**
	 * Builds and adds the clear button to the toolbar.
	 * <p>
	 * This should be called after all filter components have been added.
	 * </p>
	 * 
	 * @return This toolbar for method chaining
	 */
	public CUniversalFilterToolbar<T> build() {
		buildClearButton();
		return this;
	}
}
