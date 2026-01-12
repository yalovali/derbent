package tech.derbent.api.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServiceInitListener;
import tech.derbent.app.ui.view.CDashboardView;

@Configuration
@Profile ("!bab")
public class CDerbentDashboardRouteConfiguration {

	@SuppressWarnings ("static-method")
	@Bean
	public VaadinServiceInitListener derbentDashboardRouteInitializer() {
		return event -> {
			final RouteConfiguration configuration = RouteConfiguration.forApplicationScope();
			configuration.setRoute("home", CDashboardView.class);
		};
	}
}
