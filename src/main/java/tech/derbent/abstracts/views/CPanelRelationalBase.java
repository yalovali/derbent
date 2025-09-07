package tech.derbent.abstracts.views;

import java.util.List;
import java.util.function.Supplier;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;

public abstract class CPanelRelationalBase<MasterClass extends CEntityDB<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CAccordionDBEntity<MasterClass> {

	private static final long serialVersionUID = 1L;
	protected Supplier<List<RelationalClass>> getSettings;
	protected final Grid<RelationalClass> grid;
	protected final Class<RelationalClass> relationalClass;
	protected Runnable saveEntity;

	public CPanelRelationalBase(final String title, final MasterClass currentEntity, final CEnhancedBinder<MasterClass> beanValidationBinder,
			final Class<MasterClass> entityClass, final CAbstractService<MasterClass> masterService, final Class<RelationalClass> relationalClass) {
		super(title, currentEntity, beanValidationBinder, entityClass, masterService);
		this.relationalClass = relationalClass;
		this.grid = new Grid<>(relationalClass, false);
		this.getSettings = () -> List.of();
	}

	public void refresh() {
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
		refresh();
	}
}
