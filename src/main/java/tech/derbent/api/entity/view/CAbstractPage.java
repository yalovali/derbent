package tech.derbent.api.entity.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.theme.lumo.LumoUtility;
import tech.derbent.api.interfaces.IPageTitleProvider;

public abstract class CAbstractPage extends Main implements BeforeEnterObserver, IPageTitleProvider {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public CAbstractPage() {
		super();
		setSizeFull();
		addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, /* */
				LumoUtility.FlexDirection.COLUMN, /* */
				LumoUtility.Gap.SMALL, /* */
				LumoUtility.Padding.SMALL, /* */
				LumoUtility.Width.FULL);
		initPage();
		setupToolbar();
	}

	/** Initializes the page with necessary components and layout. */
	protected void initPage() { /*****/
	}

	/** Sets up the toolbar for the page. */
	protected abstract void setupToolbar();
}
