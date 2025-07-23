package tech.derbent.session.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import tech.derbent.session.service.LayoutService;
import tech.derbent.session.service.SessionService;

/**
 * Configuration class to handle the circular dependency between SessionService and LayoutService.
 */
@Configuration
public class SessionConfiguration {
    
    private final SessionService sessionService;
    private final LayoutService layoutService;
    
    public SessionConfiguration(final SessionService sessionService, final LayoutService layoutService) {
        this.sessionService = sessionService;
        this.layoutService = layoutService;
    }
    
    @PostConstruct
    public void configureServices() {
        sessionService.setLayoutService(layoutService);
    }
}