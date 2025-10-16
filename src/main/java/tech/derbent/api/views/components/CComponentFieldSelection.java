package tech.derbent.api.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;

public class CComponentFieldSelection<MasterEntity, DetailEntity> extends CHorizontalLayout {

	private static final long serialVersionUID = 1L;
	private CButton addButton;
	private ListBox<DetailEntity> availableList;
	private CButton downButton;
	private ItemLabelGenerator<DetailEntity> itemLabelGenerator = Object::toString;
	Logger LOGGER = LoggerFactory.getLogger(CComponentFieldSelection.class);
	private CButton removeButton;
	private final List<DetailEntity> selectedItems = new ArrayList<>();
	private ListBox<DetailEntity> selectedList;
	private final List<DetailEntity> sourceItems = new ArrayList<>();
	private CButton upButton;

	public CComponentFieldSelection() {
		super();
		initialize();
	}

	public CComponentFieldSelection(String string, String string2) {
		// TODO Auto-generated constructor stub
	}

	private void initialize() {
		CVerticalLayout leftLayout = new CVerticalLayout();
		CVerticalLayout rightLayout = new CVerticalLayout();
		leftLayout.setWidth("50%");
		rightLayout.setWidth("50%");
		// Add components to leftLayout and rightLayout as needed
		this.add(leftLayout, rightLayout);
		// set titles
		leftLayout.add(new CDiv("Available Fields"));
		rightLayout.add(new CDiv("Selected Fields"));
		// Add dual list selector component here
		availableList = new ListBox<>();
		selectedList = new ListBox<>();
		leftLayout.add(availableList);
		rightLayout.add(selectedList);
		// // Control buttons with icons
		addButton = new CButton("Add", VaadinIcon.ARROW_RIGHT.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setEnabled(false);
		addButton.setTooltipText("Add selected item to the list");
		removeButton = new CButton("Remove", VaadinIcon.ARROW_LEFT.create());
		removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		removeButton.setEnabled(false);
		removeButton.setTooltipText("Remove selected item from the list");
		CHorizontalLayout controlButtons = new CHorizontalLayout(addButton, removeButton);
		controlButtons.setSpacing(true);
		// Order buttons with icons
		upButton = new CButton("Move Up", VaadinIcon.ARROW_UP.create());
		upButton.setEnabled(false);
		upButton.setTooltipText("Move selected item up in the order");
		downButton = new CButton("Move Down", VaadinIcon.ARROW_DOWN.create());
		downButton.setEnabled(false);
		downButton.setTooltipText("Move selected item down in the order");
		// Layouts
		CHorizontalLayout orderButtons = new CHorizontalLayout(upButton, downButton);
		orderButtons.setSpacing(true);
		// Add buttons to right layout
		rightLayout.add(controlButtons);
		leftLayout.add(orderButtons);
	}

	private void refreshLists() {
		// Update available list - show items not in selected
		List<DetailEntity> available = sourceItems.stream().filter(item -> !selectedItems.contains(item)).collect(Collectors.toList());
		availableList.setItems(available);
		// Update selected list
		selectedList.setItems(selectedItems);
	}

	public void setItemLabelGenerator(ItemLabelGenerator<DetailEntity> itemLabelGenerator) {
		try {
			this.itemLabelGenerator = itemLabelGenerator != null ? itemLabelGenerator : Object::toString;
			// Refresh renderer to use new label generator
			// configureColorAwareRenderer(availableList);
			// configureColorAwareRenderer(selectedList);
		} catch (Exception e) {
			LOGGER.error("Failed to set item label generator", e);
			throw new IllegalStateException("Failed to set item label generator", e);
		}
	}

	public void setItems(List<DetailEntity> items) {
		try {
			sourceItems.clear();
			if (items != null) {
				sourceItems.addAll(items);
			}
			refreshLists();
		} catch (Exception e) {
			LOGGER.error("Failed to set items in dual list selector", e);
			throw new IllegalStateException("Failed to set items", e);
		}
	}
}
