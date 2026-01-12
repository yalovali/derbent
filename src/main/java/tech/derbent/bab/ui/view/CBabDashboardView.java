package tech.derbent.bab.ui.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Profile;
import tech.derbent.api.entity.view.CAbstractPage;
import tech.derbent.api.ui.component.basic.CH1;
import tech.derbent.api.ui.component.basic.CSpan;

/** Minimal BAB dashboard view for the bab profile. */
@Route (value = "home", registerAtStartup = false)
@PageTitle ("Dashboard")
@PermitAll
@Profile ("bab")
public final class CBabDashboardView extends CAbstractPage {

	public static final String DEFAULT_COLOR = "#6B5FA7";
	public static final String DEFAULT_ICON = "vaadin:dashboard";
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "BAB Dashboard View";

	@Override
	protected void initPage() {
		add(new CH1("BAB Gateway"), new CSpan("Hello from the BAB dashboard."));
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) { /****/ }

	@Override
	public String getPageTitle() { return "Dashboard"; }

	@Override
	protected void setupToolbar() { /****/ }
}
