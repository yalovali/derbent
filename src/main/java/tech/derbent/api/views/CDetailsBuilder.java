package tech.derbent.api.views;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.tabs.TabSheet;
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
import tech.derbent.base.users.domain.CUser;

@org.springframework.stereotype.Component
public final class CDetailsBuilder implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDetailsBuilder.class);
	private static final int MAX_NESTING_LEVEL = 3;

	// Helper class to track container hierarchy
	private static class ContainerContext {
		CPanelDetails panel;
		HasComponents container;
		TabSheet tabSheet;
		int level;
		
		ContainerContext(CPanelDetails panel, HasComponents container, TabSheet tabSheet, int level) {
			this.panel = panel;
			this.container = container;
			this.tabSheet = tabSheet;
			this.level = level;
		}
	}

	public static ApplicationContext getApplicationContext() { return applicationContext; }

	private static Component processLine(final int counter, final CDetailSection screen, final CDetailLines line, 
			final String containerType) {
		Check.notNull(line, "Line cannot be null");
		if (line.getRelationFieldName().equals(CEntityFieldService.SECTION)) {
			final CPanelDetails sectionPanel = new CPanelDetails(line.getSectionName(), line.getFieldCaption(), containerType);
			return sectionPanel;
		}
		return null;
	}

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
		// for lazy loading of screen lines
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
		
		final CUser user = sessionService.getActiveUser().orElseThrow();
		final List<CDetailLines> lines = screen.getScreenLines();
		
		// Use a stack to manage hierarchical nesting
		Deque<ContainerContext> containerStack = new ArrayDeque<>();
		final int counter = 0;
		
		// Initialize root container
		containerStack.push(new ContainerContext(null, formLayout, null, 0));
		
		for (final CDetailLines line : lines) {
			final String relationFieldName = line.getRelationFieldName();
			final String containerType = line.getContainerType();
			
			// Handle SECTION_END - pop from stack
			if (CEntityFieldService.SECTION_END.equals(relationFieldName) || 
			    CEntityFieldService.CONTAINER_TYPE_SECTION_END.equals(containerType)) {
				if (containerStack.size() > 1) {
					containerStack.pop();
					LOGGER.debug("Closed container, current level: {}", containerStack.peek().level);
				} else {
					LOGGER.warn("Attempted to close root container, ignoring");
				}
				continue;
			}
			
			// Get current context
			ContainerContext currentContext = containerStack.peek();
			CPanelDetails currentPanel = currentContext.panel;
			HasComponents currentContainer = currentContext.container;
			int currentLevel = currentContext.level;
			
			// Handle new section/tab
			if (CEntityFieldService.SECTION.equals(relationFieldName)) {
				// Check nesting level
				if (currentLevel >= MAX_NESTING_LEVEL) {
					LOGGER.error("Maximum nesting level ({}) exceeded, skipping section: {}", 
						MAX_NESTING_LEVEL, line.getSectionName());
					continue;
				}
				
				// Determine container type - use line's containerType if specified, otherwise use SECTION
				String newContainerType = (containerType != null && !containerType.isEmpty()) 
					? containerType 
					: CEntityFieldService.CONTAINER_TYPE_SECTION;
				
				// Create new panel
				final Component component = processLine(counter, screen, line, newContainerType);
				if (component instanceof CPanelDetails) {
					CPanelDetails newPanel = (CPanelDetails) component;
					
					// Determine where to add this new panel based on parent type and new panel type
					if (currentPanel == null) {
						// Root level - add to form layout
						formLayout.add(newPanel);
					} else if (CEntityFieldService.CONTAINER_TYPE_TAB.equals(newContainerType)) {
						// Creating a TAB
						if (CEntityFieldService.CONTAINER_TYPE_SECTION.equals(currentPanel.getContainerType())) {
							// TAB inside SECTION - need to create/use TabSheet
							TabSheet tabSheet = currentPanel.getChildTabSheet();
							if (tabSheet == null) {
								tabSheet = new TabSheet();
								tabSheet.setWidthFull();
								currentPanel.getBaseLayout().add(tabSheet);
								currentPanel.setChildTabSheet(tabSheet);
							}
							tabSheet.add(line.getSectionName(), newPanel);
						} else {
							// TAB inside TAB - add to parent's TabSheet
							if (currentContext.tabSheet != null) {
								currentContext.tabSheet.add(line.getSectionName(), newPanel);
							} else {
								LOGGER.error("Cannot add TAB to TAB without TabSheet context");
								currentPanel.getBaseLayout().add(newPanel);
							}
						}
					} else {
						// Creating a SECTION
						if (CEntityFieldService.CONTAINER_TYPE_TAB.equals(currentPanel.getContainerType())) {
							// SECTION inside TAB - add to panel's base layout
							currentPanel.getBaseLayout().add(newPanel);
						} else {
							// SECTION inside SECTION - add to panel's base layout
							currentPanel.getBaseLayout().add(newPanel);
						}
					}
					
					// Push new context onto stack
					TabSheet contextTabSheet = null;
					if (CEntityFieldService.CONTAINER_TYPE_TAB.equals(newContainerType)) {
						contextTabSheet = currentContext.tabSheet; // Inherit parent's TabSheet if we're a tab
					}
					containerStack.push(new ContainerContext(newPanel, newPanel.getBaseLayout(), contextTabSheet, currentLevel + 1));
					mapSectionPanels.put(newPanel.getName(), newPanel);
					
					LOGGER.debug("Created {} '{}' at level {}", newContainerType, line.getSectionName(), currentLevel + 1);
				} else {
					LOGGER.error("First create a section! Line: {}", line.getFieldCaption());
				}
			} else {
				// Regular field line - add to current panel
				if (currentPanel != null) {
					currentPanel.processLine(contentOwner, counter, screen, line, getFormBuilder());
				} else {
					LOGGER.error("No current section to add field to: {}", line.getFieldCaption());
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

	public CFormBuilder<?> getFormBuilder() {
		return formBuilder;
	}
}
