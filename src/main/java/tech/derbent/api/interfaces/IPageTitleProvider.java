package tech.derbent.api.interfaces;
/** Interface for components that can provide a page title for display in the main layout toolbar. This allows dynamic pages and other custom views to
 * specify their own page titles instead of relying on MenuConfiguration alone. */
public interface IPageTitleProvider {

	/** Gets the page title to be displayed in the main layout toolbar.
	 * @return the page title, or null if no custom title should be used */
	String getPageTitle();
}
