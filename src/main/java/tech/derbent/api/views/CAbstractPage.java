package tech.derbent.api.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.theme.lumo.LumoUtility;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.interfaces.IPageTitleProvider;
import tech.derbent.api.ui.notifications.CNotificationService;

public abstract class CAbstractPage extends Main implements BeforeEnterObserver, IPageTitleProvider {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected CNotificationService notificationService;

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
		notificationService = CSpringContext.<CNotificationService>getBean(CNotificationService.class);
	};

	public CNotificationService getNotificationService() { return notificationService; }

	/** Initializes the page with necessary components and layout. */
	protected void initPage() {}

	/** Sets up the toolbar for the page. */
	protected abstract void setupToolbar();
}
