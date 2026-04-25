package tech.derbent.plm.sprints.planning.view.components;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.sprints.domain.CSprint;

/**
 * Dialog for adding a selected backlog item to a sprint.
 *
 * <p>This complements drag/drop so users can plan quickly without relying on DnD (mobile, trackpad).
 * The dialog is intentionally lightweight: select sprint → confirm.</p>
 */
public class CDialogAddBacklogItemToSprint extends CDialog {

	public static final String ID_BUTTON_CANCEL = "custom-sprint-planning-add-to-sprint-cancel";
	public static final String ID_BUTTON_OK = "custom-sprint-planning-add-to-sprint-ok";
	public static final String ID_COMBO_SPRINT = "custom-sprint-planning-add-to-sprint-sprint-combobox";

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogAddBacklogItemToSprint.class);
	private static final long serialVersionUID = 1L;

	private final String itemTitle;
	private final Consumer<CSprint> onConfirm;
	private final List<CSprint> availableSprints;

	private CButton buttonCancel;
	private CButton buttonOk;
	private CComboBox<CSprint> comboBoxSprint;

	public CDialogAddBacklogItemToSprint(final String itemTitle, final List<CSprint> availableSprints,
			final Consumer<CSprint> onConfirm) throws Exception {
		Check.notBlank(itemTitle, "Item title cannot be blank");
		Check.notNull(availableSprints, "Available sprints list cannot be null");
		Check.notNull(onConfirm, "Confirm handler cannot be null");

		this.itemTitle = itemTitle;
		this.availableSprints = availableSprints;
		this.onConfirm = onConfirm;

		setupDialog();
	}

	@Override
	public String getDialogTitleString() {
		return "Add to Sprint";
	}

	@Override
	protected Icon getFormIcon() {
		return VaadinIcon.PLUS.create();
	}

	@Override
	protected String getFormTitleString() {
		return "Add Backlog Item to Sprint";
	}

	@Override
	protected void setupContent() {
		mainLayout.add(createTextBannerSection(
				"Selected item: %s".formatted(itemTitle),
				"var(--lumo-primary-text-color)",
				"var(--lumo-primary-color-10pct)"));

		comboBoxSprint = new CComboBox<>("Sprint");
		comboBoxSprint.setId(ID_COMBO_SPRINT);
		comboBoxSprint.setWidthFull();
		comboBoxSprint.setClearButtonVisible(true);
		comboBoxSprint.setItems(availableSprints);
		comboBoxSprint.setItemLabelGenerator(sprint -> sprint != null ? sprint.getName() : "");
		mainLayout.add(comboBoxSprint);
	}

	@Override
	protected void setupButtons() {
		buttonCancel = new CButton("Cancel", null);
		buttonCancel.setId(ID_BUTTON_CANCEL);
		buttonCancel.addClickListener(event -> close());

		buttonOk = new CButton("Add", null);
		buttonOk.setId(ID_BUTTON_OK);
		buttonOk.addClickListener(event -> on_buttonOk_clicked());

		buttonLayout.add(buttonCancel, buttonOk);
	}

	private void on_buttonOk_clicked() {
		try {
			final CSprint sprint = comboBoxSprint.getValue();
			if (sprint == null) {
				CNotificationService.showWarning("Select a sprint first");
				return;
			}
			onConfirm.accept(sprint);
			close();
		} catch (final Exception e) {
			LOGGER.error("Failed to add item '{}' to sprint: {}", itemTitle, e.getMessage(), e);
			CNotificationService.showException("Unable to add item to sprint", e);
		}
	}
}
