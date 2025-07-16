package tech.derbent.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;

import tech.derbent.security.service.CLoginUserDetailsService;

/**
 * Security configuration using CLogin entity for authentication.
 * 
 * This configuration is enabled by setting the property:
 * application.security.use-clogin=true
 */
@EnableWebSecurity
@Configuration
@Import({ VaadinAwareSecurityContextHolderStrategyConfiguration.class })
@ConditionalOnProperty(name = "application.security.use-clogin", havingValue = "true")
public class CLoginSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(CLoginSecurityConfig.class);

    public CLoginSecurityConfig() {
        log.info("Using CLogin-based security configuration");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer
                .loginView("/login"))
                .formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error"))
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(CLoginUserDetailsService cLoginUserDetailsService) {
        return cLoginUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}