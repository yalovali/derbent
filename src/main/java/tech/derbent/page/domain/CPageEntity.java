package tech.derbent.page.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CProjectItem;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.page.view.CPageEntityView;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cpageentity")
@AttributeOverride (name = "id", column = @Column (name = "pageentity_id"))
public class CPageEntity extends CProjectItem<CPageEntity> {

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() { return "#207bff"; }

	public static String getStaticIconFilename() { return "vaadin:tasks"; }

	public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { return CPageEntityView.class; }

	@Column (nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Icon method/File", required = false, readOnly = false, defaultValue = "vaadin:tasks",
			description = "Icon for the page in the menu (e.g., 'vaadin:tasks' or 'class:full.class.name')", hidden = false, order = 30, maxLength = 100
	)
	private String icon;

	@Column (nullable = true, length = 20)
	@Size (max = 20)
	@AMetaData (
			displayName = "Menu Order", required = false, readOnly = false, defaultValue = "1.1", 
			description = "Menu order for hierarchical display (e.g., '1.1', '2.3.1')", hidden = false, order = 40, maxLength = 20
	)
	private String menuOrder;

	@Column (nullable = false, length = 100)
	@Size (max = 100)
	@NotBlank (message = "Page title is required")
	@AMetaData (
			displayName = "Page Title", required = true, readOnly = false, defaultValue = "New Page", 
			description = "Title displayed in browser tab and page header", hidden = false, order = 10, maxLength = 100
	)
	private String pageTitle;

	@Column (nullable = false, length = 100, unique = true)
	@Size (max = 100)
	@NotBlank (message = "Route is required")
	@AMetaData (
			displayName = "Page Route", required = true, readOnly = false, defaultValue = "", 
			description = "Unique URL route for the page (e.g., 'project-overview')", hidden = false, order = 20, maxLength = 100
	)
	private String route;

	@Column (nullable = false, length = 100)
	@Size (max = 100)
	@NotBlank (message = "Menu title is required")
	@AMetaData (
			displayName = "Menu Title", required = true, readOnly = false, defaultValue = "Pages.NewPage",
			description = "Hierarchical menu title (e.g., 'Project.Overview' for nested menus)", hidden = false, order = 15, maxLength = 100
	)
	private String title;

	@ManyToOne
	@JoinColumn (name = "parent_page_id")
	@AMetaData (
			displayName = "Parent Page", required = false, readOnly = false, defaultValue = "",
			description = "Parent page for hierarchical organization", hidden = false, order = 50
	)
	private CPageEntity parentPage;

	@Lob
	@Column (nullable = true, columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Page Content", required = false, readOnly = false, defaultValue = "",
			description = "HTML/Markdown content for the page", hidden = false, order = 60
	)
	private String content;

	@Column (nullable = false)
	@AMetaData (
			displayName = "Is Active", required = true, readOnly = false, defaultValue = "true",
			description = "Whether the page is active and visible in menus", hidden = false, order = 70
	)
	private Boolean isActive = true;

	@Column (nullable = false)
	@AMetaData (
			displayName = "Requires Authentication", required = true, readOnly = false, defaultValue = "true",
			description = "Whether the page requires user authentication", hidden = false, order = 80
	)
	private Boolean requiresAuthentication = true;

	/** Default constructor for JPA. */
	public CPageEntity() {
		super();
		initializeDefaults();
	}

	public CPageEntity(final String name, final CProject project) {
		super(CPageEntity.class, name, project);
		initializeDefaults();
	}

	public CPageEntity(final String name, final String pageTitle, final String route, final CProject project) {
		super(CPageEntity.class, name, project);
		this.pageTitle = pageTitle;
		this.route = route;
		initializeDefaults();
	}

	@Override
	public String getDisplayName() {
		return pageTitle != null ? pageTitle : getName();
	}

	public String getContent() { return content; }

	public String getIcon() { return icon; }

	public Boolean getIsActive() { return isActive; }

	public String getMenuOrder() { return menuOrder; }

	public String getPageTitle() { return pageTitle; }

	public CPageEntity getParentPage() { return parentPage; }

	public Boolean getRequiresAuthentication() { return requiresAuthentication; }

	public String getRoute() { return route; }

	public String getTitle() { return title; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (icon == null) {
			icon = getStaticIconFilename();
		}
		if (menuOrder == null) {
			menuOrder = "9.0"; // Default to lower priority
		}
		if (route == null) {
			route = "page-" + System.currentTimeMillis(); // Generate unique route
		}
		if (title == null) {
			title = "Pages.NewPage";
		}
		if (pageTitle == null) {
			pageTitle = "New Page";
		}
		if (isActive == null) {
			isActive = true;
		}
		if (requiresAuthentication == null) {
			requiresAuthentication = true;
		}
	}

	public void setContent(String content) { this.content = content; }

	public void setIcon(String icon) { this.icon = icon; }

	public void setIsActive(Boolean isActive) { this.isActive = isActive; }

	public void setMenuOrder(String menuOrder) { this.menuOrder = menuOrder; }

	public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

	public void setParentPage(CPageEntity parentPage) { this.parentPage = parentPage; }

	public void setRequiresAuthentication(Boolean requiresAuthentication) { this.requiresAuthentication = requiresAuthentication; }

	public void setRoute(final String route) { this.route = route; }

	public void setTitle(String title) { this.title = title; }

	@Override
	public Class<? extends CAbstractEntityDBPage<?>> getViewClass() { // TODO Auto-generated method stub
		return CPageEntity.getViewClassStatic();
	}
}
