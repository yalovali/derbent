package tech.derbent.bab.policybase.actionmask.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentActionMaskOutputActionMappings.class);
	private record ROutputMappingGridRow(String outputName, String outputDataType, String destinationVariableName, String destinationDataType) {}

	public static final String COMPONENT_NAME = "outputActionMappings";
	private static final long serialVersionUID = 1L;
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
		gridMappings.asSingleSelect().addValueChangeListener(event -> {
			LOGGER.trace("Output row selection changed fromClient={} selectedOutput={}",
					event.isFromClient(), event.getValue() != null ? event.getValue().outputName() : null);
			refreshSelectionDetails();
		});
		destinationVariableCombo = new CComboBox<>("Destination Protocol Variable");
		destinationVariableCombo.setAllowCustomValue(false);
		destinationVariableCombo.setClearButtonVisible(true);
		destinationVariableCombo.setWidthFull();
		destinationVariableCombo.setPlaceholder("Search destination variable...");
		destinationVariableCombo.setItemLabelGenerator(variable -> variable != null ? variable.name() : "");
		destinationVariableCombo.addValueChangeListener(event -> {
			LOGGER.trace("Destination variable selection changed fromClient={} selectedDestination={}",
					event.isFromClient(), event.getValue() != null ? event.getValue().name() : null);
			refreshSelectionDetails();
		});
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

	private List<CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> getDestinationVariables() {
		return pageService.getDestinationProtocolVariables(currentMask);
	}

	private CBabPolicyActionMaskCAN requirePersistedMaskContext() {
		final CBabPolicyActionMaskCAN activeMask = currentMask != null ? currentMask : pageService.getCurrentMask();
		Check.notNull(activeMask, "No active CAN action mask context while updating output mappings");
		Check.notNull(activeMask.getId(), "CAN action mask must be saved before updating output mappings");
		return activeMask;
	}

	@Override
	public String getComponentName() { return COMPONENT_NAME; }

	private List<ROutputStructure> getSourceOutputStructure() {
		return pageService.getSourceFilterOutputStructure(currentMask);
	}

	private boolean isCompatibleDataType(final String sourceDataType, final String destinationDataType) {
		final String normalizedSourceType = CBabPolicyFilterCAN.normalizeDataType(sourceDataType);
		final String normalizedDestinationType = CBabPolicyFilterCAN.normalizeDataType(destinationDataType);
		return !normalizedSourceType.isBlank() && !normalizedDestinationType.isBlank()
				&& normalizedSourceType.equalsIgnoreCase(normalizedDestinationType);
	}

	private void on_buttonSetAsDestination_clicked(@SuppressWarnings ("unused") final com.vaadin.flow.component.ClickEvent<Button> event) {
		applySelectedDestinationMapping();
	}

	private void applySelectedDestinationMapping() {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		Check.notNull(selectedRow, "Cannot set output mapping: no output row is selected");
		final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable selectedDestination = destinationVariableCombo.getValue();
		Check.notNull(selectedDestination, "Cannot set output mapping: no destination protocol variable is selected");
		Check.isTrue(!selectedDestination.name().isBlank(),
				"Cannot set output mapping: destination protocol variable name is blank");
		Check.isTrue(isCompatibleDataType(selectedRow.outputDataType(), selectedDestination.dataType()),
				"Cannot set output mapping: output and destination data types do not match");
		final Map<String, ROutputActionMapping> mappingsByOutputName = getMappingsByOutputName(getValue());
		final String outputKey = CBabPolicyFilterCAN.normalizeVariableName(selectedRow.outputName());
		mappingsByOutputName.put(outputKey,
				new ROutputActionMapping(selectedRow.outputName(), selectedRow.outputDataType(), selectedDestination.name(),
						selectedDestination.dataType()));
		final CBabPolicyActionMaskCAN activeMask = requirePersistedMaskContext();
		LOGGER.info("Set output mapping maskId={} output='{}' destination='{}' newMappingCount={}",
				activeMask.getId(), selectedRow.outputName(), selectedDestination.name(), mappingsByOutputName.size());
		final List<ROutputActionMapping> newMappings = new ArrayList<>(mappingsByOutputName.values());
		try {
			final List<ROutputActionMapping> persistedMappings = pageService.persistOutputActionMappings(activeMask, newMappings);
			setValue(sanitizeMappings(persistedMappings));
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to persist output mapping immediately", e);
		}
		CNotificationService.showSaveSuccess();
	}

	private void on_buttonUnsetDestination_clicked(@SuppressWarnings ("unused") final com.vaadin.flow.component.ClickEvent<Button> event) {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		Check.notNull(selectedRow, "Cannot unset output mapping: no output row is selected");
		final Map<String, ROutputActionMapping> mappingsByOutputName = getMappingsByOutputName(getValue());
		final String outputKey = CBabPolicyFilterCAN.normalizeVariableName(selectedRow.outputName());
		Check.notNull(mappingsByOutputName.remove(outputKey),
				"Cannot unset output mapping: selected output row has no mapping");
		final CBabPolicyActionMaskCAN activeMask = requirePersistedMaskContext();
		LOGGER.info("Unset output mapping maskId={} output='{}' newMappingCount={}",
				activeMask.getId(), selectedRow.outputName(), mappingsByOutputName.size());
		final List<ROutputActionMapping> newMappings = new ArrayList<>(mappingsByOutputName.values());
		try {
			final List<ROutputActionMapping> persistedMappings = pageService.persistOutputActionMappings(activeMask, newMappings);
			setValue(sanitizeMappings(persistedMappings));
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to persist output mapping unset immediately", e);
		}
		CNotificationService.showSaveSuccess();
	}

	private void refreshButtonStates() {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable selectedDestination = destinationVariableCombo.getValue();
		final boolean hasSelection = selectedRow != null;
		buttonSetAsDestination.setEnabled(hasSelection && selectedDestination != null);
		buttonUnsetDestination.setEnabled(hasSelection && selectedRow.destinationVariableName() != null
				&& !selectedRow.destinationVariableName().isBlank());
	}

	@Override
	protected void onValueChanged(final List<ROutputActionMapping> oldValue, final List<ROutputActionMapping> newValue, final boolean fromClient) {
		super.onValueChanged(oldValue, newValue, fromClient);
		if (!fromClient && Objects.equals(oldValue, newValue)) {
			LOGGER.trace("Ignoring redundant server-side output mapping update maskId={} count={}",
					currentMask != null ? currentMask.getId() : null, newValue != null ? newValue.size() : 0);
			return;
		}
		LOGGER.trace("Output mappings value changed maskId={} fromClient={} oldCount={} newCount={}",
				currentMask != null ? currentMask.getId() : null, fromClient, oldValue != null ? oldValue.size() : 0,
				newValue != null ? newValue.size() : 0);
		refreshComponent();
	}

	@Override
	protected void refreshComponent() {
		if (currentMask == null) {
			resetForNoMask();
			return;
		}
		final List<ROutputStructure> sourceOutputs;
		final List<CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationVariables;
		try {
			sourceOutputs = getSourceOutputStructure();
			destinationVariables = getDestinationVariables();
		} catch (final RuntimeException e) {
			LOGGER.warn("Output-action mappings refresh downgraded to warning maskId={} actionId={} reason={}",
					currentMask.getId(),
					currentMask.getPolicyAction() != null ? currentMask.getPolicyAction().getId() : null,
					e.getMessage());
			applyUnavailableState("Output mappings are unavailable for current destination/mask setup.");
			CNotificationService.showWarning("Output mappings are unavailable for current destination/mask setup.");
			return;
		}
		final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable previousDestinationSelection = destinationVariableCombo.getValue();
		final Map<String, ROutputActionMapping> mappingsByOutputName = getMappingsByOutputName(getValue());
		LOGGER.trace("Refreshing output-action mappings component maskId={} actionId={} destinationNodeId={} sourceOutputs={} destinationVariables={}",
				currentMask.getId(),
				currentMask.getPolicyAction() != null ? currentMask.getPolicyAction().getId() : null,
				currentMask.getDestinationNode() != null ? currentMask.getDestinationNode().getId() : null,
				sourceOutputs.size(), destinationVariables.size());
		validateDestinationVariables(destinationVariables);
		destinationVariableCombo.setItems(destinationVariables);
		restoreDestinationSelection(destinationVariables, previousDestinationSelection);
		final Map<String, CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationByName =
				buildDestinationByName(destinationVariables);

		if (sourceOutputs.isEmpty()) {
			applyRowsForEmptySourceOutputs(mappingsByOutputName, destinationByName);
			return;
		}

		final String previousSelectedOutputName = getSelectedOutputName();
		final List<ROutputMappingGridRow> rowsFromSource = buildRowsFromSourceOutputs(sourceOutputs, mappingsByOutputName, destinationByName);
		final List<ROutputMappingGridRow> mergedRows = mergeRowsWithSavedMappings(rowsFromSource, mappingsByOutputName, destinationByName);
		applyGridRows(mergedRows, previousSelectedOutputName);
		updateMappedStatus(mergedRows);
		refreshSelectionDetails();
	}

	private void applyGridRows(final List<ROutputMappingGridRow> rows, final String previousSelectedOutputName) {
		gridMappings.setItems(rows);
		if (previousSelectedOutputName == null || previousSelectedOutputName.isBlank()) {
			return;
		}
		rows.stream()
				.filter(row -> previousSelectedOutputName.equalsIgnoreCase(row.outputName()))
				.findFirst()
				.ifPresent(row -> gridMappings.asSingleSelect().setValue(row));
	}

	private void applyRowsForEmptySourceOutputs(final Map<String, ROutputActionMapping> mappingsByOutputName,
			final Map<String, CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationByName) {
		if (mappingsByOutputName.isEmpty()) {
			gridMappings.setItems(List.of());
			labelStatus.setText("No source CAN filter outputs available. Select protocol variables in the source filter first.");
			refreshButtonStates();
			return;
		}
		final List<ROutputMappingGridRow> rows = mappingsByOutputName.values().stream()
				.map(mapping -> createGridRowFromMapping(mapping, destinationByName))
				.sorted(Comparator.comparing(ROutputMappingGridRow::outputName, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
		gridMappings.setItems(rows);
		labelStatus.setText("Showing saved mappings. Source CAN filter outputs are currently unavailable.");
		refreshSelectionDetails();
	}

	private List<ROutputMappingGridRow> buildRowsFromSourceOutputs(final List<ROutputStructure> sourceOutputs,
			final Map<String, ROutputActionMapping> mappingsByOutputName,
			final Map<String, CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationByName) {
		return sourceOutputs.stream()
				.filter(output -> output != null && !output.name().isBlank())
				.map(output -> {
					final ROutputActionMapping mapping = mappingsByOutputName.get(CBabPolicyFilterCAN.normalizeVariableName(output.name()));
					if (mapping == null) {
						return new ROutputMappingGridRow(output.name(), output.dataType(), "", "");
					}
					return createGridRowForOutputAndMapping(output, mapping, destinationByName);
				})
				.sorted(Comparator.comparing(ROutputMappingGridRow::outputName, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	private ROutputMappingGridRow createGridRowForOutputAndMapping(final ROutputStructure output,
			final ROutputActionMapping mapping,
			final Map<String, CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationByName) {
		final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable destinationVariable =
				destinationByName.get(CBabPolicyFilterCAN.normalizeVariableName(mapping.targetProtocolVariableName()));
		final String destinationName = destinationVariable != null ? destinationVariable.name() : mapping.targetProtocolVariableName();
		final String destinationType = destinationVariable != null ? destinationVariable.dataType() : mapping.targetProtocolVariableDataType();
		return new ROutputMappingGridRow(output.name(), output.dataType(), destinationName, destinationType);
	}

	private String getSelectedOutputName() {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		return selectedRow != null ? selectedRow.outputName() : null;
	}

	private List<ROutputMappingGridRow> mergeRowsWithSavedMappings(final List<ROutputMappingGridRow> rowsFromSource,
			final Map<String, ROutputActionMapping> mappingsByOutputName,
			final Map<String, CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationByName) {
		// Preserve saved mappings that are no longer present in source outputs.
		final Map<String, ROutputMappingGridRow> rowsByOutputKey = new LinkedHashMap<>();
		rowsFromSource.forEach(row -> rowsByOutputKey.putIfAbsent(CBabPolicyFilterCAN.normalizeVariableName(row.outputName()), row));
		mappingsByOutputName.values().forEach(mapping -> rowsByOutputKey.computeIfAbsent(
				CBabPolicyFilterCAN.normalizeVariableName(mapping.outputName()),
				unused -> createGridRowFromMapping(mapping, destinationByName)));
		return rowsByOutputKey.values().stream()
				.sorted(Comparator.comparing(ROutputMappingGridRow::outputName, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	private void resetForNoMask() {
		gridMappings.setItems(List.of());
		destinationVariableCombo.setItems(List.of());
		destinationVariableCombo.clear();
		labelStatus.setText("No CAN action mask selected.");
		labelSelectedOutputInfo.setText("Select an output row.");
		labelDestinationInfo.setText("Select a destination variable.");
		refreshButtonStates();
	}

	private void applyUnavailableState(final String statusText) {
		gridMappings.setItems(List.of());
		destinationVariableCombo.setItems(List.of());
		destinationVariableCombo.clear();
		labelStatus.setText(statusText);
		labelSelectedOutputInfo.setText("Select an output row.");
		labelDestinationInfo.setText("Select a destination variable.");
		refreshButtonStates();
	}

	private void restoreDestinationSelection(
			final List<CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationVariables,
			final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable previousDestinationSelection) {
		if (previousDestinationSelection == null || previousDestinationSelection.name() == null
				|| previousDestinationSelection.name().isBlank()) {
			return;
		}
		destinationVariables.stream()
				.filter(variable -> variable != null && variable.name() != null
						&& variable.name().equalsIgnoreCase(previousDestinationSelection.name()))
				.findFirst()
				.ifPresent(destinationVariableCombo::setValue);
	}

	private void updateMappedStatus(final List<ROutputMappingGridRow> rows) {
		final long mappedCount = rows.stream()
				.filter(row -> row.destinationVariableName() != null && !row.destinationVariableName().isBlank())
				.count();
		labelStatus.setText("Mapped outputs: " + mappedCount + " / " + rows.size());
	}

	private void validateDestinationVariables(
			final List<CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationVariables) {
		if (currentMask.getDestinationNode() == null || !destinationVariables.isEmpty()) {
			return;
		}
		LOGGER.warn("Destination protocol variable list is empty maskId={} actionId={} destinationNodeId={}",
				currentMask.getId(),
				currentMask.getPolicyAction() != null ? currentMask.getPolicyAction().getId() : null,
				currentMask.getDestinationNode().getId());
		CNotificationService.showWarning("No destination protocol variables are available for this destination node.");
	}

	private void refreshSelectionDetails() {
		final ROutputMappingGridRow selectedRow = gridMappings.asSingleSelect().getValue();
		if (selectedRow == null) {
			labelSelectedOutputInfo.setText("Select an output row.");
		} else {
			labelSelectedOutputInfo.setText(
					"Selected Output: %s (%s)".formatted(selectedRow.outputName(), selectedRow.outputDataType() == null ? "" : selectedRow.outputDataType()));
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
		final Long previousMaskId = currentMask != null ? currentMask.getId() : null;
		currentMask = value;
		final Long currentMaskId = currentMask != null ? currentMask.getId() : null;
		final List<ROutputActionMapping> sanitizedMappings = sanitizeMappings(value == null ? List.of() : value.getOutputActionMappings());
		LOGGER.trace("setThis output mappings component previousMaskId={} currentMaskId={} mappingCount={}",
				previousMaskId, currentMaskId, sanitizedMappings.size());
		final boolean sameMask = Objects.equals(previousMaskId, currentMaskId);
		final boolean sameMappings = Objects.equals(getValue(), sanitizedMappings);
		if (sameMask && sameMappings) {
			return;
		}
		setValue(sanitizedMappings);
		// If only the mask context changed (same mapping list), onValueChanged may short-circuit;
		// force one refresh so destination/source variables are loaded for the new mask.
		if (!sameMask && sameMappings) {
			refreshComponent();
		}
	}

	private ROutputMappingGridRow createGridRowFromMapping(final ROutputActionMapping mapping,
			final Map<String, CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationByName) {
		final CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable destinationVariable =
				destinationByName.get(CBabPolicyFilterCAN.normalizeVariableName(mapping.targetProtocolVariableName()));
		final String destinationName = destinationVariable != null ? destinationVariable.name() : mapping.targetProtocolVariableName();
		final String destinationType = destinationVariable != null ? destinationVariable.dataType() : mapping.targetProtocolVariableDataType();
		return new ROutputMappingGridRow(mapping.outputName(), mapping.outputDataType(), destinationName, destinationType);
	}

	private Map<String, CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> buildDestinationByName(
			final List<CPageServiceBabPolicyActionMaskCAN.RDestinationProtocolVariable> destinationVariables) {
		return destinationVariables.stream().filter(variable -> variable != null && !variable.name().isBlank()).collect(LinkedHashMap::new,
				(map, variable) -> map.putIfAbsent(CBabPolicyFilterCAN.normalizeVariableName(variable.name()), variable), Map::putAll);
	}

	private static Map<String, ROutputActionMapping> getMappingsByOutputName(final List<ROutputActionMapping> mappings) {
		if (mappings == null || mappings.isEmpty()) {
			return new LinkedHashMap<>();
		}
		final Map<String, ROutputActionMapping> mappingsByOutputName = new LinkedHashMap<>();
		mappings.stream().filter(mapping -> mapping != null && !mapping.outputName().isBlank()).forEach(mapping -> mappingsByOutputName
				.putIfAbsent(CBabPolicyFilterCAN.normalizeVariableName(mapping.outputName()), mapping));
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
}
