package tech.derbent.api.entity.domain;

import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.utils.Check;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.validation.ValidationMessages;

@MappedSuperclass
public abstract class CEntityNamed<EntityClass> extends CEntityDB<EntityClass> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityNamed.class);
	// Audit fields
	@Column (name = "created_date", nullable = true)
	@AMetaData (
			displayName = "Created Date", required = false, readOnly = true, description = "Date and time when the activity was created",
			hidden = false, order = 80
	)
	private LocalDateTime createdDate;
	@Column (nullable = true, length = 2000)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION, message = ValidationMessages.DESCRIPTION_MAX_LENGTH)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, defaultValue = "", description = "Detailed description of the project",
			hidden = false, order = 1, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String description;
	@Column (name = "last_modified_date", nullable = true)
	@AMetaData (
			displayName = "Last Modified", required = false, readOnly = true, description = "Date and time when the activity was last modified",
			hidden = false, order = 81
	)
	private LocalDateTime lastModifiedDate;
	@Column (nullable = false, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@NotBlank (message = ValidationMessages.NAME_REQUIRED)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME, message = ValidationMessages.NAME_MAX_LENGTH)
	@AMetaData (
			displayName = "Name", required = true, readOnly = false, defaultValue = "", description = "Name", hidden = false, order = 0,
			maxLength = CEntityConstants.MAX_LENGTH_NAME, setBackgroundFromColor = true
	)
	private String name;

	/** Default constructor for JPA. */
	protected CEntityNamed() {
		super();
		name = null;
	}

	public CEntityNamed(final Class<EntityClass> clazz, final String name) {
		super(clazz);
		Check.notBlank(name, "Name cannot be null or empty for " + getClass().getSimpleName());
		this.name = name.trim();
	}

	@Override
	public boolean equals(final Object obj) {
		// Use the superclass (CEntityDB) equals method which properly handles ID-based
		// equality and proxy classes. This is the standard approach for JPA entities.
		return super.equals(obj);
	}

	public LocalDateTime getCreatedDate() { return createdDate; }

	public String getDescription() { return description; }

	public String getDescriptionShort() {
		if ((description == null) || description.isBlank()) {
			return "No description";
		}
		if (description.length() <= 75) {
			return description;
		}
		return description.substring(0, 75) + "...";
	}

	public Map<String, EntityFieldInfo> getFieldsInfo() { // TODO Auto-generated method
															// stub
		return null;
	}

	public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }

	public String getName() { return name; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (createdDate == null) {
			createdDate = LocalDateTime.now();
		}
		lastModifiedDate = LocalDateTime.now();
	}

	public void setCreatedDate(final LocalDateTime createdDate) { this.createdDate = createdDate; }

	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public void setLastModifiedDate(final LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

	public void setName(final String name) {
		// can be empty !!!
		// Check.notBlank(name, "Name cannot be null or empty for " + getClass().getSimpleName());
		Check.notNull(name, "Name cannot be null for " + getClass().getSimpleName());
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	/** Update the last modified date to now. */
	protected void updateLastModified() {
		lastModifiedDate = LocalDateTime.now();
	}
}
