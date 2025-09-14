package tech.derbent.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import jakarta.annotation.security.PermitAll;
import tech.derbent.orders.domain.COrder;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.session.service.CSessionService;

/** Dynamic page view for rendering database-defined pages. This view displays content stored in CPageEntity instances. */
@PermitAll
public class CDynamicPageView extends VerticalLayout implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageView.class);
	private static final long serialVersionUID = 1L;
	private final CPageEntity pageEntity;
	private final CSessionService sessionService;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return "#102bff"; // Blue color for activity entities
	}

	public static String getStaticIconFilename() { return COrder.getStaticIconFilename(); }

	public CDynamicPageView(final CPageEntity pageEntity, final CSessionService sessionService) {
		this.pageEntity = pageEntity;
		this.sessionService = sessionService;
		LOGGER.debug("Creating dynamic page view for: {}", pageEntity.getPageTitle());
		initializePage();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// Security check
		if (pageEntity.getRequiresAuthentication() && sessionService.getActiveUser().isEmpty()) {
			LOGGER.warn("Unauthenticated access attempted to page: {}", pageEntity.getPageTitle());
			event.rerouteToError(IllegalAccessException.class, "Authentication required");
			return;
		}
		// Check if page is active
		if (!pageEntity.getIsActive()) {
			LOGGER.warn("Access attempted to inactive page: {}", pageEntity.getPageTitle());
			event.rerouteToError(IllegalStateException.class, "Page not available");
			return;
		}
		LOGGER.debug("User accessing page: {}", pageEntity.getPageTitle());
	}

	/** Initialize the page layout and content. */
	private void initializePage() {
		setSizeFull();
		setPadding(true);
		setSpacing(true);
		// Set page title for browser tab
		getElement().executeJs("document.title = $0", pageEntity.getPageTitle());
		// Create page header
		createPageHeader();
		// Create page content
		createPageContent();
		// Create page footer with metadata
		createPageFooter();
		LOGGER.debug("Dynamic page view initialized for: {}", pageEntity.getPageTitle());
	}

	/** Create the page header with title and description. */
	private void createPageHeader() {
		H1 pageTitle = new H1(pageEntity.getPageTitle());
		pageTitle.addClassNames("page-title");
		add(pageTitle);
		if (pageEntity.getDescription() != null && !pageEntity.getDescription().trim().isEmpty()) {
			Paragraph description = new Paragraph(pageEntity.getDescription());
			description.addClassNames("page-description");
			add(description);
		}
	}

	/** Create the main page content area. */
	private void createPageContent() {
		Div contentArea = new Div();
		contentArea.addClassNames("page-content");
		contentArea.setSizeFull();
		if (pageEntity.getContent() != null && !pageEntity.getContent().trim().isEmpty()) {
			// For now, render content as HTML
			// In a production system, you might want to use a more sophisticated
			// content rendering system (Markdown, rich text editor, etc.)
			contentArea.getElement().setProperty("innerHTML", sanitizeContent(pageEntity.getContent()));
		} else {
			// Default content for empty pages
			createDefaultContent(contentArea);
		}
		add(contentArea);
	}

	/** Create default content for pages without specific content. */
	private void createDefaultContent(Div contentArea) {
		H2 welcomeTitle = new H2("Welcome to " + pageEntity.getPageTitle());
		Paragraph welcomeText =
				new Paragraph("This is a dynamically generated page. " + "Content can be customized through the page management system.");
		if (pageEntity.getProject() != null) {
			Paragraph projectInfo = new Paragraph("This page belongs to project: " + pageEntity.getProject().getName());
			projectInfo.addClassNames("project-info");
			contentArea.add(welcomeTitle, welcomeText, projectInfo);
		} else {
			contentArea.add(welcomeTitle, welcomeText);
		}
		// Add some sample content based on page type
		addSampleContentByType(contentArea);
	}

	/** Add sample content based on the page name/type. */
	private void addSampleContentByType(Div contentArea) {
		String pageName = pageEntity.getName().toLowerCase();
		if (pageName.contains("overview")) {
			addOverviewContent(contentArea);
		} else if (pageName.contains("directory") || pageName.contains("team")) {
			addDirectoryContent(contentArea);
		} else if (pageName.contains("library") || pageName.contains("resource")) {
			addLibraryContent(contentArea);
		} else {
			addGenericContent(contentArea);
		}
	}

	private void addOverviewContent(Div contentArea) {
		H2 section = new H2("Project Overview");
		Paragraph content = new Paragraph("This section provides a comprehensive overview of the project objectives, "
				+ "current status, key milestones, and important information for team members.");
		contentArea.add(section, content);
	}

	private void addDirectoryContent(Div contentArea) {
		H2 section = new H2("Team Directory");
		Paragraph content = new Paragraph("Here you can find contact information for all team members, "
				+ "their roles, responsibilities, and current assignments within the project.");
		contentArea.add(section, content);
	}

	private void addLibraryContent(Div contentArea) {
		H2 section = new H2("Resource Library");
		Paragraph content = new Paragraph("This area contains important project documents, reference materials, "
				+ "templates, and other resources that team members may need.");
		contentArea.add(section, content);
	}

	private void addGenericContent(Div contentArea) {
		H2 section = new H2("Page Content");
		Paragraph content = new Paragraph("This page is ready for custom content. You can edit this page "
				+ "through the page management interface to add specific information.");
		contentArea.add(section, content);
	}

	/** Create page footer with metadata. */
	private void createPageFooter() {
		Div footer = new Div();
		footer.addClassNames("page-footer");
		if (pageEntity.getLastModifiedDate() != null) {
			Paragraph lastModified = new Paragraph("Last updated: " + pageEntity.getLastModifiedDate().toString());
			lastModified.addClassNames("last-modified");
			footer.add(lastModified);
		}
		add(footer);
	}

	/** Basic HTML sanitization for content. In a production system, use a proper HTML sanitization library. */
	private String sanitizeContent(String content) {
		if (content == null) {
			return "";
		}
		// Very basic sanitization - remove script tags
		return content.replaceAll("(?i)<script[^>]*>.*?</script>", "").replaceAll("(?i)<script[^>]*/>", "");
	}

	/** Get the page entity this view represents. */
	public CPageEntity getPageEntity() { return pageEntity; }
}
