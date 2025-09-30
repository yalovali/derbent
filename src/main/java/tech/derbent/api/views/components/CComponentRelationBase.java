package tech.derbent.api.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.domains.CEntityDB;

public abstract class CComponentRelationBase<MasterClass extends CEntityDB<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CComponentDBEntity<MasterClass> {

	private static final long serialVersionUID = 1L;
	protected Supplier<List<RelationalClass>> getSettings;
	protected final Grid<RelationalClass> grid;
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

	@Override
	public void populateForm() {
		super.populateForm();
		LOGGER.debug("Refreshing grid data");
		if (getSettings != null) {
			grid.setItems(getSettings.get());
		}
	}

	/** Sets the settings accessors (getters, setters, save callback) */
	public void setSettingsAccessors(final Supplier<List<RelationalClass>> getSettings, final Runnable saveEntity) {
		LOGGER.debug("Setting settings accessors");
		this.getSettings = getSettings;
		this.saveEntity = saveEntity;
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(new ArrayList<String>());
	}
}
