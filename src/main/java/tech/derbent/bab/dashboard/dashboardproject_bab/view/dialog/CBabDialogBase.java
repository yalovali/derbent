package tech.derbent.bab.dashboard.dashboardproject_bab.view.dialog;

import java.util.regex.Pattern;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.dialogs.CDialog;

/** CBabDialogBase - Base class for BAB Gateway configuration dialogs.
 * <p>
 * Provides common functionality for BAB profile dialogs:
 * <ul>
 * <li>Standard width (500-700px) with responsive layout</li>
 * <li>Custom spacing (12px gaps)</li>
 * <li>IP address validation pattern</li>
 * <li>Validation info header creation</li>
 * <li>Hint section creation</li>
 * <li>Common styling constants</li>
 * </ul>
 * <p>
 * Usage: Extend this class and implement abstract methods for dialog-specific logic. */
public abstract class CBabDialogBase extends CDialog {

	// IP address validation pattern: xxx.xxx.xxx.xxx where xxx is 0-255
	protected static final Pattern IP_PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");
	private static final long serialVersionUID = 1L;
	protected static final String STYLE_FONT_SIZE_SMALL = "0.875rem";
	protected static final String STYLE_FONT_SIZE_XSMALL = "0.75rem";
	// Common styling constants
	protected static final String STYLE_GAP = "12px";
	protected CSpan validationInfo;

	/** Apply custom spacing to main layout. */
	protected void applyCustomSpacing() {
		mainLayout.setSpacing(false);
		mainLayout.getStyle().set("gap", STYLE_GAP);
		mainLayout.setWidthFull();
	}

	/** Configure dialog with BAB standard width and spacing.
	 * @param width Dialog width (e.g., "500px", "600px", "700px") */
	protected void configureBabDialog(final String width) {
		setWidth(width);
		setMaxWidth(width);
		// Setup dialog structure
		try {
			setupDialog();
		} catch (final Exception e) {
			throw new RuntimeException("Failed to setup BAB dialog", e);
		}
	}

	/** Create header layout with label and validation info.
	 * @param label    Header label text
	 * @param required Whether field is required
	 * @return Header layout with label and validation info placeholder */
	protected HorizontalLayout createHeaderLayout(final String label, final boolean required) {
		final HorizontalLayout header = new HorizontalLayout();
		header.setWidthFull();
		header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		header.setAlignItems(FlexComponent.Alignment.CENTER);
		header.setPadding(false);
		header.setSpacing(false);
		// Left: Label with optional required indicator
		final CSpan labelSpan = new CSpan(label);
		labelSpan.getStyle().set("font-weight", "500").set("font-size", STYLE_FONT_SIZE_SMALL);
		if (required) {
			final CSpan requiredSpan = new CSpan(" *");
			requiredSpan.getStyle().set("color", "var(--lumo-error-color)");
			final HorizontalLayout leftSide = new HorizontalLayout(labelSpan, requiredSpan);
			leftSide.setPadding(false);
			leftSide.setSpacing(false);
			header.add(leftSide);
		} else {
			header.add(labelSpan);
		}
		// Right: Validation info (updated dynamically)
		validationInfo = new CSpan();
		validationInfo.getStyle().set("font-size", STYLE_FONT_SIZE_XSMALL).set("color", "var(--lumo-secondary-text-color)");
		header.add(validationInfo);
		return header;
	}

	/** Create hint section with usage instructions.
	 * @param hintText Hint text to display
	 * @return Styled hint section */
	protected CDiv createHintSection(final String hintText) {
		final CDiv hintDiv = new CDiv();
		hintDiv.getStyle().set("padding", "8px 12px").set("background", "var(--lumo-contrast-5pct)")
				.set("border-left", "3px solid var(--lumo-primary-color)").set("border-radius", "4px").set("font-size", "0.8125rem")
				.set("color", "var(--lumo-secondary-text-color)").set("line-height", "1.4");
		final CSpan hintSpan = new CSpan("💡 " + hintText);
		hintDiv.add(hintSpan);
		return hintDiv;
	}

	/** Validate IP address format.
	 * @param ip IP address string
	 * @return true if valid IPv4 format */
	protected boolean isValidIpAddress(final String ip) {
		if (ip == null || ip.trim().isEmpty()) {
			return false;
		}
		return IP_PATTERN.matcher(ip.trim()).matches();
	}

	/** Update validation info with error message.
	 * @param message Error message */
	protected void setValidationError(final String message) {
		validationInfo.setText(message);
		validationInfo.getStyle().set("color", "var(--lumo-error-color)");
	}

	/** Update validation info with success message.
	 * @param message Success message */
	protected void setValidationSuccess(final String message) {
		validationInfo.setText(message);
		validationInfo.getStyle().set("color", "var(--lumo-success-color)");
	}

	/** Update validation info with warning message.
	 * @param message Warning message */
	protected void setValidationWarning(final String message) {
		validationInfo.setText(message);
		validationInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
	}
}
