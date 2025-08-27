package tech.derbent.abstracts.views.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;

import tech.derbent.abstracts.utils.CAuxillaries;

/**
 * CButton - Abstract base class for all buttons in the application. Layer: View (MVC)
 * Provides common initialization patterns, logging, and standardized button
 * configurations. Extends Vaadin Button with application-specific enhancements.
 */
public class CButton extends Button {

	private static final long serialVersionUID = 1L;

	public static CButton createError(final String text, final Icon icon,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		return button;
	}

	public static CButton createPrimary(final String text, final Icon icon,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return button;
	}

	public static CButton createTertiary(final String text, final Icon icon,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		return button;
	}

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * Default constructor for CButton.
	 */
	public CButton() {
		super();
		initializeButton();
	}

	/**
	 * Constructor with text, icon and click listener.
	 * @param text          the button text
	 * @param icon          the button icon
	 * @param clickListener the click event listener
	 */
	public CButton(final String text, final Icon icon,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		super(text, icon, clickListener == null ? e -> {} : clickListener);
		initializeButton();
	}

	/**
	 * Common initialization for all CButton instances. This method can be overridden by
	 * subclasses to provide additional initialization.
	 */
	protected void initializeButton() {
		CAuxillaries.setId(this);
	}
}