package tech.derbent.base.login.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;

@AnonymousAllowed
@Route (value = "chart", autoLayout = false)
@PageTitle ("Chart Test View")
@Menu (order = 100.1, icon = "class:tech.derbent.base.setup.view.CSystemSettingsView", title = "Chart Test")
@PermitAll // When security is enabled, allow all authenticated users
public class CChartTestView extends Main {

	private static final long serialVersionUID = 1L;

	public CChartTestView() {
		add(new Div("This is a custom login view"));
	}
}
