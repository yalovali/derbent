package tech.derbent.api.entityOfCompany.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.app.companies.domain.CCompany;

/** CStatus - Abstract base entity for all status types in the system. Layer: Domain (MVC) This class provides common functionality for status
 * entities including name and description. All status types (like CProjectItemStatus) should inherit from this class. */
@MappedSuperclass
public abstract class CStatus<EntityClass> extends CEntityOfCompany<EntityClass> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CStatus.class);
	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this type entity cannot be deleted by users (system configuration)", hidden = false, order = 82
	)
	private boolean attributeNonDeletable = true;
	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7, message = ValidationMessages.COLOR_MAX_LENGTH)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = "#4A90E2", colorField = true,
			description = "Hex color code for type visualization (e.g., #4A90E2)", hidden = false, order = 3, maxLength = 7
	)
	private String color = "#4A90E2";
	@Column (name = "sort_order", nullable = false)
	@NotNull (message = ValidationMessages.SORT_ORDER_REQUIRED)
	@Min (value = 1, message = ValidationMessages.SORT_ORDER_MIN)
	@Max (value = 9999, message = ValidationMessages.SORT_ORDER_MAX)
	@AMetaData (
			displayName = "Sort Order", required = true, readOnly = false, defaultValue = "100", description = "Display order for type sorting",
			hidden = false, order = 4
	)
	private Integer sortOrder = 100;
	@Column (name = "statusTypeCancelled", nullable = false)
	@AMetaData (
			displayName = "Is Cancelled Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents a cancelled state", hidden = true, order = 2
	)
	private Boolean statusTypeCancelled = Boolean.FALSE;
	@Column (name = "statusTypeClosed", nullable = false)
	@AMetaData (
			displayName = "Is Closed Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents a closed state", hidden = true, order = 4
	)
	private Boolean statusTypeClosed = Boolean.FALSE;
	@Column (name = "statusTypeCompleted", nullable = false)
	@AMetaData (
			displayName = "Is Completed Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents a completed state", hidden = true, order = 5
	)
	private Boolean statusTypeCompleted = Boolean.FALSE;
	@Column (name = "statusTypeInprogress", nullable = false)
	@AMetaData (
			displayName = "Is In Progress Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents an in-progress state", hidden = true, order = 6
	)
	private Boolean statusTypeInprogress = Boolean.FALSE;
	@Column (name = "statusTypePause", nullable = false)
	@AMetaData (
			displayName = "Is Paused Status", required = true, readOnly = false, defaultValue = "false",
			description = "Indicates if this status represents a paused state", hidden = true, order = 7
	)
	private Boolean statusTypePause = Boolean.FALSE;

	/** Default constructor for JPA. */
	protected CStatus() {
		super();
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
	}

	/** Constructor with name (required field).
	 * @param name the name of the status */
	protected CStatus(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name, company);
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
	}

	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	public String getColor() { return color; }

	public Integer getSortOrder() { return sortOrder; }

	public Boolean getStatusTypeCancelled() { return statusTypeCancelled; }

	public Boolean getStatusTypeClosed() { return statusTypeClosed; }

	public Boolean getStatusTypeCompleted() { return statusTypeCompleted; }

	public Boolean getStatusTypeInprogress() { return statusTypeInprogress; }

	public Boolean getStatusTypePause() { return statusTypePause; }

	public void setAttributeNonDeletable(final boolean attributeNonDeletable) { this.attributeNonDeletable = attributeNonDeletable; }

	public void setColor(final String color) { this.color = color; }

	public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }

	public void setStatusTypeCancelled(final Boolean statusTypeCancelled) { this.statusTypeCancelled = statusTypeCancelled; }

	public void setStatusTypeClosed(final Boolean statusTypeClosed) { this.statusTypeClosed = statusTypeClosed; }

	public void setStatusTypeCompleted(final Boolean statusTypeCompleted) { this.statusTypeCompleted = statusTypeCompleted; }

	public void setStatusTypeInprogress(final Boolean statusTypeInprogress) { this.statusTypeInprogress = statusTypeInprogress; }

	public void setStatusTypePause(final Boolean statusTypePause) { this.statusTypePause = statusTypePause; }
}
