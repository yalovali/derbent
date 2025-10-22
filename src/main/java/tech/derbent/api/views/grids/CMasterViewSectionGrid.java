package tech.derbent.api.views.grids;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.views.CAbstractEntityDBPage;
/* Master view section using a grid for entity selection. inherited from CMasterViewSectionBase. Provides selection change events and methods to
 * manipulate selection. Super class for: - CMasterViewSectionTreeGrid - CMasterViewSectionGridWithFilter - CMasterViewSectionGridWithFilterAndSearch
 * - CMasterViewSectionGridWithSearch - CMasterViewSectionGridWithTree Atma kafadan AI !!! */

public class CMasterViewSectionGrid<EntityClass extends CEntityDB<EntityClass>> extends CMasterViewSectionBase<EntityClass> {

	// --- Custom Event Definition ---
	public static class SelectionChangeEvent<T extends CEntityDB<T>> extends ComponentEvent<CMasterViewSectionGrid<T>> {

		private static final long serialVersionUID = 1L;
		private final T selectedItem;

		public SelectionChangeEvent(final CMasterViewSectionGrid<T> source, final T selectedItem) {
			super(source, false);
			this.selectedItem = selectedItem;
		}

		public T getSelectedItem() { return selectedItem; }
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterViewSectionGrid.class);
	private static final long serialVersionUID = 1L;
	protected CGrid<EntityClass> grid;

	public CMasterViewSectionGrid(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page) {
		super(entityClass, page);
		createMasterView();
	}

	@Override
	public void createMasterView() {
		grid = new CGrid<>(entityClass);
		grid.asSingleSelect().addValueChangeListener(this::onSelectionChange);
		page.createGridForEntity(grid);
		add(grid);
	}

	private int dpSize() {
		// If you prefer your PageableUtils, you can replace with it.
		return grid.getDataProvider().size(new Query<>());
	}

	private Optional<EntityClass> fetchIndex(final int index) {
		if (index < 0) {
			return Optional.empty();
		}
		// Query(offset, limit, sortOrders, inMemoryFilter, filter)
		return grid.getDataProvider().fetch(new Query<>(index, 1, null, null, null)).findFirst();
	}

	private CGrid<EntityClass> getGrid() { return grid; }

	@Override
	public EntityClass getSelectedItem() { return grid.asSingleSelect().getValue(); }
	// ---------- Internal utilities ----------

	@SuppressWarnings ("unchecked")
	protected void onSelectionChange(final ValueChangeEvent<?> event) {
		LOGGER.debug("Grid selection changed: {}", event.getValue() != null ? event.getValue().toString() : "null");
		final EntityClass value = (EntityClass) event.getValue();
		// reselect the old one, if new selection is null
		// if (value == null && event.getOldValue() != null) {
		// final EntityClass oldValue = (EntityClass) event.getOldValue();
		// final SingleSelect<?, ?> rawSelect = grid.asSingleSelect();
		// rawSelect.setValue(oldValue);
		// // dont fire event
		// return;
		// }
		fireEvent(new SelectionChangeEvent<>(this, value));
	}

	@Override
	public void refreshMasterView() {
		getGrid().getDataProvider().refreshAll();
	}

	/** Select a specific item (null clears). */
	@Override
	public void select(final EntityClass item) {
		grid.asSingleSelect().setValue(item);
	}

	/** Select the first row if any; otherwise clears selection. */
	public void selectFirst() {
		final Optional<EntityClass> first = fetchIndex(0);
		grid.asSingleSelect().setValue(first.orElse(null));
	}

	/** Select by zero-based index; negative or out-of-range clears. */
	public void selectIndex(final int index) {
		if (index < 0) {
			grid.asSingleSelect().clear();
			return;
		}
		final Optional<EntityClass> found = fetchIndex(index);
		grid.asSingleSelect().setValue(found.orElse(null));
	}

	/** Select the last row if any; otherwise clears selection. */
	public void selectLast() {
		final int size = dpSize();
		if (size <= 0) {
			grid.asSingleSelect().clear();
			return;
		}
		final Optional<EntityClass> last = fetchIndex(size - 1);
		grid.asSingleSelect().setValue(last.orElse(null));
	}

	/** Convenience: try to select last; if none, select first (clears when empty). */
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
		getGrid().setDataProvider(masterQuery);
	}

	@Override
	public void setItems(final List<EntityClass> filteredMeetings) {
		getGrid().setItems(filteredMeetings);
	}
}
