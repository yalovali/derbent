package tech.derbent.abstracts.views;

import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.theme.lumo.LumoUtility;

public abstract class CAbstractPage extends Main implements BeforeEnterObserver {

	private static final long serialVersionUID = 1L;

	public CAbstractPage() {
		super();
		setSizeFull();
		addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
		setupToolbar();
	}

	/**
	 * Initializes the page with necessary components and layout.
	 */
	protected abstract void initPage();

	/**
	 * Sets up the toolbar for the page.
	 */
	protected abstract void setupToolbar();
}
