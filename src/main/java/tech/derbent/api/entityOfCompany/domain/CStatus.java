package tech.derbent.api.entityOfCompany.domain;

import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.companies.domain.CCompany;

/** CStatus - Abstract base entity for all status types in the system. Layer: Domain (MVC) This class provides common functionality for status
 * entities including name and description. All status types (like CProjectItemStatus) should inherit from this class. */
@MappedSuperclass
public abstract class CStatus<EntityClass> extends CEntityOfCompany<EntityClass> implements IHasIcon {

	public static final String DEFAULT_COLOR = "#4966B0"; // OpenWindows Selection Blue - project item statuses
	public static final String DEFAULT_ICON = "vaadin:flag";
	protected static final Logger LOGGER = LoggerFactory.getLogger(CStatus.class);
	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this type entity cannot be deleted by users (system configuration)", hidden = false
	)
	private boolean attributeNonDeletable = true;
	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7, message = ValidationMessages.COLOR_MAX_LENGTH)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = "#4A90E2", colorField = true,
			description = "Hex color code for type visualization (e.g., #4A90E2)", hidden = false, maxLength = 7
	)
	private String color = "#4A90E2";
	@Column (nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Icon", required = true, readOnly = false, defaultValue = "vaadin:file", description = "Icon for the page menu item",
			hidden = false, maxLength = 100, useIcon = true
	)
	private String iconString;
	@Column (name = "sort_order", nullable = false)
	@NotNull (message = ValidationMessages.SORT_ORDER_REQUIRED)
	@Min (value = 1, message = ValidationMessages.SORT_ORDER_MIN)
	@Max (value = 9999, message = ValidationMessages.SORT_ORDER_MAX)
	@AMetaData (
			displayName = "Sort Order", required = true, readOnly = false, defaultValue = "100", description = "Display order for type sorting",
			hidden = false
	)
	private Integer sortOrder = 100;
	@Column (name = "statusTypeCancelled", nullable = false)
	@AMetaData (
			displayName = "Is Cancelled Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents a cancelled state", hidden = true
	)
	private Boolean statusTypeCancelled = Boolean.FALSE;
	@Column (name = "statusTypeClosed", nullable = false)
	@AMetaData (
			displayName = "Is Closed Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents a closed state", hidden = true
	)
	private Boolean statusTypeClosed = Boolean.FALSE;
	@Column (name = "statusTypeCompleted", nullable = false)
	@AMetaData (
			displayName = "Is Completed Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents a completed state", hidden = true
	)
	private Boolean statusTypeCompleted = Boolean.FALSE;
	@Column (name = "statusTypeInprogress", nullable = false)
	@AMetaData (
			displayName = "Is In Progress Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents an in-progress state", hidden = true
	)
	private Boolean statusTypeInprogress = Boolean.FALSE;
	@Column (name = "statusTypePause", nullable = false)
	@AMetaData (
			displayName = "Is Paused Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents a paused state", hidden = true
	)
	private Boolean statusTypePause = Boolean.FALSE;

	/** Default constructor for JPA. */
	protected CStatus() {
		super();
		initializeDefaults();
	}

	/** Constructor with name (required field).
	 * @param name the name of the status */
	protected CStatus(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name, company);
		initializeDefaults();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		setColor(DEFAULT_COLOR);
		setIconString(DEFAULT_ICON);
		sortOrder = 100;
		attributeNonDeletable = false;
		statusTypeCancelled = Boolean.FALSE;
		statusTypeClosed = Boolean.FALSE;
		statusTypeCompleted = Boolean.FALSE;
		statusTypeInprogress = Boolean.FALSE;
		statusTypePause = Boolean.FALSE;
	}

	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	@Override
	public String getColor() { return color; }

	@Override
	public String getIconString() { return iconString != null ? iconString : DEFAULT_ICON; }

	public Integer getSortOrder() { return sortOrder; }

	public Boolean getStatusTypeCancelled() { return statusTypeCancelled; }

	public Boolean getStatusTypeClosed() { return statusTypeClosed; }

	public Boolean getStatusTypeCompleted() { return statusTypeCompleted; }

	public Boolean getStatusTypeInprogress() { return statusTypeInprogress; }

	public Boolean getStatusTypePause() { return statusTypePause; }

	@Override
	public boolean matchesFilter(final String searchValue, final @Nullable Collection<String> fieldNames) {
		if ((searchValue == null) || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check boolean fields for status types
		if (fieldNames.remove("attributeNonDeletable") && String.valueOf(getAttributeNonDeletable()).toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("statusTypeCancelled") && getStatusTypeCancelled().toString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("statusTypeClosed") && getStatusTypeClosed().toString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("statusTypeCompleted") && getStatusTypeCompleted().toString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("statusTypeInprogress") && getStatusTypeInprogress().toString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("statusTypePause") && getStatusTypePause().toString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		// Check string fields
		if (fieldNames.remove("color") && (getColor() != null) && getColor().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		if (fieldNames.remove("iconString") && (getIconString() != null) && getIconString().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		return false;
	}

	public void setAttributeNonDeletable(final boolean attributeNonDeletable) { this.attributeNonDeletable = attributeNonDeletable; }

	@Override
	public void setColor(final String color) { this.color = color; }

	public void setIconString(String icon) { iconString = icon; }

	public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }

	public void setStatusTypeCancelled(final Boolean statusTypeCancelled) { this.statusTypeCancelled = statusTypeCancelled; }

	public void setStatusTypeClosed(final Boolean statusTypeClosed) { this.statusTypeClosed = statusTypeClosed; }

	public void setStatusTypeCompleted(final Boolean statusTypeCompleted) { this.statusTypeCompleted = statusTypeCompleted; }

	public void setStatusTypeInprogress(final Boolean statusTypeInprogress) { this.statusTypeInprogress = statusTypeInprogress; }

	public void setStatusTypePause(final Boolean statusTypePause) { this.statusTypePause = statusTypePause; }
}
