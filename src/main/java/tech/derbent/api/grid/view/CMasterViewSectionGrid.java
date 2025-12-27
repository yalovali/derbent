package tech.derbent.api.grid.view;

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
import tech.derbent.api.grid.domain.CGrid;

public class CMasterViewSectionGrid<EntityClass extends CEntityDB<EntityClass>> extends CMasterViewSectionBase<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterViewSectionGrid.class);
	private static final long serialVersionUID = 1L;
	protected CGrid<EntityClass> grid;

	public CMasterViewSectionGrid(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page) {
		super(entityClass, page);
		createMasterView();
	}

	@SuppressWarnings ("static-method")
	protected Component createGridToolbar() {
		return null;
	}

	@Override
	public void createMasterView() {
		final Component toolbar = createGridToolbar();
		if (toolbar != null) {
			add(toolbar);
		}
		grid = new CGrid<>(entityClass);
		grid.asSingleSelect().addValueChangeListener(this::onSelectionChange);
		page.createGridForEntity(grid);
		add(grid);
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
