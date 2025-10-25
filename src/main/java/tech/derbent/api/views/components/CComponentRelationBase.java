package tech.derbent.api.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.views.grids.CGrid;
import tech.derbent.base.session.service.ISessionService;

public abstract class CComponentRelationBase<MasterClass extends CEntityDB<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CComponentDBEntity<MasterClass> {

	private static final long serialVersionUID = 1L;
	protected Supplier<List<RelationalClass>> getSettings;
	private final Grid<RelationalClass> grid;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected final Class<RelationalClass> relationalClass;
	protected Runnable saveEntity;
	protected ISessionService sessionService;

	public CComponentRelationBase(final String title, final Class<MasterClass> entityClass, final Class<RelationalClass> relationalClass,
			ISessionService sessionService) {
		super(title, entityClass);
		this.relationalClass = relationalClass;
		this.sessionService = sessionService;
		grid = new CGrid<>(relationalClass);
		getSettings = () -> List.of();
	}

	protected RelationalClass getSelectedSetting() { return grid.asSingleSelect().getValue(); }

	@Override
	protected void initPanel() throws Exception {
		CGrid.setupGrid(grid);
		setupGrid(grid);
	}

	@Override
	public void populateForm() {
		super.populateForm();
		// LOGGER.debug("Refreshing grid data");
		if (getSettings != null) {
			grid.setItems(getSettings.get());
		}
	}

	/** Sets the settings accessors (getters, setters, save callback) */
	public void setSettingsAccessors(final Supplier<List<RelationalClass>> getSettings, final Runnable saveEntity) {
		this.getSettings = getSettings;
		this.saveEntity = saveEntity;
	}

	protected void setupGrid(Grid<RelationalClass> grid) {
		grid.setHeight("300px");
		add(grid);
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(new ArrayList<String>());
	}
}
