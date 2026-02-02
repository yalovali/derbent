package tech.derbent.bab.uiobjects.view;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.bab.dashboard.view.CComponentInterfaceList;

/**
 * CComponentBabBase - Base class for BAB profile display components.
 * <p>
 * This base class provides a standard pattern for BAB components that:
 * <ul>
 * <li>Display data from external services (e.g., Calimero HTTP API)</li>
 * <li>Do not require form binding (display-only components)</li>
 * <li>Follow the Derbent component initialization lifecycle</li>
 * <li>Provide standard toolbar with Refresh and optional Edit buttons</li>
 * </ul>
 * <p>
 * Subclasses must implement:
 * <ul>
 * <li>{@link #initializeComponents()} - Build UI components and layout</li>
 * <li>{@link #refreshComponent()} - Refresh component data from service</li>
 * </ul>
 * <p>
 * Optional toolbar customization:
 * <ul>
 * <li>{@link #getRefreshButtonId()} - Override to customize refresh button ID</li>
 * <li>{@link #hasEditButton()} - Override to add Edit button</li>
 * <li>{@link #getEditButtonId()} - Override to customize edit button ID</li>
 * <li>{@link #getEditButtonText()} - Override to customize edit button text</li>
 * <li>{@link #on_buttonEdit_clicked()} - Override to handle edit button click</li>
 * <li>{@link #addAdditionalToolbarButtons(CHorizontalLayout)} - Override to add custom buttons</li>
 * </ul>
 * <p>
 * Pattern:
 * <pre>
 * public class CComponentMyData extends CComponentBabBase {
 *
 * 	private static final long serialVersionUID = 1L;
 *
 * 	public CComponentMyData(final ISessionService sessionService) {
 * 		this.sessionService = sessionService;
 * 		initializeComponents();
 * 	}
 *
 * 	&#64;Override
 * 	protected void initializeComponents() {
 * 		configureComponent();
 * 		add(createStandardToolbar());  // Standard toolbar with Refresh button
 * 		createGrid();
 * 		loadData();
 * 	}
 *
 * 	&#64;Override
 * 	protected void refreshComponent() {
 * 		loadData();
 * 	}
 *
 * 	// Optional: Add Edit button
 * 	&#64;Override
 * 	protected boolean hasEditButton() {
 * 		return true;
 * 	}
 *
 * 	&#64;Override
 * 	protected void on_buttonEdit_clicked() {
 * 		// Handle edit
 * 	}
 * }
 * </pre>
 *
 * @see CComponentInterfaceList Example implementation
 */
public abstract class CComponentBabBase extends CVerticalLayout {
	
	private static final long serialVersionUID = 1L;
	
	// Standard toolbar components
	protected CButton buttonRefresh;
	protected CButton buttonEdit;
	protected CHorizontalLayout toolbar;
	
	protected CComponentBabBase() {
		super();
	}
	
	/**
	 * Configure component styling and behavior.
	 * Subclasses can override to customize.
	 */
	protected void configureComponent() {
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", "12px");
		setMaxHeight("250px");
	}
	
	/**
	 * Create standard toolbar with Refresh and optional Edit buttons.
	 * Subclasses should call this in initializeComponents() and add to layout.
	 * 
	 * @return CHorizontalLayout with standard buttons
	 */
	protected CHorizontalLayout createStandardToolbar() {
		toolbar = new CHorizontalLayout();
		toolbar.setSpacing(true);
		toolbar.getStyle().set("gap", "8px");
		
		// Always add refresh button
		buttonRefresh = createRefreshButton();
		toolbar.add(buttonRefresh);
		
		// Optionally add edit button
		if (hasEditButton()) {
			buttonEdit = createEditButton();
			toolbar.add(buttonEdit);
		}
		
		// Allow subclasses to add additional buttons
		addAdditionalToolbarButtons(toolbar);
		
		return toolbar;
	}
	
	/**
	 * Create refresh button with standard styling.
	 * Subclasses can override for customization, but should call super.
	 * 
	 * @return CButton configured as refresh button
	 */
	protected CButton createRefreshButton() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(getRefreshButtonId());
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}
	
	/**
	 * Create edit button with standard styling.
	 * Only called if hasEditButton() returns true.
	 * 
	 * @return CButton configured as edit button
	 */
	protected CButton createEditButton() {
		final CButton button = new CButton(getEditButtonText(), VaadinIcon.EDIT.create());
		button.setId(getEditButtonId());
		button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(e -> on_buttonEdit_clicked());
		return button;
	}
	
	/**
	 * Get refresh button ID.
	 * Override to customize ID for Playwright tests.
	 * 
	 * @return String ID for refresh button
	 */
	protected String getRefreshButtonId() {
		return "custom-refresh-button";
	}
	
	/**
	 * Check if component should have Edit button.
	 * Override to return true if edit functionality needed.
	 * 
	 * @return true if Edit button should be shown
	 */
	protected boolean hasEditButton() {
		return false;
	}
	
	/**
	 * Get edit button ID.
	 * Override to customize ID for Playwright tests.
	 * 
	 * @return String ID for edit button
	 */
	protected String getEditButtonId() {
		return "custom-edit-button";
	}
	
	/**
	 * Get edit button text.
	 * Override to customize button label.
	 * 
	 * @return String text for edit button
	 */
	protected String getEditButtonText() {
		return "Edit";
	}
	
	/**
	 * Handle refresh button click.
	 * Default implementation calls refreshComponent().
	 * Override if custom behavior needed.
	 */
	protected void on_buttonRefresh_clicked() {
		refreshComponent();
	}
	
	/**
	 * Handle edit button click.
	 * Override to implement edit functionality.
	 */
	protected void on_buttonEdit_clicked() {
		// Override in subclass
	}
	
	/**
	 * Add additional buttons to toolbar.
	 * Override to add custom buttons beyond Refresh and Edit.
	 * 
	 * @param toolbarLayout The toolbar layout to add buttons to
	 */
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		// Override in subclass if needed
	}
	
	/**
	 * Initialize component UI and layout.
	 * Called once during construction.
	 * Subclasses must implement to build UI components.
	 */
	protected abstract void initializeComponents();
	
	/**
	 * Refresh component data from service.
	 * Called when data needs to be reloaded.
	 * Subclasses must implement to update displayed data.
	 */
	protected abstract void refreshComponent();
}

