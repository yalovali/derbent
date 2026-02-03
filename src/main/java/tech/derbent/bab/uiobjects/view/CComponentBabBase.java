package tech.derbent.bab.uiobjects.view;

import java.util.Optional;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.bab.dashboard.service.CAbstractCalimeroClient;
import tech.derbent.bab.dashboard.view.CComponentInterfaceList;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.base.session.service.ISessionService;

/** CComponentBabBase - Base class for BAB profile display components.
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
 *
 * <pre>
 * public class CComponentMyData extends CComponentBabBase {
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
 * 		add(createStandardToolbar()); // Standard toolbar with Refresh button
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
 * @see CComponentInterfaceList Example implementation */
public abstract class CComponentBabBase extends CVerticalLayout {
	private static final long serialVersionUID = 1L;
	// Standard toolbar components
	protected CButton buttonRefresh;
	protected CButton buttonEdit;
	protected CHorizontalLayout toolbar;
	protected CSpan summaryLabel;  // Right-aligned summary label for counts/statistics
	// Warning message component for Calimero unavailability
	protected Div warningMessage;
	// Calimero client - lazily initialized via getCalimeroClient()
	protected CAbstractCalimeroClient calimeroClient;
	protected final ISessionService sessionService;

	protected CComponentBabBase(final ISessionService sessionService) {
		this.sessionService = sessionService;
	}

	/** Add additional buttons to toolbar. Override to add custom buttons beyond Refresh and Edit.
	 * @param toolbarLayout The toolbar layout to add buttons to */
	protected void addAdditionalToolbarButtons(final CHorizontalLayout toolbarLayout) {
		// Override in subclass if needed
	}

	/** Configure component styling and behavior. Subclasses can override to customize. */
	protected void configureComponent() {
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", "12px");
		setMaxHeight("250px");
	}

	/** Create Calimero client for this component. Subclasses must implement to return their specific client type. Called lazily by
	 * getCalimeroClient() when client is first needed.
	 * @param clientProject HTTP client project for Calimero communication
	 * @return Concrete Calimero client instance */
	protected abstract CAbstractCalimeroClient createCalimeroClient(CClientProject clientProject);

	/** Create edit button with standard styling. Only called if hasEditButton() returns true.
	 * @return CButton configured as edit button */
	protected CButton createEditButton() {
		final CButton button = new CButton(getEditButtonText(), VaadinIcon.EDIT.create());
		button.setId(getEditButtonId());
		button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(e -> on_buttonEdit_clicked());
		return button;
	}

	/** Create standard header with component title. Subclasses should call this in initializeComponents() and add to layout. Override getHeaderText()
	 * to customize the title.
	 * @return CH3 header component */
	protected CH3 createHeader() {
		final CH3 header = new CH3(getHeaderText());
		header.setHeight(null);
		header.setId(getHeaderId());
		header.getStyle().set("margin", "0");
		return header;
	}

	/** Create refresh button with standard styling. Subclasses can override for customization, but should call super.
	 * @return CButton configured as refresh button */
	protected CButton createRefreshButton() {
		final CButton button = new CButton("Refresh", VaadinIcon.REFRESH.create());
		button.setId(getRefreshButtonId());
		button.addThemeVariants(ButtonVariant.LUMO_SMALL);
		button.addClickListener(e -> on_buttonRefresh_clicked());
		return button;
	}

