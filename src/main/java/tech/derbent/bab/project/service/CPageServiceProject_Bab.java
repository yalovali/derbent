package tech.derbent.bab.project.service;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.utils.CJsonSerializer;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;

public class CPageServiceProject_Bab extends CPageServiceDynamicPage<CProject_Bab> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceProject_Bab.class);
	private CButton buttonApply;
	private CButton buttonToJson;

	public CPageServiceProject_Bab(final IPageServiceImplementer<CProject_Bab> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CProject_Bab");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CProject_Bab> gridView = (CGridViewBaseDBEntity<CProject_Bab>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}

	@Override
	protected void configureToolbar(final CCrudToolbar toolbar) {
		buttonToJson = new CButton("To JSON", VaadinIcon.PLAY.create());
		buttonToJson.addClickListener(event -> on_toJson_clicked());
		toolbar.addCustomComponent(buttonToJson);
		buttonApply = new CButton("Apply Project", VaadinIcon.PLAY.create());
		buttonApply.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
		buttonApply.getElement().setAttribute("title", "Export this project JSON to temporary file");
		buttonApply.addClickListener(event -> on_apply_clicked());
		toolbar.addCustomComponent(buttonApply);
	}

	private void on_apply_clicked() {
		if (getValue() == null) {
			LOGGER.warn("Apply button clicked but no BAB project loaded");
			CNotificationService.showWarning("Please load a BAB project before applying");
			return;
		}
		try {
			final String json = CJsonSerializer.toPrettyJson(getValue(), EJsonScenario.JSONSENARIO_BABCONFIGURATION);
			final File tempFile = new File(System.getProperty("java.io.tmpdir"), "bab_project.json");
			try (var writer = new java.io.FileWriter(tempFile)) {
				writer.write(json);
				writer.flush();
				CNotificationService.showInfoDialog("Project JSON",
						"Project JSON has been written to temporary file:\n" + tempFile.getAbsolutePath());
			} catch (final Exception e) {
				LOGGER.error("Error writing project JSON to temporary file: {}", e.getMessage(), e);
				CNotificationService.showError("Failed to write project JSON to temporary file");
				return;
			}
		} catch (final Exception e) {
			LOGGER.error("Error converting BAB project to JSON: {}", e.getMessage(), e);
			CNotificationService.showError("Failed to convert BAB project to JSON");
		}
	}

	private void on_toJson_clicked() {
		if (getValue() == null) {
			LOGGER.warn("ToJson button clicked but no BAB project loaded");
			CNotificationService.showWarning("Please load a BAB project before converting to JSON");
			return;
		}
		try {
			final String json = CJsonSerializer.toPrettyJson(getValue(), EJsonScenario.JSONSENARIO_BABCONFIGURATION);
			CNotificationService.showInfoDialog("Project JSON", json);
		} catch (final Exception e) {
			LOGGER.error("Error converting BAB project to JSON: {}", e.getMessage(), e);
			CNotificationService.showError("Failed to convert BAB project to JSON");
		}
	}
}
