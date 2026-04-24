package tech.derbent.api.services.pageservice;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.ui.component.basic.CComboBoxOption;
import tech.derbent.api.ui.notifications.CNotificationService;

public abstract class CPageServiceDynamicPage<EntityClass extends CEntityDB<EntityClass>> extends CPageService<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceDynamicPage.class);

	public CPageServiceDynamicPage(final IPageServiceImplementer<EntityClass> view) {
		super(view);
	}

	public List<CComboBoxOption> getComboValuesOfHierarchyLevel() {
		return List.of(
				new CComboBoxOption("Level -1 (Leaf Task/Child Only)", "-1", "#546E7A", "vaadin:arrow-down"),
				new CComboBoxOption("Level 0 (Epic/Requirement)", "0", "#5E35B1", "vaadin:triangle-up"),
				new CComboBoxOption("Level 1 (Feature/Milestone)", "1", "#1E88E5", "vaadin:cluster"),
				new CComboBoxOption("Level 2 (User Story/Work Package)", "2", "#43A047", "vaadin:road-branches"),
				new CComboBoxOption("Level 3 (Task/Sub Item)", "3", "#FB8C00", "vaadin:tasks"),
				new CComboBoxOption("Level 4 (Detailed Step)", "4", "#8E24AA", "vaadin:list-ol"));
	}

	@Override
	@SuppressWarnings ("unchecked")
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for EntityClass");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<EntityClass> gridView = (CGridViewBaseDBEntity<EntityClass>) getView();
			gridView.generateGridReport();
			return;
		}
		final EntityClass entity = getValue();
		if (entity == null) {
			CNotificationService.showWarning("No data to export");
			return;
		}
		generateCSVReport(List.of(entity), (Class<EntityClass>) entity.getClass(), entity.getClass().getSimpleName().toLowerCase());
	}
}
