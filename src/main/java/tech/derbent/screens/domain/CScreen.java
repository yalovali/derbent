package tech.derbent.screens.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** CScreen - Domain entity representing screen views for entities. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project association.
 * This entity allows creating custom view definitions for various project entities. */
@Entity
@Table (name = "cscreen")
@AttributeOverride (name = "id", column = @Column (name = "screen_id"))
public class CScreen extends CEntityOfProject<CScreen> {

	public static String getIconColorCode() {
		return "#6f42c1"; // Purple color for screen entities
	}

	public static String getIconFilename() { return "vaadin:viewport"; }

	@Column (name = "entity_type", nullable = false, length = 100)
	@Size (max = 100, message = "Entity type cannot exceed 100 characters")
	@MetaData (
			displayName = "Entity Type", required = true, readOnly = false, description = "Type of entity this screen is designed for",
			hidden = false, order = 2, maxLength = 100, dataProviderBean = "CViewsService", dataProviderMethod = "getAvailableBaseTypes"
	)
	private String entityType;
	@Column (name = "screen_title", nullable = true, length = 255)
	@Size (max = 255, message = "Screen title cannot exceed 255 characters")
	@MetaData (
			displayName = "Screen Title", required = false, readOnly = false, description = "Title to display for this screen view", hidden = false,
			order = 3, maxLength = 255
	)
	private String screenTitle;
	@Column (name = "header_text", nullable = true, length = 500)
	@Size (max = 500, message = "Header text cannot exceed 500 characters")
	@MetaData (
			displayName = "Header Text", required = false, readOnly = false, description = "Header text to display at the top of the screen",
			hidden = false, order = 4, maxLength = 500
	)
	private String headerText;
	@OneToMany (mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@OrderBy ("lineOrder ASC")
	private List<CScreenLines> screenLines = new ArrayList<>();
	@Column (name = "is_active", nullable = false)
	@MetaData (
			displayName = "Active", required = false, readOnly = false, description = "Whether this screen definition is active", hidden = false,
			order = 20, defaultValue = "true"
	)
	private Boolean isActive = true;

	/** Default constructor for JPA. */
	public CScreen() {
		super();
	}

	public CScreen(final String name, final CProject project) {
		super(CScreen.class, name, project);
	}
	// Getters and Setters

	/** Helper method to add a screen line */
	public void addScreenLine(final CScreenLines screenLine) {
		Check.notNull(screenLine, "screenLine must not be null");
		// check screen name dublicate
		if (screenLine.getSectionName() != null) {
			for (final CScreenLines line : screenLines) {
				if (line.getSectionName() == null) {
					continue;
				}
				if (line.getSectionName().equals(screenLine.getSectionName())) {
					throw new IllegalArgumentException(
							"A screen line with the name '" + screenLine.getSectionName() + "' already exists in this screen.");
				}
			}
		}
		if (screenLine.getLineOrder() == 0) {
			// default line order is the next available number
			screenLine.setLineOrder(screenLines.size() + 1);
		}
		screenLines.add(screenLine);
		screenLine.setScreen(this);
	}

	public void debug_printScreenInformation() {
		System.out.println(this.toString());
		final List<EntityFieldInfo> fields = CEntityFieldService.getEntityFields(this.entityType);
		if (this.screenLines != null) {
			for (final CScreenLines line : this.screenLines) {
				// line.printLine();
				fields.removeIf(f -> f.getFieldName().equals(line.getEntityProperty()));
			}
		} else {
			System.out.println("No screen lines available.");
		}
		if (fields.isEmpty()) {
			System.out.println("All entity fields are represented on the screen.");
		} else {
			System.out.println("Not on screen lines:");
			for (final EntityFieldInfo field : fields) {
				System.out.printf("scr.addScreenLine(CScreenLinesService.createLineFromDefaults(clazz, \"%s\"));%n", field.getFieldName());
			}
		}
	}

	public String getEntityType() { return entityType; }

	public String getHeaderText() { return headerText; }

	public Boolean getIsActive() { return isActive; }

	public List<CScreenLines> getScreenLines() { return screenLines; }

	public String getScreenTitle() { return screenTitle; }

	/** Helper method to remove a screen line */
	public void removeScreenLine(final CScreenLines screenLine) {
		screenLines.remove(screenLine);
		screenLine.setScreen(null);
	}

	public void setEntityType(final String entityType) { this.entityType = entityType; }

	public void setHeaderText(final String headerText) { this.headerText = headerText; }

	public void setIsActive(final Boolean isActive) { this.isActive = isActive; }

	public void setScreenLines(final List<CScreenLines> screenLines) { this.screenLines = screenLines; }

	public void setScreenTitle(final String screenTitle) { this.screenTitle = screenTitle; }

	@Override
	public String toString() {
		return String.format("CScreen{id=%d, name='%s', entityType='%s', screenTitle='%s'}", getId(), getName(), entityType, screenTitle);
	}
}