	/** Create standard toolbar with Refresh and optional Edit buttons. Subclasses should call this in initializeComponents() and add to layout.
	 * <p>
	 * Toolbar Layout: [Refresh] [Edit] [Additional Buttons] <spacer> [Summary Label]
	 * <p>
	 * Buttons are left-aligned, spacer pushes summary label to far right corner.
	 * @return CHorizontalLayout with standard buttons */
	protected CHorizontalLayout createStandardToolbar() {
		toolbar = new CHorizontalLayout();
		toolbar.setSpacing(true);
		toolbar.setWidthFull();
		toolbar.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		toolbar.getStyle().set("gap", "8px");
		
		// Add buttons directly to toolbar (left-aligned by default)
		buttonRefresh = createRefreshButton();
		toolbar.add(buttonRefresh);
		
		// Optionally add edit button
		if (hasEditButton()) {
			buttonEdit = createEditButton();
			toolbar.add(buttonEdit);
		}
		
		// Allow subclasses to add additional buttons (they stay left-aligned)
		addAdditionalToolbarButtons(toolbar);
		
		// Add spacer to push summary label to far right
		final com.vaadin.flow.component.html.Div spacer = new com.vaadin.flow.component.html.Div();
		toolbar.add(spacer);
		toolbar.setFlexGrow(1, spacer);
		
		// Add summary label (far right corner)
		summaryLabel = createSummaryLabel();
		toolbar.add(summaryLabel);
		
		return toolbar;
	}
	
	/**
	 * Create summary label for displaying counts/statistics in toolbar.
	 * <p>
	 * Initially hidden. Positioned at far right via spacer in createStandardToolbar().
	 * Use updateSummary() to show/update text.
	 * <p>
	 * Styling:
	 * <ul>
	 *   <li>Font: smaller, secondary color</li>
	 *   <li>Position: far right corner (via flex spacer)</li>
	 *   <li>Visibility: hidden until updateSummary() called</li>
	 * </ul>
	 * 
	 * @return CSpan configured as summary label
	 */
	protected CSpan createSummaryLabel() {
		final CSpan label = new CSpan();
		label.getStyle()
			.set("font-size", "0.875rem")
			.set("color", "var(--lumo-secondary-text-color)")
			.set("font-weight", "500")
			.set("white-space", "nowrap");
		label.setVisible(false);  // Hidden by default
		return label;
	}
	
	/**
	 * Update summary label with text and make visible.
	 * <p>
	 * Use this to display brief statistics in the toolbar:
	 * <ul>
	 *   <li>"12 services (8 running, 4 stopped)"</li>
	 *   <li>"5 interfaces (3 up, 2 down)"</li>
	 *   <li>"24 processes"</li>
	 * </ul>
	 * <p>
	 * Call with empty string or null to hide summary.
	 * 
	 * @param summary Summary text to display (null or empty to hide)
	 */
	protected void updateSummary(final String summary) {
		if (summaryLabel == null) {
			return;  // Toolbar not yet created
		}
		
		if (summary == null || summary.isEmpty()) {
			summaryLabel.setVisible(false);
		} else {
			summaryLabel.setText(summary);
			summaryLabel.setVisible(true);
		}
	}
	
	/**
	 * Clear and hide summary label.
	 * <p>
	 * Convenience method equivalent to updateSummary(null).
	 */
	protected void clearSummary() {
		updateSummary(null);
	}

	/** Get or create Calimero client lazily.
	 * <p>
	 * Pattern:
	 * <ul>
	 * <li>First call: Resolves client project and creates client</li>
	 * <li>Subsequent calls: Returns cached client instance</li>
	 * <li>Returns empty if no active BAB project or client unavailable</li>
	 * </ul>
	 * @return Optional containing Calimero client or empty if unavailable */
	protected Optional<CAbstractCalimeroClient> getCalimeroClient() {
		if (calimeroClient != null) {
			return Optional.of(calimeroClient);
		}
		final Optional<CClientProject> clientOptional = resolveClientProject();
		if (clientOptional.isEmpty()) {
			return Optional.empty();
		}
		calimeroClient = createCalimeroClient(clientOptional.get());
		return Optional.of(calimeroClient);
	}

	/** Get edit button ID. Override to customize ID for Playwright tests.
	 * @return String ID for edit button */
	protected String getEditButtonId() { return "custom-edit-button"; }

	/** Get edit button text. Override to customize button label.
	 * @return String text for edit button */
	protected String getEditButtonText() { return "Edit"; }

