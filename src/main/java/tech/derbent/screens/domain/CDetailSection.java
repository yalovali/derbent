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
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** CScreen - Domain entity representing screen views for entities. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project association.
 * This entity allows creating custom view definitions for various project entities. */
@Entity
@Table (name = "cdetailsection")
@AttributeOverride (name = "id", column = @Column (name = "detailsection_id"))
public class CDetailSection extends CEntityOfProject<CDetailSection> {

	public static final String DEFAULT_COLOR = "#be6f00";
	public static final String DEFAULT_ICON = "vaadin:comment";
	public static final String VIEW_NAME = "Detail Section View";
	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "false",
			description = "Whether this detail section cannot be deleted by users", hidden = false, order = 5
	)
	private boolean attributeNonDeletable = false;
	@OneToMany (mappedBy = "detailSection", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@OrderBy ("lineOrder ASC")
	private List<CDetailLines> detailLines = new ArrayList<>();
	@Column (name = "entity_type", nullable = false, length = 100)
	@Size (max = 100, message = "Entity type cannot exceed 100 characters")
	@AMetaData (
			displayName = "Entity Type", required = true, readOnly = false, description = "Type of entity this screen is designed for",
			hidden = false, order = 2, maxLength = 100, dataProviderBean = "CViewsService", dataProviderMethod = "getAvailableBaseTypes"
	)
	private String entityType;
	@Column (name = "header_text", nullable = true, length = 500)
	@Size (max = 500, message = "Header text cannot exceed 500 characters")
	@AMetaData (
			displayName = "Header Text", required = false, readOnly = false, description = "Header text to display at the top of the screen",
			hidden = false, order = 4, maxLength = 500
	)
	private String headerText;
	@Column (name = "screen_title", nullable = true, length = 255)
	@Size (max = 255, message = "Screen title cannot exceed 255 characters")
	@AMetaData (
			displayName = "Screen Title", required = false, readOnly = false, description = "Title to display for this screen view", hidden = false,
			order = 3, maxLength = 255
	)
	private String screenTitle;

	/** Default constructor for JPA. */
	public CDetailSection() {
		super();
	}

	public CDetailSection(final String name, final CProject project) {
		super(CDetailSection.class, name, project);
		attributeNonDeletable = false;
	}
	// Getters and Setters

	/** Helper method to add a screen line */
	public void addScreenLine(final CDetailLines screenLine) {
		Check.notNull(screenLine, "screenLine must not be null");
		// check screen name dublicate
		if (screenLine.getSectionName() != null) {
			for (final CDetailLines line : detailLines) {
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
			screenLine.setLineOrder(detailLines.size() + 1);
		}
		detailLines.add(screenLine);
		screenLine.setDetailSection(this);
	}

	public void debug_printScreenInformation() {
		final String title = toString();
		final List<EntityFieldInfo> fields = CEntityFieldService.getEntityFields(entityType);
		if (detailLines != null) {
			for (final CDetailLines line : detailLines) {
				// line.printLine();
				fields.removeIf(f -> f.getFieldName().equals(line.getEntityProperty()));
			}
		} else {
			System.out.printf("No screen lines available for screen type %s.%n", title);
		}
		if (fields.isEmpty()) {
			System.out.printf("All entity fields are represented on the screen type %s.%n", title);
		} else {
			System.out.printf("Not on screen lines for screen type %s.%n", title);
			for (final EntityFieldInfo field : fields) {
				System.out.printf("scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, \"%s\"));%n", field.getFieldName());
			}
		}
	}

	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	public String getEntityType() { return entityType; }

	public String getHeaderText() { return headerText; }

	public List<CDetailLines> getScreenLines() { return detailLines; }

	public String getScreenTitle() { return screenTitle; }

	/** Helper method to remove a screen line */
	public void removeScreenLine(final CDetailLines screenLine) {
		detailLines.remove(screenLine);
		screenLine.setDetailSection(null);
	}

	public void setAttributeNonDeletable(boolean attributeNonDeletable) { this.attributeNonDeletable = attributeNonDeletable; }

	public void setEntityType(final String entityType) { this.entityType = entityType; }

	public void setHeaderText(final String headerText) { this.headerText = headerText; }

	public void setScreenLines(final List<CDetailLines> screenLines) { detailLines = screenLines; }

	public void setScreenTitle(final String screenTitle) { this.screenTitle = screenTitle; }

	@Override
	public String toString() {
		return String.format("CScreen{id=%d, name='%s', entityType='%s', screenTitle='%s'}", getId(), getName(), entityType, screenTitle);
	}
}
