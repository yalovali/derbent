package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;

/** CButton - Abstract base class for all buttons in the application. Layer: View (MVC) Provides common initialization patterns, logging, and
 * standardized button configurations. Extends Vaadin Button with application-specific enhancements. */
public class CButton extends Button {

	private static final long serialVersionUID = 1L;

	public static CButton createCancelButton(final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final Icon icon = CColorUtils.createStyledIcon(CColorUtils.CRUD_CANCEL_ICON, CColorUtils.CRUD_CANCEL_COLOR);
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		return button;
	}

	public static CButton createCloneButton(final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final Icon icon = CColorUtils.createStyledIcon(CColorUtils.CRUD_CLONE_ICON, CColorUtils.CRUD_UPDATE_COLOR);
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		return button;
	}

	public static CButton createDeleteButton(final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final Icon icon = CColorUtils.createStyledIcon(CColorUtils.CRUD_DELETE_ICON, CColorUtils.CRUD_DELETE_COLOR);
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		return button;
	}

	public static CButton createEditButton(final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final Icon icon = CColorUtils.createStyledIcon(CColorUtils.CRUD_EDIT_ICON, CColorUtils.CRUD_UPDATE_COLOR);
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		return button;
	}

	public static CButton createError(final String text, final Icon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_ERROR);
		return button;
	}

	public static CButton createNewButton(final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final Icon icon = CColorUtils.createStyledIcon(CColorUtils.CRUD_CREATE_ICON, CColorUtils.CRUD_CREATE_COLOR);
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		return button;
	}

	public static CButton createPrimary(final String text, final Icon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return button;
	}

	public static CButton createSaveButton(final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final Icon icon = CColorUtils.createStyledIcon(CColorUtils.CRUD_SAVE_ICON, CColorUtils.CRUD_SAVE_COLOR);
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		return button;
	}

	public static CButton createTertiary(final String text, final Icon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		return button;
	}
	// CRUD-specific button factory methods with consistent icons and colors

	public static CButton createViewButton(final String text, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		final Icon icon = CColorUtils.createStyledIcon(CColorUtils.CRUD_VIEW_ICON, CColorUtils.CRUD_READ_COLOR);
		final CButton button = new CButton(text, icon, clickListener);
		button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		return button;
	}

	/** Default constructor for CButton. */
	public CButton() {
		super();
		initializeComponent();
	}

	public CButton(final Icon icon) {
		super(CColorUtils.setIconClassSize(icon, IconSize.MEDIUM));
		initializeComponent();
	}

	/** Constructor with text, icon and click listener.
	 * @param text          the button text
	 * @param icon          the button icon
	 * @param clickListener the click event listener */
	public CButton(final String text, final Icon icon) {
		super(text, CColorUtils.setIconClassSize(icon, IconSize.MEDIUM));
		initializeComponent();
	}

	/** Constructor with text, icon and click listener.
	 * @param text          the button text
	 * @param icon          the button icon
	 * @param clickListener the click event listener */
	public CButton(final String text, final Icon icon, final ComponentEventListener<ClickEvent<Button>> clickListener) {
		super(text, CColorUtils.setIconClassSize(icon, IconSize.MEDIUM), clickListener == null ? e -> {/***/
		} : clickListener);
		initializeComponent();
	}

	/** Common initialization for all CButton instances. This method can be overridden by subclasses to provide additional initialization. */
	protected void initializeComponent() {
		CAuxillaries.setId(this);
		if (!getText().isEmpty()) {
			setMinWidth("120px");
		}
	}
}
