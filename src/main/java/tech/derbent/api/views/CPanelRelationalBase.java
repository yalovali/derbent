package tech.derbent.api.views;

import java.util.List;
import java.util.function.Supplier;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.views.grids.CGrid;

public abstract class CPanelRelationalBase<MasterClass extends CEntityDB<MasterClass>, RelationalClass extends CEntityDB<RelationalClass>>
		extends CAccordionDBEntity<MasterClass> {

	private static final long serialVersionUID = 1L;
	protected Supplier<List<RelationalClass>> getSettings;
	protected final Grid<RelationalClass> grid;
	protected final Class<RelationalClass> relationalClass;
	protected Runnable saveEntity;

	public CPanelRelationalBase(final String title, IContentOwner parentContent, final CEnhancedBinder<MasterClass> beanValidationBinder,
			final Class<MasterClass> entityClass, final CAbstractService<MasterClass> masterService, final Class<RelationalClass> relationalClass) {
		super(title, parentContent, beanValidationBinder, entityClass, masterService);
		this.relationalClass = relationalClass;
		this.grid = new CGrid<>(relationalClass);
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
