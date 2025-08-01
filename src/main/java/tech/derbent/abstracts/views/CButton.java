package tech.derbent.abstracts.views;

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

	/**
	 * Creates an error button with the specified text.
	 * @param text the button text
	 * @return a new CButton with error styling
	 */
	public static CButton createError(final String text) {
		final CButton button = new CButton(text);
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		return button;
	}

	/**
	 * Creates an error button with text and click listener.
	 * @param text          the button text
	 * @param clickListener the click event listener
	 * @return a new CButton with error styling
	 */
	public static CButton createError(final String text,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		return button;
	}

	/**
	 * Creates an error button with text and icon.
	 * @param text the button text
	 * @param icon the button icon
	 * @return a new CButton with error styling and icon
	 */
	public static CButton createError(final String text, final Icon icon) {
		final CButton button = new CButton(text, icon);
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		return button;
	}

	/**
	 * Creates an error button with text, icon and click listener.
	 * @param text          the button text
	 * @param icon          the button icon
	 * @param clickListener the click event listener
	 * @return a new CButton with error styling and icon
	 */
	public static CButton createError(final String text, final Icon icon,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		return button;
	}

	/**
	 * Creates a primary button with the specified text.
	 * @param text the button text
	 * @return a new CButton with primary styling
	 */
	public static CButton createPrimary(final String text) {
		final CButton button = new CButton(text);
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return button;
	}

	/**
	 * Creates a primary button with text and click listener.
	 * @param text          the button text
	 * @param clickListener the click event listener
	 * @return a new CButton with primary styling
	 */
	public static CButton createPrimary(final String text,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return button;
	}

	/**
	 * Creates a primary button with text and icon.
	 * @param text the button text
	 * @param icon the button icon
	 * @return a new CButton with primary styling and icon
	 */
	public static CButton createPrimary(final String text, final Icon icon) {
		final CButton button = new CButton(text, icon);
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return button;
	}

	/**
	 * Creates a primary button with text, icon and click listener.
	 * @param text          the button text
	 * @param icon          the button icon
	 * @param clickListener the click event listener
	 * @return a new CButton with primary styling and icon
	 */
	public static CButton createPrimary(final String text, final Icon icon,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return button;
	}

	/**
	 * Creates a tertiary button with the specified text.
	 * @param text the button text
	 * @return a new CButton with tertiary styling
	 */
	public static CButton createTertiary(final String text) {
		final CButton button = new CButton(text);
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		return button;
	}

	/**
	 * Creates a tertiary button with text and click listener.
	 * @param text          the button text
	 * @param clickListener the click event listener
	 * @return a new CButton with tertiary styling
	 */
	public static CButton createTertiary(final String text,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, clickListener);
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
	 * Constructor with icon.
	 * @param icon the button icon
	 */
	public CButton(final Icon icon) {
		super(icon);
		initializeButton();
	}

	/**
	 * Constructor with icon and click listener.
	 * @param icon          the button icon
	 * @param clickListener the click event listener
	 */
	public CButton(final Icon icon,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		super(icon, clickListener);
		initializeButton();
	}

	/**
	 * Constructor with text.
	 * @param text the button text
	 */
	public CButton(final String text) {
		super(text);
		initializeButton();
	}

	/**
	 * Constructor with text and click listener.
	 * @param text          the button text
	 * @param clickListener the click event listener
	 */
	public CButton(final String text,
		final ComponentEventListener<ClickEvent<Button>> clickListener) {
		super(text, clickListener);
		initializeButton();
	}

	/**
	 * Constructor with text and icon.
	 * @param text the button text
	 * @param icon the button icon
	 */
	public CButton(final String text, final Icon icon) {
		super(text, icon);
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
		super(text, icon, clickListener);
		initializeButton();
	}

	/**
	 * Common initialization for all CButton instances. This method can be overridden by
	 * subclasses to provide additional initialization.
	 */
	protected void initializeButton() {
		CAuxillaries.generateId(this);
	}
}