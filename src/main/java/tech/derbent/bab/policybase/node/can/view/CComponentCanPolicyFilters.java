package tech.derbent.bab.policybase.node.can.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCANService;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;

/** Node-scoped CAN policy filter manager component. */
public class CComponentCanPolicyFilters extends CComponentBase<CBabCanNode>
		implements IPageServiceAutoRegistrable, IComponentTransientPlaceHolder<CBabCanNode> {

	private static final String COMPONENT_NAME = "canPolicyFilters";
	private static final String DEFAULT_FILTER_NAME = "CAN Filter";
	public static final String ID_COMBO = "custom-can-filter-combo";
	public static final String ID_DELETE_BUTTON = "custom-can-filter-delete-button";
	public static final String ID_EDIT_BUTTON = "custom-can-filter-edit-button";
	public static final String ID_NEW_BUTTON = "custom-can-filter-new-button";
	public static final String ID_ROOT = "custom-can-filter-component";
	public static final String ID_STATUS = "custom-can-filter-status";
	private static final long serialVersionUID = 1L;
	private final CButton buttonDelete;
	private final CButton buttonEdit;
	private final CButton buttonNew;
	private final CBabCanNodeService canNodeService;
	private final ComboBox<CBabPolicyFilterCAN> comboFilters;
	private final CBabPolicyFilterCANService filterService;
	private final CSpan labelStatus;

	public CComponentCanPolicyFilters(final CBabPolicyFilterCANService filterService, final CBabCanNodeService canNodeService) {
		Check.notNull(filterService, "CBabPolicyFilterCANService cannot be null");
		Check.notNull(canNodeService, "CBabCanNodeService cannot be null");
		this.filterService = filterService;
		this.canNodeService = canNodeService;
		setId(ID_ROOT);
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", CUIConstants.GAP_TINY);

		comboFilters = new ComboBox<>("CAN Message Filter");
		comboFilters.setId(ID_COMBO);
		comboFilters.setItemLabelGenerator(filter -> filter != null ? filter.getName() : "");
		comboFilters.addValueChangeListener(event -> updateActionState());
		comboFilters.setWidthFull();

		buttonNew = CButton.createPrimary("New", VaadinIcon.PLUS.create(), this::on_buttonNew_clicked);
		buttonNew.setId(ID_NEW_BUTTON);
		buttonEdit = CButton.createTertiary("Edit", VaadinIcon.EDIT.create(), this::on_buttonEdit_clicked);
		buttonEdit.setId(ID_EDIT_BUTTON);
		buttonDelete = CButton.createError("Delete", VaadinIcon.TRASH.create(), this::on_buttonDelete_clicked);
		buttonDelete.setId(ID_DELETE_BUTTON);

		final CHorizontalLayout actionRow = new CHorizontalLayout(comboFilters, buttonNew, buttonEdit, buttonDelete);
		actionRow.setWidthFull();
		actionRow.setDefaultVerticalComponentAlignment(Alignment.END);
		actionRow.setSpacing(true);

		labelStatus = new CSpan();
		labelStatus.setId(ID_STATUS);

		add(actionRow, labelStatus);
		refreshComponent();
	}

	@Override
	public String getComponentName() { return COMPONENT_NAME; }

	private CBabCanNode getCurrentNode() { return getValue(); }

	private void on_buttonDelete_clicked(@SuppressWarnings ("unused") final ClickEvent<Button> event) {
		final CBabCanNode node = getCurrentNode();
		final CBabPolicyFilterCAN selected = comboFilters.getValue();
		if (node == null || selected == null) {
			return;
		}
		try {
			CNotificationService.showConfirmationDialog("Delete filter '" + selected.getName() + "' from node '" + node.getName() + "'?", () -> {
				try {
					filterService.delete(selected);
					comboFilters.clear();
					refreshComponent();
					updateValueFromClient(node);
					CNotificationService.showDeleteSuccess();
				} catch (final Exception e) {
					CNotificationService.showException("Failed to delete CAN filter", e);
				}
			});
		} catch (final Exception e) {
			CNotificationService.showException("Failed to show filter delete confirmation", e);
		}
	}

	private void on_buttonEdit_clicked(@SuppressWarnings ("unused") final ClickEvent<Button> event) {
		final CBabCanNode node = getCurrentNode();
		final CBabPolicyFilterCAN selected = comboFilters.getValue();
		if (node == null || node.getId() == null || selected == null) {
			return;
		}
		openFilterDialog(node, selected, false);
	}

	private void on_buttonNew_clicked(@SuppressWarnings ("unused") final ClickEvent<Button> event) {
		final CBabCanNode node = getCurrentNode();
		if (node == null || node.getId() == null) {
			CNotificationService.showWarning("Save CAN node before creating filters.");
			return;
		}
		final CBabPolicyFilterCAN newFilter = new CBabPolicyFilterCAN(generateDefaultFilterName(node), node);
		openFilterDialog(node, newFilter, true);
	}

	@Override
	protected void onValueChanged(final CBabCanNode oldValue, final CBabCanNode newValue, final boolean fromClient) {
		super.onValueChanged(oldValue, newValue, fromClient);
		refreshComponent();
	}

	private void openFilterDialog(final CBabCanNode node, final CBabPolicyFilterCAN filter, final boolean isNew) {
		final List<String> candidates = resolveProtocolVariableCandidates(node, filter);
		final Consumer<CBabPolicyFilterCAN> onSave = savedFilter -> {
			savedFilter.setParentNode(node);
			filterService.save(savedFilter);
			refreshComponent();
			if (savedFilter.getId() != null) {
				filterService.getById(savedFilter.getId()).ifPresent(comboFilters::setValue);
			}
			updateValueFromClient(node);
		};
		try {
			new CDialogCanPolicyFilter(filter, candidates, onSave, isNew).open();
		} catch (final Exception e) {
			CNotificationService.showException("Failed to open CAN filter dialog", e);
		}
	}

	@Override
	protected void refreshComponent() {
		final CBabCanNode node = getCurrentNode();
		final CBabPolicyFilterCAN previouslySelected = comboFilters.getValue();
		final Long previousId = previouslySelected != null ? previouslySelected.getId() : null;
		if (node == null) {
			comboFilters.setItems(List.of());
			comboFilters.clear();
			comboFilters.setEnabled(false);
			buttonNew.setEnabled(false);
			buttonEdit.setEnabled(false);
			buttonDelete.setEnabled(false);
			labelStatus.setText("No CAN node selected.");
			labelStatus.getStyle().set("color", "var(--lumo-error-text-color)");
			return;
		}
		if (node.getId() == null) {
			comboFilters.setItems(List.of());
			comboFilters.clear();
			comboFilters.setEnabled(false);
			buttonNew.setEnabled(false);
			buttonEdit.setEnabled(false);
			buttonDelete.setEnabled(false);
			labelStatus.setText("Save CAN node to manage node-owned filters.");
			labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
			return;
		}

		final List<CBabPolicyFilterCAN> filters = new ArrayList<>(filterService.listByParentNode(node));
		filters.sort(Comparator.comparing(CBabPolicyFilterCAN::getExecutionOrder, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(CBabPolicyFilterCAN::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
		comboFilters.setItems(filters);
		if (previousId != null) {
			filters.stream().filter(filter -> Objects.equals(filter.getId(), previousId)).findFirst().ifPresent(comboFilters::setValue);
		}
		if (comboFilters.getValue() == null && !filters.isEmpty()) {
			comboFilters.setValue(filters.get(0));
		}

		labelStatus.setText("Loaded " + filters.size() + " CAN filter(s) for node '" + node.getName() + "'.");
		labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
		updateActionState();
	}

	private List<String> resolveProtocolVariableCandidates(final CBabCanNode node, final CBabPolicyFilterCAN filter) {
		final Set<String> variableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		String protocolJson = node.getProtocolFileJson();
		if ((protocolJson == null || protocolJson.isBlank()) && node.getId() != null) {
			protocolJson = canNodeService.loadProtocolContentFromDb(node.getId(), CBabCanNodeService.EProtocolContentField.JSON);
		}
		variableNames.addAll(canNodeService.extractProtocolVariableNames(protocolJson));
		if (filter != null && filter.getProtocolVariableNames() != null) {
			variableNames.addAll(filter.getProtocolVariableNames());
		}
		return List.copyOf(variableNames);
	}

	@Override
	public void setThis(final CBabCanNode value) {
		setValue(value);
	}

	private void updateActionState() {
		final CBabCanNode node = getCurrentNode();
		final CBabPolicyFilterCAN selected = comboFilters.getValue();
		final boolean nodeReady = node != null && node.getId() != null;
		final boolean canCreate = nodeReady && !isReadOnly();
		comboFilters.setEnabled(nodeReady);
		buttonNew.setEnabled(canCreate);
		buttonEdit.setEnabled(canCreate && selected != null);
		buttonDelete.setEnabled(canCreate && selected != null);
	}

	private String generateDefaultFilterName(final CBabCanNode node) {
		final List<CBabPolicyFilterCAN> existingFilters = filterService.listByParentNode(node);
		final Set<String> existingNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		existingFilters.stream().map(CBabPolicyFilterCAN::getName).filter(Objects::nonNull).forEach(existingNames::add);
		if (!existingNames.contains(DEFAULT_FILTER_NAME)) {
			return DEFAULT_FILTER_NAME;
		}
		int index = 2;
		String candidate = DEFAULT_FILTER_NAME + " " + index;
		while (existingNames.contains(candidate)) {
			index++;
			candidate = DEFAULT_FILTER_NAME + " " + index;
		}
		return candidate;
	}

	private static final class CDialogCanPolicyFilter extends CDialogDBEdit<CBabPolicyFilterCAN> {

		private static final long serialVersionUID = 1L;
		private final TextField fieldCanFrameRegex;
		private final TextField fieldCanPayloadRegex;
		private final TextArea fieldDescription;
		private final Checkbox fieldEnabled;
		private final IntegerField fieldExecutionOrder;
		private final TextField fieldName;
		private final MultiSelectComboBox<String> fieldProtocolVariables;
		private final Checkbox fieldRequireExtendedFrame;

		private CDialogCanPolicyFilter(final CBabPolicyFilterCAN filter, final List<String> protocolVariables,
				final Consumer<CBabPolicyFilterCAN> onSave, final boolean isNew) throws Exception {
			super(filter, onSave, isNew);
			fieldName = new TextField("Name");
			fieldName.setWidthFull();
			fieldDescription = new TextArea("Description");
			fieldDescription.setWidthFull();
			fieldDescription.setHeight(CUIConstants.TEXTAREA_HEIGHT_STANDARD);
			fieldCanFrameRegex = new TextField("CAN Frame-ID Regex");
			fieldCanFrameRegex.setWidthFull();
			fieldCanPayloadRegex = new TextField("CAN Payload Regex");
			fieldCanPayloadRegex.setWidthFull();
			fieldRequireExtendedFrame = new Checkbox("Require Extended Frame");
			fieldExecutionOrder = new IntegerField("Execution Order");
			fieldExecutionOrder.setMin(0);
			fieldEnabled = new Checkbox("Enabled");
			fieldProtocolVariables = new MultiSelectComboBox<>("Protocol Variables");
			fieldProtocolVariables.setItems(protocolVariables);
			fieldProtocolVariables.setWidthFull();
			if (protocolVariables.isEmpty()) {
				fieldProtocolVariables.setHelperText("No protocol variables found in parent CAN node protocol JSON.");
			}
			setupDialog();
			populateForm();
		}

		@Override
		public String getDialogTitleString() { return isNew ? "New CAN Filter" : "Edit CAN Filter"; }

		@Override
		protected Icon getFormIcon() throws Exception { return VaadinIcon.FILTER.create(); }

		@Override
		protected String getFormTitleString() { return isNew ? "Create CAN Message Filter" : "Update CAN Message Filter"; }

		@Override
		protected String getSuccessCreateMessage() { return "CAN filter created successfully"; }

		@Override
		protected String getSuccessUpdateMessage() { return "CAN filter updated successfully"; }

		@Override
		protected void populateForm() {
			final CBabPolicyFilterCAN filter = getEntity();
			fieldName.setValue(filter.getName() != null ? filter.getName() : "");
			fieldDescription.setValue(filter.getDescription() != null ? filter.getDescription() : "");
			fieldCanFrameRegex.setValue(filter.getCanFrameIdRegularExpression());
			fieldCanPayloadRegex.setValue(filter.getCanPayloadRegularExpression());
			fieldRequireExtendedFrame.setValue(Boolean.TRUE.equals(filter.getRequireExtendedFrame()));
			fieldEnabled.setValue(Boolean.TRUE.equals(filter.getIsEnabled()));
			fieldExecutionOrder.setValue(filter.getExecutionOrder());
			fieldProtocolVariables
					.setValue(new LinkedHashSet<>(filter.getProtocolVariableNames() != null ? filter.getProtocolVariableNames() : List.of()));
		}

		@Override
		protected void save() throws Exception {
			applyFormValues();
			super.save();
		}

		@Override
		protected void setupContent() throws Exception {
			super.setupContent();
			final CVerticalLayout formLayout = new CVerticalLayout();
			formLayout.setPadding(false);
			formLayout.setSpacing(true);
			formLayout.setWidthFull();
			formLayout.add(fieldName, fieldDescription, fieldCanFrameRegex, fieldCanPayloadRegex, fieldProtocolVariables);
			formLayout.add(new CHorizontalLayout(fieldExecutionOrder, fieldEnabled, fieldRequireExtendedFrame));
			getDialogLayout().add(formLayout);
		}

		@Override
		protected void validateForm() {
			Check.notBlank(fieldName.getValue(), "Filter name is required");
			Check.notBlank(fieldCanFrameRegex.getValue(), "CAN Frame-ID regex is required");
			Check.notBlank(fieldCanPayloadRegex.getValue(), "CAN payload regex is required");
		}

		private void applyFormValues() {
			final CBabPolicyFilterCAN filter = getEntity();
			filter.setName(fieldName.getValue().trim());
			filter.setDescription(fieldDescription.getValue());
			filter.setCanFrameIdRegularExpression(fieldCanFrameRegex.getValue());
			filter.setCanPayloadRegularExpression(fieldCanPayloadRegex.getValue());
			filter.setProtocolVariableNames(new ArrayList<>(fieldProtocolVariables.getValue()));
			filter.setExecutionOrder(fieldExecutionOrder.getValue() != null ? fieldExecutionOrder.getValue() : 0);
			filter.setIsEnabled(fieldEnabled.getValue());
			filter.setRequireExtendedFrame(fieldRequireExtendedFrame.getValue());
			filter.setCanNodeEnabled(true);
			filter.setModbusNodeEnabled(false);
			filter.setFileNodeEnabled(false);
			filter.setHttpNodeEnabled(false);
			filter.setRosNodeEnabled(false);
			filter.setSyslogNodeEnabled(false);
			filter.setLogicOperator(CBabPolicyFilterBase.LOGIC_OPERATOR_AND);
			filter.setNullHandling(CBabPolicyFilterBase.NULL_HANDLING_IGNORE);
		}
	}
}
