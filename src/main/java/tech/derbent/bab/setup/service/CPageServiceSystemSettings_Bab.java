package tech.derbent.bab.setup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.calimero.service.CCalimeroProcessManager;
import tech.derbent.bab.setup.domain.CSystemSettings_Bab;
import tech.derbent.bab.setup.view.CComponentCalimeroStatus;
import tech.derbent.base.setup.service.CPageServiceSystemSettings;

/** CPageServiceSystemSettings_Bab - BAB IoT Gateway system settings page service. Layer: Service (MVC) Active when: 'bab' profile is active Provides
 * page management functionality for BAB gateway system settings. Follows Derbent pattern: Concrete class marked final. */
@Service
@Profile ("bab")
public final class CPageServiceSystemSettings_Bab extends CPageServiceSystemSettings<CSystemSettings_Bab> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSystemSettings_Bab.class);
	private final CCalimeroProcessManager calimeroProcessManager;

	public CPageServiceSystemSettings_Bab(final IPageServiceImplementer<CSystemSettings_Bab> view) {
		super(view);
		// Initialize Calimero process manager from Spring context
		CCalimeroProcessManager manager = null;
		try {
			manager = CSpringContext.getBean(CCalimeroProcessManager.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CCalimeroProcessManager - Calimero management will not be available", e);
		}
		this.calimeroProcessManager = manager;
	}

	/** Create Calimero status component for displaying and managing Calimero service.
	 * <p>
	 * Creates a value-bound component that supports Vaadin binding. The component automatically fires ValueChangeEvents when settings are modified by
	 * the user. The page service should register a value change listener to handle save operations.
	 * <p>
	 * Usage pattern:
	 * 
	 * <pre>
	 * CComponentCalimeroStatus component = (CComponentCalimeroStatus) createComponentCComponentCalimeroStatus();
	 * component.setValue(currentSettings);
	 * component.addValueChangeListener(event -> saveSettings(event.getValue()));
	 * </pre>
	 * 
	 * @return Component for Calimero service management */
	public Component createComponentCComponentCalimeroStatus() {
		try {
			LOGGER.debug("Creating Calimero status component");
			final CComponentCalimeroStatus component = new CComponentCalimeroStatus(calimeroProcessManager);
			// Register value change listener (replaces callback pattern)
			component.addValueChangeListener(event -> {
				LOGGER.debug("Calimero settings changed via component: enabled={}, path={}",
						event.getValue().getEnableCalimeroService(), event.getValue().getCalimeroExecutablePath());
				// Here you could trigger save if needed, or let form binder handle it
				// For now, just log the change
			});
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating Calimero status component: {}", e.getMessage(), e);
			// Fallback to error message
			final Div errorDiv = new Div();
			errorDiv.add(new H3("Error loading Calimero status component"));
			errorDiv.add(new com.vaadin.flow.component.html.Paragraph("Error: " + e.getMessage()));
			errorDiv.getStyle().set("color", "var(--lumo-error-color)");
			return errorDiv;
		}
	}
}
