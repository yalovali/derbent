package tech.derbent.api.domains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.app.projects.domain.CProject;

/** CStatus - Abstract base entity for all status types in the system. Layer: Domain (MVC) This class provides common functionality for status
 * entities including name and description. All status types (like CActivityStatus) should inherit from this class. */
@MappedSuperclass
public abstract class CStatus<EntityType> extends CTypeEntity<EntityType> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CStatus.class);
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
	}

	/** Constructor with name (required field).
	 * @param name the name of the status */
	protected CStatus(final Class<EntityType> clazz, final String name, final CProject project) {
		super(clazz, name, project);
	}

	public Boolean getStatusTypeCancelled() { return statusTypeCancelled; }

	public Boolean getStatusTypeClosed() { return statusTypeClosed; }

	public Boolean getStatusTypeCompleted() { return statusTypeCompleted; }

	public Boolean getStatusTypeInprogress() { return statusTypeInprogress; }

	public Boolean getStatusTypePause() { return statusTypePause; }

	public void setStatusTypeCancelled(Boolean statusTypeCancelled) { this.statusTypeCancelled = statusTypeCancelled; }

	public void setStatusTypeClosed(Boolean statusTypeClosed) { this.statusTypeClosed = statusTypeClosed; }

	public void setStatusTypeCompleted(Boolean statusTypeCompleted) { this.statusTypeCompleted = statusTypeCompleted; }

	public void setStatusTypeInprogress(Boolean statusTypeInprogress) { this.statusTypeInprogress = statusTypeInprogress; }

	public void setStatusTypePause(Boolean statusTypePause) { this.statusTypePause = statusTypePause; }
}
