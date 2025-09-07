package tech.derbent.abstracts.domains;

import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

@MappedSuperclass
public abstract class CEntityNamed<EntityClass> extends CEntityDB<EntityClass> implements IDisplayEntity {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityNamed.class);
	@Column (name = "name", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Name", required = true, readOnly = false, defaultValue = "", description = "Name", hidden = false, order = 0,
			maxLength = CEntityConstants.MAX_LENGTH_NAME, setBackgroundFromColor = true
	)
	private String name;
	@Column (name = "description", nullable = true, length = 2000)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, defaultValue = "", description = "Detailed description of the project",
			hidden = false, order = 1, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String description;
	// Audit fields
	@Column (name = "created_date", nullable = true)
	@AMetaData (
			displayName = "Created Date", required = false, readOnly = true, description = "Date and time when the activity was created",
			hidden = false, order = 80
	)
	private LocalDateTime createdDate;
	@Column (name = "last_modified_date", nullable = true)
	@AMetaData (
			displayName = "Last Modified", required = false, readOnly = true, description = "Date and time when the activity was last modified",
			hidden = false, order = 81
	)
	private LocalDateTime lastModifiedDate;

	/** Default constructor for JPA. */
	protected CEntityNamed() {
		super();
		this.name = null;
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
	public Class<?> getViewClass() {
		Check.fail("CEntityDB.getViewClass() called - returning NONE");
		return null;
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (this.createdDate == null) {
			this.createdDate = LocalDateTime.now();
		}
		this.lastModifiedDate = LocalDateTime.now();
	}

	public void setCreatedDate(final LocalDateTime createdDate) { this.createdDate = createdDate; }

	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public void setLastModifiedDate(final LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

	public void setName(final String name) {
		Check.notBlank(name, "Name cannot be null or empty for " + getClass().getSimpleName());
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	/** Update the last modified date to now. */
	protected void updateLastModified() {
		this.lastModifiedDate = LocalDateTime.now();
	}
}
