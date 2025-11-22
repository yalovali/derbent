package tech.derbent.api.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
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
import tech.derbent.api.utils.CPanelDetails;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

@org.springframework.stereotype.Component
public final class CDetailsBuilder implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDetailsBuilder.class);

	public static ApplicationContext getApplicationContext() { return applicationContext; }

	CFormBuilder<?> formBuilder = null;
	private HasComponents formLayout = null;
	private final Map<String, CPanelDetails> mapSectionPanels;
	private final ISessionService sessionService;

	public CDetailsBuilder(final ISessionService sessionService) {
		Check.notNull(sessionService, "Session service cannot be null");
		this.sessionService = sessionService;
		mapSectionPanels = new HashMap<>();
	}

	public HasComponents buildDetails(final IContentOwner contentOwner, CDetailSection screen, final CEnhancedBinder<?> binder,
			final HasComponents detailsLayout) throws Exception {
		Check.notNull(screen, "Screen cannot be null");
		Check.notNull(binder, "Binder cannot be null");
		Check.notNull(applicationContext, "Details name cannot be null");
		
		if (detailsLayout != null) {
			formLayout = detailsLayout;
		} else {
			formLayout = new FormLayout();
		}
		
		final CDetailSectionService screenService = applicationContext.getBean(CDetailSectionService.class);
		Check.notNull(screenService, "Screen service cannot be null");
		
		// For lazy loading of screen lines
		final PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		if (!persistenceUtil.isLoaded(screen, "screenLines")) {
			screen = screenService.findByIdWithScreenLines(screen.getId());
		}
		
		if ((screen.getScreenLines() == null) || screen.getScreenLines().isEmpty()) {
			LOGGER.warn("No lines found for screen: {}", screen.getName());
			return new FormLayout(); // Return an empty layout if no lines are present
		}
		
		final Class<?> screenClass = CEntityRegistry.getEntityClass(screen.getEntityType());
		Check.notNull(screenClass, "Screen class cannot be null");
		formBuilder = new CFormBuilder<>(null, screenClass, binder);
		
		final List<CDetailLines> lines = screen.getScreenLines();
		
		// First pass: check if we need a master TabSheet (if any top-level section has Tab=True)
		com.vaadin.flow.component.tabs.TabSheet masterTabSheet = null;
		for (final CDetailLines line : lines) {
			if (line.getRelationFieldName().equals(CEntityFieldService.SECTION_START) && Boolean.TRUE.equals(line.getSectionAsTab())) {
				// At least one top-level section needs tabs, create master TabSheet
				masterTabSheet = new com.vaadin.flow.component.tabs.TabSheet();
				formLayout.add(masterTabSheet);
				LOGGER.debug("Created master TabSheet for sections with Tab=True");
				break;
			}
		}
		
		// Stack to track nested CPanelDetails sections
		final Stack<CPanelDetails> containerStack = new Stack<>();
		
		// Process all lines iteratively
		for (int i = 0; i < lines.size(); i++) {
			final CDetailLines line = lines.get(i);
			final String relationField = line.getRelationFieldName();
			
			if (relationField.equals(CEntityFieldService.SECTION_START)) {
				LOGGER.debug("Creating section: {} (sectionAsTab: {}, depth: {})", 
					line.getFieldCaption(), line.getSectionAsTab(), containerStack.size());
				
				// ALL sections are CPanelDetails (accordions)
				final CPanelDetails newSection = new CPanelDetails(line.getSectionName(), line.getFieldCaption());
				mapSectionPanels.put(line.getSectionName(), newSection);
				LOGGER.debug("Created CPanelDetails: {}", line.getFieldCaption());
				
				// Determine where to add this section
				if (containerStack.isEmpty()) {
					// Top-level section
					if (Boolean.TRUE.equals(line.getSectionAsTab())) {
						// Add as tab to master TabSheet
						if (masterTabSheet != null) {
							masterTabSheet.add(line.getFieldCaption(), newSection);
							LOGGER.debug("Added CPanelDetails '{}' as tab to master TabSheet", line.getFieldCaption());
						}
					} else {
						// Add directly to formLayout as accordion
						formLayout.add(newSection);
						LOGGER.debug("Added CPanelDetails '{}' directly to formLayout", line.getFieldCaption());
					}
				} else {
					// Nested section: add to parent's base layout
					final CPanelDetails parentSection = containerStack.peek();
					parentSection.getBaseLayout().add(newSection);
					LOGGER.debug("Added nested CPanelDetails '{}' to parent's base layout", line.getFieldCaption());
				}
				
				// Push onto stack for potential nesting
				containerStack.push(newSection);
				
			} else if (relationField.equals(CEntityFieldService.SECTION_END)) {
				// Pop the current section from stack
				if (!containerStack.isEmpty()) {
					containerStack.pop();
					LOGGER.debug("Popped section from stack (remaining depth: {})", containerStack.size());
				} else {
					LOGGER.warn("Unmatched SECTION_END marker at line {}", i);
				}
				
			} else {
				// Regular field line - add to current section
				if (!containerStack.isEmpty()) {
					final CPanelDetails currentSection = containerStack.peek();
					currentSection.processLine(contentOwner, 0, screen, line, formBuilder);
				} else {
					LOGGER.warn("Field '{}' found outside of any section, skipping", line.getFieldCaption());
				}
			}
		}
		
		return formLayout;
	}

	public Component getComponentByName(final String panelName, final String componentName) {
		final CPanelDetails panel = getSectionPanel(panelName);
		Check.notNull(panel, "Panel cannot be null");
		return panel.getComponentByName(componentName);
	}

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

	public void setCurrentEntity(final CEntityDB<?> entity) {
		Check.notNull(getFormBuilder(), "Form builder cannot be null, first initialize it");
		getFormBuilder().setCurrentEntity(entity);
	}
}
