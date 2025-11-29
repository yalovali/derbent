package tech.derbent.api.grid.widget;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.view.CAbstractEntityDBPage;
import tech.derbent.api.grid.view.CMasterViewSectionBase;
import tech.derbent.api.utils.Check;

/**
 * CMasterViewSectionWidgetGrid - Master view section that uses CWidgetGrid.
 * Follows the same pattern as CMasterViewSectionGrid but uses widget-based display.
 *
 * This component maintains:
 * - Same grid binding patterns
 * - Same selection behavior
 * - Same master-detail relations
 *
 * @param <EntityClass> the entity type
 */
public class CMasterViewSectionWidgetGrid<EntityClass extends CEntityDB<EntityClass>> extends CMasterViewSectionBase<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterViewSectionWidgetGrid.class);
	private static final long serialVersionUID = 1L;

	protected CWidgetGrid<EntityClass> grid;
	protected IEntityDisplayWidget<EntityClass> displayWidget;

	/**
	 * Constructor for CMasterViewSectionWidgetGrid.
	 *
	 * @param entityClass   the entity class
	 * @param page          the parent page
	 * @param displayWidget the display widget for rendering entities
	 */
	public CMasterViewSectionWidgetGrid(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page,
			final IEntityDisplayWidget<EntityClass> displayWidget) {
		super(entityClass, page);
		Check.notNull(displayWidget, "Display widget cannot be null");
		this.displayWidget = displayWidget;
		createMasterView();
	}

	/**
	 * Creates a toolbar component for the grid.
	 * Subclasses can override to add custom toolbar.
	 *
	 * @return the toolbar component or null
	 */
	protected Component createGridToolbar() {
		return null;
	}

	@Override
	public void createMasterView() {
		final Component toolbar = createGridToolbar();
		if (toolbar != null) {
			add(toolbar);
		}

		grid = new CWidgetGrid<>(entityClass);
		grid.setDisplayWidget(displayWidget);
		grid.asSingleSelect().addValueChangeListener(this::onSelectionChange);

		// Configure the widget grid columns
		configureGridColumns();

		add(grid);
	}

	/**
	 * Configures the grid columns.
	 * Default implementation adds ID and widget columns.
	 * Subclasses can override to customize columns.
	 */
	protected void configureGridColumns() {
		grid.addStandardColumns(getWidgetColumnHeader());
	}

	/**
	 * Gets the header text for the widget column.
	 * Default is "Entity". Subclasses can override to customize.
	 *
	 * @return the widget column header
	 */
	protected String getWidgetColumnHeader() {
		return entityClass.getSimpleName().replace("C", "");
	}

	private int dpSize() {
		return grid.getDataProvider().size(new Query<>());
	}

	private Optional<EntityClass> fetchIndex(final int index) {
		if (index < 0) {
			return Optional.empty();
		}
		return grid.getDataProvider().fetch(new Query<>(index, 1, null, null, null)).findFirst();
	}

	/**
	 * Gets the underlying widget grid.
	 *
	 * @return the widget grid
	 */
	public CWidgetGrid<EntityClass> getGrid() {
		return grid;
	}

	@Override
	public EntityClass getSelectedItem() {
		return grid.asSingleSelect().getValue();
	}

	@SuppressWarnings("unchecked")
	protected void onSelectionChange(final ValueChangeEvent<?> event) {
		LOGGER.debug("Widget grid selection changed: {}", event.getValue() != null ? event.getValue().toString() : "null");
		final EntityClass value = (EntityClass) event.getValue();
		fireEvent(new SelectionChangeEvent<>(this, value));
	}

	@Override
	public void refreshMasterView() {
		grid.getDataProvider().refreshAll();
	}

	@Override
	public void select(final EntityClass item) {
		grid.asSingleSelect().setValue(item);
	}

	/**
	 * Selects the first row if any; otherwise clears selection.
	 */
	public void selectFirst() {
		final Optional<EntityClass> first = fetchIndex(0);
		grid.asSingleSelect().setValue(first.orElse(null));
	}

	/**
	 * Selects by zero-based index; negative or out-of-range clears.
	 *
	 * @param index the index to select
	 */
	public void selectIndex(final int index) {
		if (index < 0) {
			grid.asSingleSelect().clear();
			return;
		}
		final Optional<EntityClass> found = fetchIndex(index);
		grid.asSingleSelect().setValue(found.orElse(null));
	}

	/**
	 * Selects the last row if any; otherwise clears selection.
	 */
	public void selectLast() {
		final int size = dpSize();
		if (size <= 0) {
			grid.asSingleSelect().clear();
			return;
		}
		final Optional<EntityClass> last = fetchIndex(size - 1);
		grid.asSingleSelect().setValue(last.orElse(null));
	}

	@Override
	public void selectLastOrFirst(final EntityClass lastEntity) {
		if (lastEntity != null) {
			select(lastEntity);
			return;
		}
		selectFirst();
	}

	@Override
	public void setDataProvider(final CallbackDataProvider<EntityClass, Void> masterQuery) {
		grid.setDataProvider(masterQuery);
	}

	@Override
	public void setItems(final List<EntityClass> items) {
		grid.setItems(items);
	}

	/**
	 * Sets a new display widget for the grid.
	 *
	 * @param displayWidget the new display widget
	 */
	public void setDisplayWidget(final IEntityDisplayWidget<EntityClass> displayWidget) {
		Check.notNull(displayWidget, "Display widget cannot be null");
		this.displayWidget = displayWidget;
		grid.setDisplayWidget(displayWidget);
	}
}
