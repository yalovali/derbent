package tech.derbent.abstracts.domains;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;

@MappedSuperclass
public abstract class CEntityNamed<EntityClass> extends CEntityDB<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityNamed.class);

	@Column (
		name = "name", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME,
		unique = false
	)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@MetaData (
		displayName = "Name", required = true, readOnly = false, defaultValue = "",
		description = "Name", hidden = false, order = 0,
		maxLength = CEntityConstants.MAX_LENGTH_NAME, setBackgroundFromColor = true
	)
	private String name;

	@Column (name = "description", nullable = true, length = 2000)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@MetaData (
		displayName = "Description", required = false, readOnly = false,
		defaultValue = "", description = "Detailed description of the project",
		hidden = false, order = 1, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String description;

	// Audit fields
	@Column (name = "created_date", nullable = true)
	@MetaData (
		displayName = "Created Date", required = false, readOnly = true,
		description = "Date and time when the activity was created", hidden = false,
		order = 80
	)
	private LocalDateTime createdDate;

	@Column (name = "last_modified_date", nullable = true)
	@MetaData (
		displayName = "Last Modified", required = false, readOnly = true,
		description = "Date and time when the activity was last modified", hidden = false,
		order = 81
	)
	private LocalDateTime lastModifiedDate;

	/**
	 * Default constructor for JPA.
	 */
	protected CEntityNamed() {
		super();
		// Initialize with default values for JPA
		this.name = "";
		this.description = null;
	}

	public CEntityNamed(final Class<EntityClass> clazz, final String name) {
		super(clazz);

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.error("Constructor called with null or empty name for {}",
				getClass().getSimpleName());
			throw new IllegalArgumentException(
				"Name cannot be null or empty for " + getClass().getSimpleName());
		}
		this.name = name.trim();
		this.description = null; // Default description is null
	}

	public LocalDateTime getCreatedDate() { return createdDate; }

	public String getDescription() { return description; }

	public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }

	public String getName() { return name; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();

		if (this.createdDate == null) {
			this.createdDate = LocalDateTime.now();
		}
		this.lastModifiedDate = LocalDateTime.now();
	}

	public void setCreatedDate(final LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public void setLastModifiedDate(final LocalDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public void setName(final String name) {

		if ((name == null) || name.trim().isEmpty()) {
			LOGGER.warn("setname called with null or empty name for {}",
				getClass().getSimpleName());
		}
		this.name = name;
	}

	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}

	/**
	 * Update the last modified date to now.
	 */
	protected void updateLastModified() {
		this.lastModifiedDate = LocalDateTime.now();
	}
}
