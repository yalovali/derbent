package tech.derbent.page.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;

@Entity
@Table (name = "cpageentity", uniqueConstraints = @UniqueConstraint (columnNames = {
		"menu_title", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "pageentity_id"))
public class CPageEntity extends CProjectItem<CPageEntity> {

	public static final String DEFAULT_COLOR = "#6f42c1";
	public static final String DEFAULT_ICON = "vaadin:file";
	public static final String VIEW_NAME = "Pages View";
	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "false",
			description = "Whether this page entity cannot be deleted by users", hidden = false, order = 82
	)
	private boolean attributeNonDeletable = false;
	@Column (nullable = false)
	@AMetaData (
			displayName = "Read Only", required = false, readOnly = false, defaultValue = "false",
			description = "Whether this page entity is read-only and cannot be modified", hidden = false, order = 81
	)
	private boolean attributeReadonly = false;
	@Column (name = "attributeShowInQuickToolbar", nullable = false)
	@AMetaData (
			displayName = "Show in Quick Toolbar", required = false, readOnly = false, defaultValue = "false",
			description = "Whether this page should appear as a button in the quick access toolbar", hidden = false, order = 85
	)
	private boolean attributeShowInQuickToolbar = false;
	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = "#4A90E2", colorField = true,
			description = "Hex color code (e.g., #4A90E2)", hidden = false, order = 3, maxLength = 7
	)
	private String color = "#4A90E2";
	@Column (nullable = true, length = 10000)
	@Size (max = 10000)
	@AMetaData (
			displayName = "Page Content", required = false, readOnly = false, defaultValue = "", description = "HTML content of the page",
			hidden = false, order = 90, maxLength = 10000
	)
	private String content;
	@ManyToOne
	@JoinColumn (name = "detail_section_id")
	@AMetaData (
			displayName = "Detail Section", required = false, readOnly = false, description = "Detail section configuration for this page",
			hidden = false, order = 99
	)
	private CDetailSection detailSection;
	@ManyToOne
	@JoinColumn (name = "grid_entity_id")
	@AMetaData (
			displayName = "Grid Entity", required = false, readOnly = false, description = "Grid entity configuration for this page", hidden = false,
			dataProviderMethod = "listForComboboxSelectorByProject", dataProviderBean = "CGridEntityService", dataProviderParamBean = "session",
			dataProviderParamMethod = "getActiveProject", order = 98
	)
	private CGridEntity gridEntity;
	@Column (nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Icon", required = true, readOnly = false, defaultValue = "vaadin:file", description = "Icon for the page menu item",
			hidden = false, order = 70, maxLength = 100, useIcon = true
	)
	private String icon;
	@Column (nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Menu Order", required = true, readOnly = false, defaultValue = "1.1", description = "Menu Order", hidden = false,
			order = 70, maxLength = 100
	)
	private String menuOrder;
	@Column (name = "menu_title", nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Menu Title", required = true, readOnly = false, defaultValue = "Project.Page",
			description = "Use like, Project.Page, separate parent with . ", hidden = false, order = 70, maxLength = 100
	)
	private String menuTitle;
	@Column (name = "page_service", nullable = true, length = 200)
	@AMetaData (
			displayName = "Page Service", required = false, readOnly = false, defaultValue = "",
			description = "Backend service associated with this page", hidden = true, order = 100, maxLength = 200,
			dataProviderMethod = "getPageServiceList", dataProviderBean = "CPageServiceUtility"
	)
	private String pageService;
	@Column (nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Page Title", required = true, readOnly = false, defaultValue = "Title of Page", description = "Title of Page",
			hidden = false, order = 70, maxLength = 100
	)
	private String pageTitle;
	@Column (nullable = false)
	@AMetaData (
			displayName = "Requires Authentication", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this page requires user authentication", hidden = false, order = 80
	)
	private boolean requiresAuthentication = true;

	/** Default constructor for JPA. */
	public CPageEntity() {
		super();
		initializeDefaults();
	}

	public CPageEntity(final String name, final CProject project) {
		super(CPageEntity.class, name, project);
		initializeDefaults();
	}

	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	public boolean getAttributeReadonly() { return attributeReadonly; }

	public boolean getAttributeShowInQuickToolbar() { return attributeShowInQuickToolbar; }

	public String getColor() { return color; }

	public String getContent() { return content; }

	public CDetailSection getDetailSection() { return detailSection; }

	public CGridEntity getGridEntity() { return gridEntity; }

	public String getIcon() { return icon; }

	public String getMenuOrder() { return menuOrder; }

	public String getMenuTitle() { return menuTitle; }

	public String getPageTitle() { return pageTitle; }

	public boolean getRequiresAuthentication() { return requiresAuthentication; }

	public String getRoute() { return "cdynamicpagerouter/" + getId(); }

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships
		if (detailSection != null) {
			detailSection.getName(); // Trigger detail section loading
		}
		if (gridEntity != null) {
			gridEntity.getName(); // Trigger grid entity loading
		}
		// Parent class relationships (from CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		icon = DEFAULT_ICON;
		menuOrder = "10.110";
		menuTitle = "System.New Page";
		pageTitle = "New Dynamic Page";
		requiresAuthentication = true;
		attributeReadonly = false;
		attributeNonDeletable = false;
		attributeShowInQuickToolbar = false;
		content = "<div style=\"padding: 20px;\">" + "<h1 style=\"color: #2196F3; margin-bottom: 16px;\">📄 Dynamic Page</h1>"
				+ "<p style=\"font-size: 16px; line-height: 1.6; color: #666;\">This is a customizable dynamic page. "
				+ "You can edit the content, configure navigation, and set up data grids through the administration interface.</p>"
				+ "<div style=\"background: #f5f5f5; padding: 16px; border-radius: 8px; margin-top: 20px;\">"
				+ "<h3 style=\"margin-top: 0; color: #333;\">✨ Features</h3>" + "<ul style=\"margin: 0; color: #666;\">"
				+ "<li>Custom HTML content</li>" + "<li>Configurable navigation menu</li>" + "<li>Data grid integration</li>"
				+ "<li>Role-based access control</li>" + "</ul></div></div>";
	}

	public void setAttributeNonDeletable(boolean attributeNonDeletable) { this.attributeNonDeletable = attributeNonDeletable; }

	public void setAttributeReadonly(boolean attributeReadonly) { this.attributeReadonly = attributeReadonly; }

	public void setAttributeShowInQuickToolbar(boolean showInQuickToolbar) { attributeShowInQuickToolbar = showInQuickToolbar; }

	public void setColor(String color) { this.color = color; }

	public void setContent(String content) { this.content = content; }

	public void setDetailSection(CDetailSection detailSection) { this.detailSection = detailSection; }

	public void setGridEntity(CGridEntity gridEntity) { this.gridEntity = gridEntity; }

	public void setIcon(String icon) { this.icon = icon; }

	public void setMenuOrder(String menuOrder) { this.menuOrder = menuOrder; }

	public void setMenuTitle(String title) { menuTitle = title; }

	public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

	public void setRequiresAuthentication(boolean requiresAuthentication) { this.requiresAuthentication = requiresAuthentication; }
}
