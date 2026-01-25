package tech.derbent.api.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServiceInitListener;
import tech.derbent.bab.ui.view.CBabDashboardView;

@Configuration
@Profile ("bab")
public class CBabDashboardRouteConfiguration {

	@SuppressWarnings ({})
	@Bean
	public VaadinServiceInitListener babDashboardRouteInitializer() {
		return event -> {
			final RouteConfiguration configuration = RouteConfiguration.forApplicationScope();
			configuration.setRoute("home", CBabDashboardView.class);
		};
	}
}
