package tech.derbent.abstracts.views;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.shared.Registration;
import tech.derbent.abstracts.domains.CEntityDB;

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

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterViewSectionGrid.class);
	protected CGrid<EntityClass> grid;
	protected final Class<EntityClass> entityClass;
	private final CAbstractEntityDBPage<EntityClass> page;

	public CMasterViewSectionGrid(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page) {
		super();
		this.entityClass = entityClass;
		this.page = page;
		createMasterView();
	}

	// --- Listener Registration ---
	public Registration addSelectionChangeListener(final ComponentEventListener<SelectionChangeEvent<EntityClass>> listener) {
		return addListener(SelectionChangeEvent.class, (ComponentEventListener) listener);
	}

	@Override
	public void createMasterView() {
		grid = new CGrid<>(entityClass);
		grid.asSingleSelect().addValueChangeListener(this::onSelectionChange);
		page.createGridForEntity(grid);
		add(new CDiv("asdfsafs"));
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

	public CGrid<EntityClass> getGrid() { return grid; }

	public EntityClass getSelectedItem() { return grid.asSingleSelect().getValue(); }
	// ---------- Internal utilities ----------

	@SuppressWarnings ("unchecked")
	protected void onSelectionChange(final ValueChangeEvent<?> event) {
		LOGGER.debug("Grid selection changed: {}", event.getValue() != null ? event.getValue().toString() : "null");
		final EntityClass value = (EntityClass) event.getValue();
		fireEvent(new SelectionChangeEvent<>(this, value));
	}

	public void refresh() {
		grid.getDataProvider().refreshAll();
	}

	/** Select a specific item (null clears). */
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
	public void selectLastOrFirst(final EntityClass lastEntity) {
		if (lastEntity != null) {
			select(lastEntity);
			return;
		}
		selectFirst();
	}
}
