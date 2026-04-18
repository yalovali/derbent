package tech.derbent.plm.agile.view;

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
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;

/** Dialog for selecting the type of agile child entity to create. */
public class CDialogAgileChildTypeSelection extends CDialog {

	public static final String ID_BUTTON_CANCEL = "custom-agile-child-type-cancel-button";
	public static final String ID_BUTTON_CREATE = "custom-agile-child-type-create-button";
	public static final String ID_COMBOBOX_TYPE = "custom-agile-child-type-combobox";

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogAgileChildTypeSelection.class);
	private static final long serialVersionUID = 1L;

	private final List<EntityTypeConfig<?>> entityTypes;
	private final Consumer<EntityTypeConfig<?>> onCreate;

	private CButton buttonCancel;
	private CButton buttonCreate;
	private CComboBox<EntityTypeConfig<?>> comboBoxType;

	public CDialogAgileChildTypeSelection(final List<EntityTypeConfig<?>> entityTypes, final Consumer<EntityTypeConfig<?>> onCreate) throws Exception {
		Check.notEmpty(entityTypes, "entityTypes cannot be empty");
		Check.notNull(onCreate, "onCreate cannot be null");
		this.entityTypes = entityTypes;
		this.onCreate = onCreate;
		setupDialog();
	}

	@Override
	public String getDialogTitleString() {
		return "Create Child";
	}

	@Override
	protected Icon getFormIcon() {
		return VaadinIcon.PLUS_CIRCLE_O.create();
	}

	@Override
	protected String getFormTitleString() {
		return "Select Type";
	}

	@Override
	protected void setupButtons() {
		buttonCreate = CButton.createPrimary("Create", VaadinIcon.PLUS.create(), event -> on_buttonCreate_clicked());
		buttonCreate.setId(ID_BUTTON_CREATE);
		buttonCancel = CButton.createTertiary("Cancel", VaadinIcon.CLOSE.create(), event -> close());
		buttonCancel.setId(ID_BUTTON_CANCEL);
		buttonLayout.add(buttonCreate, buttonCancel);
	}

	@Override
	protected void setupContent() {
		comboBoxType = new CComboBox<>("Child Type");
		comboBoxType.setId(ID_COMBOBOX_TYPE);
		comboBoxType.setWidthFull();
		comboBoxType.setItems(entityTypes);
		comboBoxType.setItemLabelGenerator(EntityTypeConfig::getDisplayName);
		comboBoxType.setValue(entityTypes.get(0));
		mainLayout.add(comboBoxType);
	}

	private void on_buttonCreate_clicked() {
		try {
			Check.notNull(comboBoxType, "comboBoxType cannot be null");
			final EntityTypeConfig<?> selected = comboBoxType.getValue();
			if (selected == null) {
				CNotificationService.showWarning("Please select a type");
				return;
			}
			LOGGER.debug("Agile child create type selected: {}", selected.getDisplayName());
			onCreate.accept(selected);
			close();
		} catch (final Exception e) {
			LOGGER.error("Failed to start agile child creation reason={}", e.getMessage());
			CNotificationService.showException("Failed to start creation", e);
		}
	}
}
