package tech.derbent.bab.policybase.actionmask.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IComponentTransientPlaceHolder;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.actionmask.domain.ROutputActionMapping;
import tech.derbent.bab.policybase.actionmask.service.CPageServiceBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.filter.domain.ROutputStructure;

/** CAN action-mask component for mapping source filter outputs to destination CAN protocol variables. */
public class CComponentActionMaskOutputActionMappings extends CComponentBase<List<ROutputActionMapping>>
		implements IPageServiceAutoRegistrable, IComponentTransientPlaceHolder<CBabPolicyActionMaskCAN> {

	private record ROutputMappingGridRow(String outputName, String outputDataType, String destinationVariableName, String destinationDataType) {}

	public static final String COMPONENT_NAME = "outputActionMappings";
	private static final long serialVersionUID = 1L;

	private static Map<String, ROutputActionMapping> getMappingsByOutputName(final List<ROutputActionMapping> mappings) {
		if (mappings == null || mappings.isEmpty()) {
			return new LinkedHashMap<>();
		}
		final Map<String, ROutputActionMapping> mappingsByOutputName = new LinkedHashMap<>();
		mappings.stream().filter(mapping -> mapping != null && !mapping.outputName().isBlank())
				.forEach(mapping -> mappingsByOutputName.putIfAbsent(CBabPolicyFilterCAN.normalizeVariableName(mapping.outputName()), mapping));
		return mappingsByOutputName;
	}

	private static List<ROutputActionMapping> sanitizeMappings(final List<ROutputActionMapping> mappings) {
		if (mappings == null || mappings.isEmpty()) {
			return new ArrayList<>();
		}
		final Map<String, ROutputActionMapping> uniqueMappingsByOutput = new LinkedHashMap<>();
		mappings.stream().filter(mapping -> mapping != null && !mapping.outputName().isBlank()).forEach(mapping -> {
			final String outputKey = mapping.outputName().trim().toLowerCase(Locale.ROOT);
			uniqueMappingsByOutput.putIfAbsent(outputKey, mapping);
		});
		return new ArrayList<>(uniqueMappingsByOutput.values());
	}

	private final CButton buttonSetAsDestination;
	private final CButton buttonUnsetDestination;
	private CBabPolicyActionMaskCAN currentMask;
	private final CComboBox<CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationVariableCombo;
	private final CGrid<ROutputMappingGridRow> gridMappings;
	private final CSpan labelDestinationInfo;
	private final CSpan labelSelectedOutputInfo;
	private final CSpan labelStatus;
	private final CPageServiceBabPolicyActionMaskCAN pageService;

	public CComponentActionMaskOutputActionMappings(final CPageServiceBabPolicyActionMaskCAN pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		this.pageService = pageService;
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		labelStatus = new CSpan("No action mask selected.");
		labelStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
		gridMappings = new CGrid<>(ROutputMappingGridRow.class);
		CGrid.setupGrid(gridMappings);
		gridMappings.setHeight("220px");
		gridMappings.removeAllColumns();
		CGrid.styleColumnHeader(gridMappings.addColumn(ROutputMappingGridRow::outputName).setAutoWidth(true).setSortable(true), "Output Variable");
		CGrid.styleColumnHeader(gridMappings.addColumn(ROutputMappingGridRow::outputDataType).setAutoWidth(true).setSortable(true), "Output Type");
		CGrid.styleColumnHeader(gridMappings.addColumn(ROutputMappingGridRow::destinationVariableName).setAutoWidth(true).setSortable(true),
				"Destination Variable");
		CGrid.styleColumnHeader(gridMappings.addColumn(ROutputMappingGridRow::destinationDataType).setAutoWidth(true).setSortable(true),
				"Destination Type");
		gridMappings.asSingleSelect().addValueChangeListener(event -> refreshSelectionDetails());
		destinationVariableCombo = new CComboBox<>("Destination Protocol Variable");
		destinationVariableCombo.setAllowCustomValue(false);
		destinationVariableCombo.setClearButtonVisible(true);
		destinationVariableCombo.setWidthFull();
		destinationVariableCombo.setPlaceholder("Search destination variable...");
		destinationVariableCombo.setItemLabelGenerator(variable -> variable != null ? variable.name() : "");
		destinationVariableCombo.addValueChangeListener(event -> refreshSelectionDetails());
		buttonSetAsDestination = CButton.createPrimary("Set as Destination", VaadinIcon.CHECK.create(), this::on_buttonSetAsDestination_clicked);
		buttonUnsetDestination = CButton.createError("Unset", VaadinIcon.CLOSE_SMALL.create(), this::on_buttonUnsetDestination_clicked);
		labelSelectedOutputInfo = new CSpan("Select an output row.");
		labelSelectedOutputInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
		labelDestinationInfo = new CSpan("Select a destination variable.");
		labelDestinationInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
		final CHorizontalLayout actionRow = new CHorizontalLayout();
		actionRow.setPadding(false);
		actionRow.setSpacing(true);
		actionRow.setWidthFull();
		actionRow.add(destinationVariableCombo, buttonSetAsDestination, buttonUnsetDestination);
		actionRow.expand(destinationVariableCombo);
		final CHorizontalLayout detailsRow = new CHorizontalLayout();
		detailsRow.setPadding(false);
		detailsRow.setSpacing(true);
		detailsRow.setWidthFull();
		detailsRow.add(labelSelectedOutputInfo, labelDestinationInfo);
		detailsRow.expand(labelSelectedOutputInfo);
		final CVerticalLayout editorLayout = new CVerticalLayout();
		editorLayout.setPadding(false);
		editorLayout.setSpacing(false);
		editorLayout.setWidthFull();
		editorLayout.add(actionRow, detailsRow);
		add(labelStatus, gridMappings, editorLayout);
		refreshComponent();
	}

	@Override
	public String getComponentName() { return COMPONENT_NAME; }

	private List<CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> getDestinationVariables() {
		return pageService.getDestinationProtocolVariables(currentMask);
	}

	private List<ROutputStructure> getSourceOutputStructure() { return pageService.getSourceFilterOutputStructure(currentMask); }

	private boolean isCompatibleDataType(final String sourceDataType, final String destinationDataType) {
		final String normalizedSourceType = CBabPolicyFilterCAN.normalizeDataType(sourceDataType);
		final String normalizedDestinationType = CBabPolicyFilterCAN.normalizeDataType(destinationDataType);
		return !normalizedSourceType.isBlank() && !normalizedDestinationType.isBlank()
				&& normalizedSourceType.equalsIgnoreCase(normalizedDestinationType);
	}

	private void on_buttonSetAsDestination_clicked(@SuppressWarnings ("unused") final com.vaadin.flow.component.ClickEvent<Button> event) {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		if (selectedRow == null) {
			CNotificationService.showWarning("Select an output variable row first");
			return;
		}
		final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable selectedDestination = destinationVariableCombo.getValue();
		if (selectedDestination == null || selectedDestination.name().isBlank()) {
			CNotificationService.showWarning("Select a destination protocol variable first");
			return;
		}
		if (!isCompatibleDataType(selectedRow.outputDataType(), selectedDestination.dataType())) {
			CNotificationService.showWarning("Output and destination data types must match before setting mapping");
			return;
		}
		final Map<String, ROutputActionMapping> mappingsByOutputName = getMappingsByOutputName(getValue());
		final String outputKey = CBabPolicyFilterCAN.normalizeVariableName(selectedRow.outputName());
		mappingsByOutputName.put(outputKey, new ROutputActionMapping(selectedRow.outputName(), selectedRow.outputDataType(),
				selectedDestination.name(), selectedDestination.dataType()));
		updateValueFromClient(new ArrayList<>(mappingsByOutputName.values()));
		CNotificationService.showSaveSuccess();
	}

	private void on_buttonUnsetDestination_clicked(@SuppressWarnings ("unused") final com.vaadin.flow.component.ClickEvent<Button> event) {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		if (selectedRow == null) {
			CNotificationService.showWarning("Select an output variable row first");
			return;
		}
		final Map<String, ROutputActionMapping> mappingsByOutputName = getMappingsByOutputName(getValue());
		final String outputKey = CBabPolicyFilterCAN.normalizeVariableName(selectedRow.outputName());
		if (mappingsByOutputName.remove(outputKey) == null) {
			CNotificationService.showInfo("Selected output row is already unset");
			return;
		}
		updateValueFromClient(new ArrayList<>(mappingsByOutputName.values()));
		CNotificationService.showSaveSuccess();
	}

	@Override
	protected void onValueChanged(final List<ROutputActionMapping> oldValue, final List<ROutputActionMapping> newValue, final boolean fromClient) {
		super.onValueChanged(oldValue, newValue, fromClient);
		if (currentMask != null) {
			currentMask.setOutputActionMappings(newValue);
		}
		refreshComponent();
	}

	private void refreshButtonStates() {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable selectedDestination = destinationVariableCombo.getValue();
		final boolean hasSelection = selectedRow != null;
		buttonSetAsDestination.setEnabled(hasSelection && selectedDestination != null);
		buttonUnsetDestination.setEnabled(hasSelection && selectedRow != null && selectedRow.destinationVariableName() != null
				&& !selectedRow.destinationVariableName().isBlank());
	}

	@Override
	protected void refreshComponent() {
		if (currentMask == null) {
			gridMappings.setItems(List.of());
			destinationVariableCombo.setItems(List.of());
			destinationVariableCombo.clear();
			labelStatus.setText("No CAN action mask selected.");
			labelSelectedOutputInfo.setText("Select an output row.");
			labelDestinationInfo.setText("Select a destination variable.");
			refreshButtonStates();
			return;
		}
		final List<ROutputStructure> sourceOutputs = getSourceOutputStructure();
		final List<CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationVariables = getDestinationVariables();
		destinationVariableCombo.setItems(destinationVariables);
		if (sourceOutputs.isEmpty()) {
			gridMappings.setItems(List.of());
			labelStatus.setText("No source CAN filter outputs available. Select protocol variables in the source filter first.");
			refreshButtonStates();
			return;
		}
		final Map<String, CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationByName =
				destinationVariables.stream().filter(variable -> variable != null && !variable.name().isBlank()).collect(LinkedHashMap::new,
						(map, variable) -> map.putIfAbsent(CBabPolicyFilterCAN.normalizeVariableName(variable.name()), variable), Map::putAll);
		final Map<String, ROutputActionMapping> mappingsByOutputName = getMappingsByOutputName(getValue());
		final String previousSelectedOutputName =
				gridMappings.asSingleSelect().getValue() != null ? gridMappings.asSingleSelect().getValue().outputName() : null;
		final List<ROutputMappingGridRow> rows = sourceOutputs.stream().filter(output -> output != null && !output.name().isBlank()).map(output -> {
			final ROutputActionMapping mapping = mappingsByOutputName.get(CBabPolicyFilterCAN.normalizeVariableName(output.name()));
			if (mapping == null) {
				return new ROutputMappingGridRow(output.name(), output.dataType(), "", "");
			}
			final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable destinationVariable =
					destinationByName.get(CBabPolicyFilterCAN.normalizeVariableName(mapping.targetProtocolVariableName()));
			final String destinationName = destinationVariable != null ? destinationVariable.name() : mapping.targetProtocolVariableName();
			final String destinationType = destinationVariable != null ? destinationVariable.dataType() : mapping.targetProtocolVariableDataType();
			return new ROutputMappingGridRow(output.name(), output.dataType(), destinationName, destinationType);
		}).sorted(Comparator.comparing(ROutputMappingGridRow::outputName, Comparator.nullsLast(String::compareToIgnoreCase))).toList();
		gridMappings.setItems(rows);
		if (previousSelectedOutputName != null && !previousSelectedOutputName.isBlank()) {
			rows.stream().filter(row -> previousSelectedOutputName.equalsIgnoreCase(row.outputName())).findFirst()
					.ifPresent(row -> gridMappings.asSingleSelect().setValue(row));
		}
		final long mappedCount =
				rows.stream().filter(row -> row.destinationVariableName() != null && !row.destinationVariableName().isBlank()).count();
		labelStatus.setText("Mapped outputs: " + mappedCount + " / " + rows.size());
		refreshSelectionDetails();
	}

	private void refreshSelectionDetails() {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		if (selectedRow == null) {
			labelSelectedOutputInfo.setText("Select an output row.");
		} else {
			labelSelectedOutputInfo.setText("Selected Output: %s (%s)".formatted(selectedRow.outputName(),
					selectedRow.outputDataType() == null ? "" : selectedRow.outputDataType()));
		}
		final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable selectedDestination = destinationVariableCombo.getValue();
		if (selectedDestination == null) {
			labelDestinationInfo.setText("Select a destination variable.");
		} else {
			labelDestinationInfo.setText("Destination Type: %s (%s)".formatted(selectedDestination.name(), selectedDestination.dataType()));
			if (selectedRow != null && !isCompatibleDataType(selectedRow.outputDataType(), selectedDestination.dataType())) {
				labelDestinationInfo.setText(labelDestinationInfo.getText() + " [type mismatch]");
			}
		}
		refreshButtonStates();
	}

	@Override
	public void setThis(final CBabPolicyActionMaskCAN value) {
		currentMask = value;
		setValue(sanitizeMappings(value == null ? List.of() : value.getOutputActionMappings()));
	}
}
