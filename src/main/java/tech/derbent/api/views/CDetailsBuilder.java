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

	/** Helper class to track section processing state during recursive parsing. */
	private static class SectionContext {

		int currentIndex;
		final List<CDetailLines> lines;

		SectionContext(final List<CDetailLines> lines, final int startIndex) {
			this.lines = lines;
			this.currentIndex = startIndex;
		}
	}

	private static ApplicationContext applicationContext;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDetailsBuilder.class);

	public static ApplicationContext getApplicationContext() { return applicationContext; }

	/** Processes lines for a section between SECTION_START and SECTION_END markers.
	 * @param context          The section context tracking current position in lines
	 * @param contentOwner     The content owner
	 * @param screen           The detail section screen
	 * @param user             The current user
	 * @param currentSection   The current CPanelDetails section to add fields to
	 * @param formBuilder      The form builder for creating fields
	 * @param mapSectionPanels Map to store section panels by name
	 * @return The index of the SECTION_END line, or end of list if not found */
	private static int processSectionLines(final SectionContext context, final IContentOwner contentOwner, final CDetailSection screen,
			final CUser user, final CPanelDetails currentSection, final CFormBuilder<?> formBuilder,
			final Map<String, CPanelDetails> mapSectionPanels) throws Exception {
		while (context.currentIndex < context.lines.size()) {
			final CDetailLines line = context.lines.get(context.currentIndex);
			// Check for section end marker
			if (line.getRelationFieldName().equals(CEntityFieldService.SECTION_END)) {
				return context.currentIndex;
			}
			// Check for nested section start
			if (line.getRelationFieldName().equals(CEntityFieldService.SECTION_START)) {
				// Create nested section panel
				final CPanelDetails nestedSection = new CPanelDetails(line.getSectionName(), line.getFieldCaption(), user);
				mapSectionPanels.put(nestedSection.getName(), nestedSection);
				// Add nested section to current section's base layout
				currentSection.getBaseLayout().add(nestedSection);
				// Move to next line and process nested section content
				context.currentIndex++;
				context.currentIndex = processSectionLines(context, contentOwner, screen, user, nestedSection, formBuilder, mapSectionPanels);
			} else {
				// Regular field line - process it
				currentSection.processLine(contentOwner, 0, screen, line, formBuilder);
			}
			context.currentIndex++;
		}
		return context.currentIndex;
	}

	CFormBuilder<?> formBuilder = null;
	private HasComponents formLayout = null;
	private final Map<String, CPanelDetails> mapSectionPanels;
	private final ISessionService sessionService;
	private TabSheet tabsOfForm;

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
		// Initialize tabs if user prefers sections as tabs
		if (user.getAttributeDisplaySectionsAsTabs()) {
			tabsOfForm = new TabSheet();
			formLayout.add(tabsOfForm);
		}
		// Process all lines sequentially, handling sections with SECTION_START/SECTION_END
		final SectionContext context = new SectionContext(lines, 0);
		while (context.currentIndex < lines.size()) {
			final CDetailLines line = lines.get(context.currentIndex);
			if (line.getRelationFieldName().equals(CEntityFieldService.SECTION_START)) {
				// Create top-level section
				final CPanelDetails section = new CPanelDetails(line.getSectionName(), line.getFieldCaption(), user);
				mapSectionPanels.put(section.getName(), section);
				// Add section to appropriate container (tabs or accordion)
				if (user.getAttributeDisplaySectionsAsTabs() || line.getSectionAsTab()) {
					tabsOfForm.add(line.getSectionName(), section);
				} else {
					formLayout.add(section);
				}
				// Process section content (move to next line and process until SECTION_END)
				context.currentIndex++;
				context.currentIndex = processSectionLines(context, contentOwner, screen, user, section, formBuilder, mapSectionPanels);
			} else if (!line.getRelationFieldName().equals(CEntityFieldService.SECTION_END)) {
				// Line outside of any section - log warning
				LOGGER.warn("Line '{}' found outside of any section, skipping", line.getFieldCaption());
			}
			context.currentIndex++;
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
