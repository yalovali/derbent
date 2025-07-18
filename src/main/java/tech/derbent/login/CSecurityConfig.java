package tech.derbent.login;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

@EnableWebSecurity
@Configuration
class CSecurityConfig extends VaadinWebSecurity {

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		// this ensures that the application is properly configured. It is recommended
		// to use the default configuration provided by Vaadin.
		super.configure(http);
		setLoginView(http, CLoginView.class);
	}

	@Bean
	public UserDetailsManager userDetailsManager() {
		LoggerFactory.getLogger(CSecurityConfig.class).warn("NOT FOR PRODUCITON: Using in-memory user details manager!");
		final var user = User.withUsername("user").password("{noop}user").roles("USER").build();
		final var admin = User.withUsername("admin").password("{noop}admin").roles("ADMIN").build();
		return new InMemoryUserDetailsManager(user, admin);
	}
}