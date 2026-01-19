package tech.derbent.plm.kanban.kanbanline.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** CDialogKanbanStatusSelection - Simple, colorful dialog for selecting a status during kanban drag-drop. This dialog displays a list of available
 * statuses with their colors and icons, allowing the user to select which status to apply when dragging an item to a column that has multiple valid
 * statuses. Features: - Large, colorful status buttons with icon and status name - Easy to click on touch devices - Cancel option to abort the status
 * change - Callback-based selection handling */
public class CDialogKanbanStatusSelection extends CDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogKanbanStatusSelection.class);
	private static final long serialVersionUID = 1L;

	/** Determines if a hex color is light (closer to white) or dark (closer to black). Uses a simple brightness calculation based on RGB values.
	 * @param hexColor Hex color string (e.g., "#FF5733" or "FF5733")
	 * @return true if color is light (use black text), false if dark (use white text) */
	private static boolean isLightColor(final String hexColor) {
		if (hexColor == null || hexColor.isBlank()) {
			return true; // Default to light
		}
		try {
			// Remove # if present
			final String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
			// Parse RGB values
			final int r = Integer.parseInt(hex.substring(0, 2), 16);
			final int g = Integer.parseInt(hex.substring(2, 4), 16);
			final int b = Integer.parseInt(hex.substring(4, 6), 16);
			// Calculate brightness using perceived brightness formula
			// See: https://www.w3.org/TR/AERT/#color-contrast
			final double brightness = r * 0.299 + g * 0.587 + b * 0.114;
			// Threshold: 128 is middle gray
			return brightness > 128;
		} catch (final Exception e) {
			LOGGER.debug("Could not parse color {}: {}", hexColor, e.getMessage());
			return true; // Default to light on error
		}
	}

	private CButton buttonCancel;
	private final String columnName;
	private final Consumer<CProjectItemStatus> onStatusSelected;
	private CVerticalLayout statusButtonsLayout;
	private final List<CProjectItemStatus> statuses;

	/** Creates a status selection dialog for kanban drag-drop operations.
	 * @param columnName       The name of the kanban column being dropped onto
	 * @param statuses         List of valid statuses to choose from (must have at least 2 items)
	 * @param onStatusSelected Callback invoked when user selects a status (receives selected status, or null if cancelled) */
	public CDialogKanbanStatusSelection(final String columnName, final List<CProjectItemStatus> statuses,
			final Consumer<CProjectItemStatus> onStatusSelected) {
		super();
		Check.notBlank(columnName, "Column name cannot be blank");
		Check.notEmpty(statuses, "Statuses list cannot be empty");
		Check.isTrue(statuses.size() >= 2, "Status selection dialog requires at least 2 statuses");
		Check.notNull(onStatusSelected, "Status selection callback cannot be null");
		this.columnName = columnName;
		this.statuses = statuses;
		this.onStatusSelected = onStatusSelected;
		try {
			setupDialog();
			// Override default width to make buttons more prominent
			setWidth("450px");
			setResizable(false);
		} catch (final Exception e) {
			LOGGER.error("Error setting up status selection dialog", e);
			CNotificationService.showException("Error creating status selection dialog", e);
		}
	}

	/** Creates a large, colorful button for a status. The button displays: - Status icon (if available) - Status name - Background color matching the
	 * status color - Hover effect for better UX */
	private CButton createStatusButton(final CProjectItemStatus status) {
		Check.notNull(status, "Status cannot be null when creating button");
		// Create button with status name and icon
		Icon icon = null;
		try {
			icon = CColorUtils.getIconForEntity(status);
			if (icon != null) {
				icon.setSize("20px");
				icon.getStyle().set("margin-right", "8px");
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create icon for status {}: {}", status.getName(), e.getMessage());
		}
		// Create button with icon and name
		// Note: CButton requires both text and icon, so we provide a default icon if status doesn't have one
		if (icon == null) {
			icon = VaadinIcon.CIRCLE.create(); // Default icon if status doesn't have one
		}
		icon.setSize("20px");
		icon.getStyle().set("margin-right", "8px");
		final CButton button = new CButton(status.getName(), icon);
		// Apply status color as background
		final String backgroundColor =
				status.getColor() != null && !status.getColor().isBlank() ? status.getColor() : CProjectItemStatus.DEFAULT_COLOR;
		// Use white or black text depending on background brightness
		// Simple algorithm: if color is light, use black text, otherwise white
		final String textColor = isLightColor(backgroundColor) ? "#000000" : "#FFFFFF";
		// Style the button to be large, colorful, and easy to click
		button.getStyle().set("width", "100%").set("padding", "16px 20px").set("font-size", "16px").set("font-weight", "500")
				.set("background-color", backgroundColor).set("color", textColor).set("border", "2px solid rgba(0, 0, 0, 0.1)")
				.set("border-radius", "8px").set("cursor", "pointer").set("transition", "all 0.2s ease").set("margin-bottom", "12px")
				.set("text-align", "left").set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");
		// Add hover effect using JavaScript (Vaadin doesn't support CSS :hover directly)
		button.getElement()
				.executeJs("this.addEventListener('mouseenter', () => { " + "  this.style.transform = 'translateX(5px)'; "
						+ "  this.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.2)'; " + "}); " + "this.addEventListener('mouseleave', () => { "
						+ "  this.style.transform = 'translateX(0)'; " + "  this.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.1)'; " + "});");
		// Handle click: invoke callback and close dialog
		button.addClickListener(on_statusButton_clicked(status));
		return button;
	}

	@Override
	public String getDialogTitleString() { return "Select Status"; }

	@Override
	protected Icon getFormIcon() throws Exception { return VaadinIcon.CLIPBOARD_CHECK.create(); }

	@Override
	protected String getFormTitleString() { return "Choose Status for Column '" + columnName + "'"; }

	/** Handles cancel button click: close dialog without selection. */
	@SuppressWarnings ("unused")
	protected ComponentEventListener<ClickEvent<Button>> on_buttonCancel_clicked() {
		return event -> {
			LOGGER.debug("Status selection cancelled by user");
			onStatusSelected.accept(null); // Null indicates cancellation
			close();
		};
	}

	/** Handles status button click: invoke callback with selected status and close dialog. */
	@SuppressWarnings ("unused")
	protected ComponentEventListener<ClickEvent<Button>> on_statusButton_clicked(final CProjectItemStatus status) {
		return event -> {
			LOGGER.debug("User selected status: {} (ID: {})", status.getName(), status.getId());
			onStatusSelected.accept(status);
			close();
		};
	}

	@Override
	protected void setupButtons() {
		// Cancel button at the bottom
		final Icon cancelIcon = VaadinIcon.CLOSE.create();
		buttonCancel = new CButton("Cancel", cancelIcon);
		buttonCancel.addClickListener(on_buttonCancel_clicked());
		buttonCancel.getStyle().set("background-color", "#E0E0E0").set("color", "#333").set("border-radius", "6px").set("padding", "10px 24px")
				.set("font-size", "14px");
		buttonLayout.add(buttonCancel);
	}

	@Override
	protected void setupContent() throws Exception {
		// Create a vertical layout for status buttons
		statusButtonsLayout = new CVerticalLayout();
		statusButtonsLayout.setPadding(false);
		statusButtonsLayout.setSpacing(false);
		statusButtonsLayout.setWidthFull();
		// Add instruction text
		final CDiv instructionText = new CDiv();
		instructionText.setText("This column supports multiple statuses. Please select which status to apply:");
		instructionText.getStyle().set("margin-bottom", "16px").set("padding", "12px").set("background-color", "#E3F2FD").set("border-radius", "6px")
				.set("color", "#0D47A1").set("font-size", "14px").set("line-height", "1.5");
		mainLayout.add(instructionText);
		// Create a button for each status
		for (final CProjectItemStatus status : statuses) {
			final CButton statusButton = createStatusButton(status);
			statusButtonsLayout.add(statusButton);
		}
		mainLayout.add(statusButtonsLayout);
	}
}
