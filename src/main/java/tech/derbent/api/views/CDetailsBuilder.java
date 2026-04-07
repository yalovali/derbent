package tech.derbent.api.views;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.formlayout.FormLayout;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.ui.component.basic.CTabSheet;
import tech.derbent.api.utils.CPanelDetails;
import tech.derbent.api.utils.Check;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;

@org.springframework.stereotype.Component
public final class CDetailsBuilder implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDetailsBuilder.class);

	public static ApplicationContext getApplicationContext() { return applicationContext; }

	private static Component processLine(final CDetailLines line, final CUser user) {
		Check.notNull(line, "Line cannot be null");
		if (!line.getRelationFieldName().equals(CEntityFieldService.SECTION_START)) {
			return null;
		}
		return new CPanelDetails(line.getSectionName(), line.getFieldCaption(), user);
	}

	// Centralized maps - store ALL components and layouts from ALL panels.
	// CRITICAL: These maps must be shared across all panels so that CPageService
	// can retrieve components/layouts regardless of which section they belong to.
	private final Map<String, Component> componentMap;
	private final Map<String, tech.derbent.api.ui.component.basic.CHorizontalLayout> horizontalLayoutMap;
	private CFormBuilder<?> formBuilder = null;
	private HasComponents formLayout = null;
	private final Map<String, CPanelDetails> mapSectionPanels;
	private final ISessionService sessionService;
	private CTabSheet tabsOfForm;

	public CDetailsBuilder(final ISessionService sessionService) {
		Check.notNull(sessionService, "Session service cannot be null");
		this.sessionService = sessionService;
		mapSectionPanels = new HashMap<>();
		// Initialize centralized maps to be shared with all child panels/sections
		componentMap = new HashMap<>();
		horizontalLayoutMap = new HashMap<>();
	}

	public HasComponents buildDetails(final IContentOwner contentOwner, final CDetailSection screen, final CEnhancedBinder<?> binder,
			final HasComponents detailsLayout) throws Exception {
		// CRITICAL: Basic parameters must be present before attempting to build the screen.
		Check.notNull(screen, "DetailSection (screen) must not be null for buildDetails. Ensure the database contains a valid screen record.");
		Check.notNull(binder, "Binder must not be null for buildDetails. Binders are required for automated UI data binding.");
		Check.notNull(applicationContext, "Spring ApplicationContext is required for data resolution in CDetailsBuilder (must be set by ApplicationContextAware).");
		
		// CRITICAL: Centralized maps are mandatory to support cross-panel lookups (e.g., CPageService needing a component from another section).
		Check.notNull(componentMap, "Centralized componentMap must be initialized before build. Ensure constructor was called correctly.");
		Check.notNull(horizontalLayoutMap, "Centralized horizontalLayoutMap must be initialized before build. Required for cross-panel layout lookups.");
		
		formLayout = detailsLayout != null ? detailsLayout : new FormLayout();
		if (formLayout instanceof HasStyle) {
			((HasStyle) formLayout).addClassName("cdetailsbuilder-form-layout");
		}
		final CDetailSectionService screenService = applicationContext.getBean(CDetailSectionService.class);
		Check.notNull(screenService, "CDetailSectionService could not be retrieved from context.");
		
		CDetailSection screenToUse = screen;
		// CRITICAL: Hibernate lazy loading check for screen lines. We must have lines loaded before processing.
		final PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		if (!persistenceUtil.isLoaded(screenToUse, "screenLines")) {
			screenToUse = screenService.findByIdWithScreenLines(screenToUse.getId());
		}
		if (screenToUse.getScreenLines() == null || screenToUse.getScreenLines().isEmpty()) {
			LOGGER.warn("No lines found for screen: {}. Grid will be empty.", screenToUse.getName());
			return new FormLayout(); // Return an empty layout if no lines are present
		}
		final Class<?> screenClass = CEntityRegistry.getEntityClass(screenToUse.getEntityType());
		Check.notNull(screenClass, "Screen class not found for entity type: " + screenToUse.getEntityType());
		
		// Initialize the form builder with centralized maps to support the PageService pattern.
		formBuilder = new CFormBuilder<>(null, screenClass, binder, componentMap, horizontalLayoutMap);
		//
		CPanelDetails currentSection = null;
		final CUser user = sessionService.getActiveUser().orElseThrow(() -> new IllegalStateException("No active user found in session during screen build"));
		// screen.getScreenLines().size(); // Ensure lines are loaded
		if (user.getAttributeDisplaySectionsAsTabs()) {
			// LOGGER.debug("User '{}' prefers sections as tabs.", user.getUsername());
			tabsOfForm = new CTabSheet();
			formLayout.add(tabsOfForm);
		} else {
			// LOGGER.debug("User '{}' prefers sections as accordion.", user.getUsername());
		}
		final List<CDetailLines> lines = screenToUse.getScreenLines();
		for (final CDetailLines line : lines) {
			if (line.getRelationFieldName().equals(CEntityFieldService.SECTION_START)) {
				// no more current section. switch to base
				currentSection = null;
			}
			if (currentSection != null) {
				// CRITICAL: Pass centralized maps to the section so it registers its components/layouts globally
				currentSection.processLine(contentOwner, screenToUse, line, getFormBuilder(), componentMap, horizontalLayoutMap);
				continue;
			}
			final Component component = processLine(line, user);
			if (component instanceof CPanelDetails) {
				if (user.getAttributeDisplaySectionsAsTabs()) {
					tabsOfForm.add(line.getSectionName(), component);
				} else {
					formLayout.add(component);
				}
				currentSection = (CPanelDetails) component;
				mapSectionPanels.put(currentSection.getName(), currentSection);
			} else {
				LOGGER.error("Configuration error: Line '{}' processed before any section was created.", line.getFieldCaption());
			}
		}
		return formLayout;
	}

	/** Gets the centralized component map that contains all components from all panels.
	 * @return the component map */
	public Map<String, Component> getComponentMap() { return componentMap; }

	/** Gets the centralized horizontal layout map that contains all layouts from all panels.
	 * @return the horizontal layout map */
	public Map<String, tech.derbent.api.ui.component.basic.CHorizontalLayout> getHorizontalLayoutMap() { return horizontalLayoutMap; }

	public CFormBuilder<?> getFormBuilder() { return formBuilder; }

	public CPanelDetails getSectionPanel(final String sectionName) {
		Check.notNull(sectionName, "Section name cannot be null");
		return mapSectionPanels.get(sectionName);
	}

	/** Clears the details form by setting the form builder bean to null. */
	public void populateForm() {
		if (getFormBuilder() != null) {
			getFormBuilder().populateForm();
		}
	}

	/** Sets the application context and initializes the data provider resolver. This method is called automatically by Spring.
	 * @param context the Spring application context */
	@Override
	public void setApplicationContext(final ApplicationContext context) {
		// Store the application context for String data provider resolution
		CDetailsBuilder.applicationContext = context;
	}

	/** Sets the bean value for the form and its child components. 
	 * CRITICAL: This is Step 1 of the Mandatory 3-step population pattern. */
	public void setValue(final CEntityDB<?> entity) {
		Check.notNull(getFormBuilder(), "Form builder cannot be null. buildDetails() must be called before setValue().");
		getFormBuilder().setValue(entity);
	}
}
