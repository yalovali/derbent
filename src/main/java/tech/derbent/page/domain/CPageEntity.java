package tech.derbent.page.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CProjectItem;
import tech.derbent.page.view.CPageEntityView;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cpageentity")
@AttributeOverride (name = "id", column = @Column (name = "pageentity_id"))
public class CPageEntity extends CProjectItem<CPageEntity> {

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() { return "#207bff"; }

	public static String getIconFilename() { return "vaadin:tasks"; }

	public static Class<?> getViewClassStatic() { return CPageEntityView.class; }

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return getName();
	}

	public String getRoute() { return route; }

	public void setRoute(final String route) { this.route = route; }

	@Column (nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Page route", required = true, readOnly = false, defaultValue = "", description = "Page section to be displayed under",
			hidden = false, order = 70, maxLength = 100
	)
	private String route;
	@Column (nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Menu Order", required = true, readOnly = false, defaultValue = "1.1", description = "Menu Order", hidden = false,
			order = 70, maxLength = 100
	)
	private String menuOrder;
	@Column (nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Icon method/File", required = true, readOnly = false, defaultValue = "class:tech.derbent.meetings.view.CMeetingsView",
			description = "class:tech.derbent.meetings.view.CMeetingsView", hidden = false, order = 70, maxLength = 100
	)
	private String icon;

	public String getOrder() { return menuOrder; }

	public void setOrder(String order) { this.menuOrder = order; }

	public String getIcon() { return icon; }

	public void setIcon(String icon) { this.icon = icon; }

	public String getTitle() { return title; }

	public void setTitle(String title) { this.title = title; }

	public String getPageTitle() { return pageTitle; }

	public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

	@Column (nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Title", required = true, readOnly = false, defaultValue = "Project.Page",
			description = "Use like, Project.Page, separate parent with . ", hidden = false, order = 70, maxLength = 100
	)
	private String title;
	@Column (nullable = false, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Page Title", required = true, readOnly = false, defaultValue = "Title of Page", description = "Title of Page",
			hidden = false, order = 70, maxLength = 100
	)
	private String pageTitle;

	/** Default constructor for JPA. */
	public CPageEntity() {
		super();
	}

	public CPageEntity(final String name, final CProject project) {
		super(CPageEntity.class, name, project);
	}
}