	/** Get header ID for Playwright tests. Override to customize ID.
	 * @return String ID for header */
	protected String getHeaderId() { return "custom-component-header"; }

	/** Get header text for component title. Override to customize header title.
	 * @return String header text */
	protected String getHeaderText() { return "Component Header"; }

	/** Get refresh button ID. Override to customize ID for Playwright tests.
	 * @return String ID for refresh button */
	protected String getRefreshButtonId() { return "custom-refresh-button"; }

	/** Get session service for accessing active project/company/user. Subclasses must implement to provide session access.
	 * @return Session service instance */
	protected abstract ISessionService getSessionService();

	/** Check if component should have Edit button. Override to return true if edit functionality needed.
	 * @return true if Edit button should be shown */
	protected boolean hasEditButton() {
		return false;
	}

	/** Hide the Calimero unavailable warning message. Called when data loads successfully or before showing a new warning. */
	protected void hideCalimeroUnavailableWarning() {
		if ((warningMessage != null) && warningMessage.getParent().isPresent()) {
			remove(warningMessage);
			warningMessage = null;
		}
	}

	/** Initialize component UI and layout. Called once during construction. Subclasses must implement to build UI components. */
	protected abstract void initializeComponents();

	/** Handle edit button click. Override to implement edit functionality. */
	protected void on_buttonEdit_clicked() {
		// Override in subclass
	}

	/** Handle refresh button click. Default implementation calls refreshComponent(). Override if custom behavior needed. */
	protected void on_buttonRefresh_clicked() {
		refreshComponent();
	}

	/** Refresh component data from service. Called when data needs to be reloaded. Subclasses must implement to update displayed data. */
	protected abstract void refreshComponent();

	/** Resolve HTTP client project from active BAB project.
	 * <p>
	 * Returns empty if:
	 * <ul>
	 * <li>No active project in session</li>
	 * <li>Active project is not a BAB project</li>
	 * <li>BAB project has no HTTP client configured</li>
	 * </ul>
	 * @return Optional containing client project or empty if unavailable */
	protected Optional<CClientProject> resolveClientProject() {
		final ISessionService service = getSessionService();
		if (service == null) {
			return Optional.empty();
		}
		final var projectOpt = service.getActiveProject();
		if (projectOpt.isEmpty()) {
			return Optional.empty();
		}
		if (!(projectOpt.get() instanceof CProject_Bab)) {
			return Optional.empty();
		}
		final CProject_Bab babProject = (CProject_Bab) projectOpt.get();
		return Optional.ofNullable(babProject.getHttpClient());
	}

	/** Show warning message when Calimero service is unavailable. Displays a small warning banner inside the component with icon and message. This is
	 * a graceful degradation - no exceptions thrown, just informative text.
	 * @param message Warning message to display (e.g., "Calimero service not available") */
	protected void showCalimeroUnavailableWarning(final String message) {
		// Remove any existing warning
		hideCalimeroUnavailableWarning();
		// Create warning banner
		warningMessage = new Div();
		warningMessage.getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px").set("padding", "8px 12px")
				.set("margin-bottom", "12px").set("background", "var(--lumo-warning-color-10pct)")
				.set("border", "1px solid var(--lumo-warning-color-50pct)").set("border-radius", "4px")
				.set("color", "var(--lumo-warning-text-color)");
		// Warning icon
		final Icon icon = VaadinIcon.WARNING.create();
		icon.setSize("16px");
		icon.getStyle().set("color", "var(--lumo-warning-color)");
		// Warning text
		final CSpan text = new CSpan(message);
		text.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-warning-text-color)");
		warningMessage.add(icon, text);
		// Add warning at the beginning of component (after toolbar if exists)
		if (toolbar != null) {
			getElement().insertChild(getElement().indexOfChild(toolbar.getElement()) + 1, warningMessage.getElement());
		} else {
			getElement().insertChild(0, warningMessage.getElement());
		}
	}
}
