package tech.derbent.abstracts.views;

public abstract class CAbstractMDPage extends CAbstractPage {

	private static final long serialVersionUID = 1L;

	protected CAbstractMDPage() {
		super();
		addClassNames("md-page");
		setupContent();
	}

	/**
	 * Initializes the page with necessary components and layout.
	 */
	@Override
	protected abstract void initPage();

	/**
	 * Sets up the main content area of the page.
	 */
	protected abstract void setupContent();

	/**
	 * Sets up the toolbar for the page.
	 */
	@Override
	protected abstract void setupToolbar();
}
