package tech.derbent.bab.uiobjects.view;

import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.bab.dashboard.view.CComponentInterfaceList;

/** CComponentBabBase - Base class for BAB profile display components.
 * <p>
 * This base class provides a standard pattern for BAB components that:
 * <ul>
 * <li>Display data from external services (e.g., Calimero HTTP API)</li>
 * <li>Do not require form binding (display-only components)</li>
 * <li>Follow the Derbent component initialization lifecycle</li>
 * </ul>
 * <p>
 * Subclasses must implement:
 * <ul>
 * <li>{@link #initializeComponents()} - Build UI components and layout</li>
 * <li>{@link #refreshComponent()} - Refresh component data from service</li>
 * </ul>
 * <p>
 * Pattern:
 *
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
 * 		createToolbar();
 * 		createGrid();
 * 		loadData();
 * 	}
 *
 * 	&#64;Override
 * 	protected void refreshComponent() {
 * 		loadData();
 * 	}
 * }
 * </pre>
 *
 * @see CComponentInterfaceList Example implementation */
public abstract class CComponentBabBase extends CVerticalLayout {

	private static final long serialVersionUID = 1L;

	protected CComponentBabBase() {
		super();
	}

	/** Configure component styling and behavior. Subclasses can override to customize. */
	protected void configureComponent() {
		setSpacing(false);
		setPadding(false);
		getStyle().set("gap", "12px");
		setMaxHeight("250px");
	}

	/** Initialize component UI and layout. Called once during construction. Subclasses must implement to build UI components. */
	protected abstract void initializeComponents();
	/** Refresh component data from service. Called when data needs to be reloaded. Subclasses must implement to update displayed data. */
	protected abstract void refreshComponent();
}
