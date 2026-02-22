package tech.derbent.api.ui.component.enhanced;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CVerticalLayout;

/** Sample usage for {@link CComponentMultiColumnListSelection}. */
public class CComponentMultiColumnListSelectionSample extends CVerticalLayout {

	private static final long serialVersionUID = 1L;

	public CComponentMultiColumnListSelectionSample() {
		super(false, false, false);
		setWidthFull();
		final CComponentMultiColumnListSelection component = new CComponentMultiColumnListSelection("Users");
		component.setColumns(List.of(new CComponentMultiColumnListSelection.CColumnDefinition("key", "Key"),
				new CComponentMultiColumnListSelection.CColumnDefinition("name", "Name"),
				new CComponentMultiColumnListSelection.CColumnDefinition("role", "Role")));
		component.setReturnedColumnId("key");
		component.setSourceItems(createSampleRows());
		final Span output = new Span("Click 'Read selection' after choosing rows.");
		output.setWidthFull();
		final CButton readSelectionButton = CButton.createPrimary("Read Selection", VaadinIcon.CLIPBOARD_CHECK.create(), event -> {
			final List<CComponentMultiColumnListSelection.CMultiColumnStringRow> selectedRecords = component.getValue();
			final List<String> selectedReturnValues = component.getReturnedValues();
			final String selectedNames = selectedRecords.stream().map(row -> row.getValue("name")).collect(Collectors.joining(", "));
			output.setText("returnValues=" + selectedReturnValues + " | selectedRecords(name)=" + selectedNames);
		});
		add(component, readSelectionButton, output);
	}

	private static List<CComponentMultiColumnListSelection.CMultiColumnStringRow> createSampleRows() {
		return List.of(new CComponentMultiColumnListSelection.CMultiColumnStringRow("vaadin:user", "#1C88FF",
				Map.of("key", "USR-001", "name", "Alice Brown", "role", "Project Manager")),
				new CComponentMultiColumnListSelection.CMultiColumnStringRow("vaadin:user-star", "#7CAF50",
						Map.of("key", "USR-002", "name", "Bob Carter", "role", "Developer")),
				new CComponentMultiColumnListSelection.CMultiColumnStringRow("vaadin:user-check", "#FF8C00",
						Map.of("key", "USR-003", "name", "Celine Diaz", "role", "QA Engineer")));
	}
}
