package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.screens.view.CComponentGridEntity;

public class CComponentDetailsMasterToolbar extends HorizontalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentDetailsMasterToolbar.class);
	private static final long serialVersionUID = 1L;
	private CButton btnEditGrid;
	private CComponentGridEntity grid;

	public CComponentDetailsMasterToolbar(CComponentGridEntity grid) {
		this.grid = grid;
		setSpacing(true);
		setPadding(true);
		addClassName("crud-toolbar");
		setWidthFull(); // Make toolbar take full width
		createToolbarButtons();
	}

	/** Creates all the CRUD toolbar buttons. */
	private void createToolbarButtons() {
		// Create (New) Button
		btnEditGrid = CButton.createPrimary("Edit Grid Entity", VaadinIcon.PLUS.create(), e -> handleEditGridEntity());
		btnEditGrid.getElement().setAttribute("title", "Edit Grid Entity");
		add(btnEditGrid);
		updateButtonStates();
	}

	private void updateButtonStates() {
		// TODO Auto-generated method stub
	}

	private void handleEditGridEntity() {
		// open the CGridEntity editor
		// if saved refresh the grid and select the new entity
	}
}
