package tech.derbent.gannt.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CMasterViewSectionBase;

public class CMasterViewSectionGannt<EntityClass extends CEntityDB<EntityClass>> extends CMasterViewSectionBase<EntityClass> {
	// --- Custom Event Definition ---
	public static class SelectionChangeEvent<T extends CEntityDB<T>> extends ComponentEvent<CMasterViewSectionGannt<T>> {
		private static final long serialVersionUID = 1L;
		private final T selectedItem;

		public SelectionChangeEvent(final CMasterViewSectionGannt<T> source, final T selectedItem) {
			super(source, false);
			this.selectedItem = selectedItem;
		}

		public T getSelectedItem() { return selectedItem; }
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CMasterViewSectionGannt.class);
	private static final long serialVersionUID = 1L;
	protected CGrid<EntityClass> grid;

	public CMasterViewSectionGannt(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page) {
		super(entityClass, page);
		LOGGER.debug("Initializing CMasterViewSectionGannt for entity: {}", entityClass.getSimpleName());
		createMasterView();
	}

	@Override
	public void createMasterView() {
		LOGGER.debug("Creating master view");
		grid = new CGrid<>(entityClass);
		grid.asSingleSelect().addValueChangeListener(this::onSelectionChange);
		page.createGridForEntity(grid);
		add(grid);
	}

	@Override
	public EntityClass getSelectedItem() {
		LOGGER.debug("Getting selected item");
		return null;
	}

	@SuppressWarnings ("unchecked")
	protected void onSelectionChange(final ValueChangeEvent<?> event) {
		LOGGER.debug("Grid selection changed: {}", event.getValue() != null ? event.getValue().toString() : "null");
		final EntityClass value = (EntityClass) event.getValue();
		fireEvent(new SelectionChangeEvent<>(this, value));
	}

	@Override
	public void refreshMasterView() {
		LOGGER.debug("Refreshing master view");
	}

	@Override
	public void select(final EntityClass object) {
		LOGGER.debug("Selecting object: {}", object != null ? object.toString() : "null");
		// TODO Auto-generated method stub
	}

	@Override
	public void selectLastOrFirst(final EntityClass orElse) {
		LOGGER.debug("Selecting last or first, default: {}", orElse != null ? orElse.toString() : "null");
		// TODO Auto-generated method stub
	}

	@Override
	public void setDataProvider(final CallbackDataProvider<EntityClass, Void> masterQuery) {
		LOGGER.debug("Setting data provider");
	}

	@Override
	public void setItems(final List<EntityClass> filteredMeetings) {
		LOGGER.debug("Setting items, count: {}", filteredMeetings != null ? filteredMeetings.size() : 0);
	}
}
