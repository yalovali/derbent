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
import tech.derbent.api.interfaces.IDetailsContainer;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.ui.component.CDetailsTabSheet;
import tech.derbent.api.ui.component.CHorizontalLayout;
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
		
		// Stack to track nested containers
		final Stack<IDetailsContainer> containerStack = new Stack<>();
		
		// Root container - will hold all top-level items
		// Store containers, not their components, so we can populate them first
		final List<IDetailsContainer> rootContainers = new java.util.ArrayList<>();
		IDetailsContainer rootContainer = new IDetailsContainer() {
			@Override
			public void addItem(Component component) {
				// Not used - root stores containers, not components
			}
			
			@Override
			public void addItem(String name, Component component) {
				// Not used - root stores containers, not components
			}
			
			public void addContainer(IDetailsContainer container) {
				rootContainers.add(container);
			}
			
			@Override
			public com.vaadin.flow.component.orderedlayout.VerticalLayout getBaseLayout() {
				// Not used for root container
				return null;
			}
			
			@Override
			public Component asComponent() {
				// Not used for root container
				return null;
			}
		};
		
		containerStack.push(rootContainer);
		
		// Process all lines using a simple iterative approach
		for (int i = 0; i < lines.size(); i++) {
			final CDetailLines line = lines.get(i);
			final String relationField = line.getRelationFieldName();
			
			if (relationField.equals(CEntityFieldService.SECTION_START)) {
				// Log section creation for debugging
				LOGGER.debug("Creating section: {} (sectionAsTab: {}, depth: {})", 
					line.getFieldCaption(), line.getSectionAsTab(), containerStack.size() - 1);
				
				// Create container based on whether this section is marked as a tab
				// TabSheets can exist at any nesting level, not just top-level
				IDetailsContainer newContainer;
				
				if (Boolean.TRUE.equals(line.getSectionAsTab())) {
					// Create a TabSheet container (can be at any level)
					// TabSheet uses its caption and can contain fields, nested tabs, and panels
					newContainer = new CDetailsTabSheet(line.getFieldCaption());
					LOGGER.debug("Created TabSheet: {}", line.getFieldCaption());
				} else {
					// Create an accordion panel
					newContainer = new CPanelDetails(line.getSectionName(), line.getFieldCaption());
					mapSectionPanels.put(line.getSectionName(), (CPanelDetails) newContainer);
					LOGGER.debug("Created CPanelDetails: {}", line.getFieldCaption());
				}
				
				// Add the new container to the current container
				final IDetailsContainer currentContainer = containerStack.peek();
				final boolean isTopLevel = (containerStack.size() == 1);
				
				if (isTopLevel) {
					// Top-level sections: store container reference, convert to component later
					rootContainers.add(newContainer);
					LOGGER.debug("Added to root: {}", line.getFieldCaption());
				} else {
					// Nested sections: add based on parent type
					if (currentContainer instanceof CPanelDetails) {
						// Parent is accordion: add to its base layout
						((CPanelDetails) currentContainer).getBaseLayout().add(newContainer.asComponent());
						LOGGER.debug("Added to CPanelDetails base layout: {}", line.getFieldCaption());
					} else if (currentContainer instanceof CDetailsTabSheet) {
						// Parent is TabSheet: add as a named tab
						currentContainer.addItem(line.getSectionName(), newContainer.asComponent());
						LOGGER.debug("Added as tab to TabSheet: {}", line.getFieldCaption());
					}
				}
				
				// Push the new container onto the stack
				containerStack.push(newContainer);
				
			} else if (relationField.equals(CEntityFieldService.SECTION_END)) {
				// Pop the current container from the stack
				if (containerStack.size() > 1) {
					final IDetailsContainer popped = containerStack.pop();
					LOGGER.debug("Popped container from stack (remaining depth: {})", containerStack.size() - 1);
				} else {
					LOGGER.warn("Unmatched SECTION_END marker at line {}", i);
				}
				
			} else {
				// Regular field line - add to the current container
				final IDetailsContainer currentContainer = containerStack.peek();
				
				if (currentContainer instanceof CPanelDetails) {
					// Use the existing processLine method for CPanelDetails
					((CPanelDetails) currentContainer).processLine(contentOwner, 0, screen, line, formBuilder);
				} else if (currentContainer instanceof CDetailsTabSheet) {
					// Fields inside TabSheet are added directly to its base layout
					// TabSheets can contain regular fields alongside tabs
					// Reuse component maps from the container or create new ones if needed
					final Map<String, Component> componentMap = new java.util.HashMap<>();
					final Map<String, CHorizontalLayout> horizontalLayoutMap = new java.util.HashMap<>();
					formBuilder.addFieldLine(contentOwner, screen.getEntityType(), line, 
						((CDetailsTabSheet) currentContainer).getBaseLayout(), 
						componentMap, horizontalLayoutMap);
				} else {
					// Fields should always be inside CPanelDetails or TabSheet sections
					// This case shouldn't occur with properly configured screen definitions
					// Log warning and skip the field (consistent with old behavior)
					LOGGER.warn("Field '{}' found outside of CPanelDetails/TabSheet section, skipping", line.getFieldCaption());
				}
			}
		}
		
		// Add all root-level containers to the form layout
		// Convert containers to components only after they're fully populated
		for (IDetailsContainer container : rootContainers) {
			formLayout.add(container.asComponent());
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
