package tech.derbent.api.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.utils.Check;

public abstract class CComponentRelationBase<MasterClass extends CEntityDB<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CComponentDBEntity<MasterClass> {

	private static final long serialVersionUID = 1L;
	protected Supplier<List<RelationalClass>> getSettings;
	private final Grid<RelationalClass> grid;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected final Class<RelationalClass> relationalClass;
	protected Runnable saveEntity;

	public CComponentRelationBase(final String title, final Class<MasterClass> entityClass, final Class<RelationalClass> relationalClass,
			ApplicationContext applicationContext) {
		super(title, entityClass, applicationContext);
		this.relationalClass = relationalClass;
		grid = new Grid<>(relationalClass, false);
		getSettings = () -> List.of();
	}

	protected RelationalClass getSelectedSetting() { return grid.asSingleSelect().getValue(); }

	@Override
	protected void initPanel() throws Exception {
		// super is abstract, stop here
		// super.initPanel();
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

	protected void setupGrid(final Grid<RelationalClass> grid) {
		Check.notNull(grid, "Grid cannot be null when setting up relational component");
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.getStyle().set("border-radius", "8px");
		grid.getStyle().set("border", "1px solid #E0E0E0");
		grid.setWidthFull();
		grid.setHeight("300px");
		add(grid);
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(new ArrayList<String>());
	}
}
