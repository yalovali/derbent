package tech.derbent.api.entity.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;

@MappedSuperclass
public abstract class CEntityNamed<EntityClass> extends CEntityDB<EntityClass> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityNamed.class);
	// Audit fields
	@Column (name = "created_date", nullable = true)
	@AMetaData (
			displayName = "Created Date", required = false, readOnly = true, description = "Date and time when the activity was created",
			hidden = false
	)
	private LocalDateTime createdDate;
	@Column (nullable = true, length = 2000)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION, message = ValidationMessages.DESCRIPTION_MAX_LENGTH)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, defaultValue = "", description = "Detailed description of the project",
			hidden = false, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String description;
	@Column (name = "last_modified_date", nullable = true)
	@AMetaData (
			displayName = "Last Modified", required = false, readOnly = true, description = "Date and time when the activity was last modified",
			hidden = false
	)
	private LocalDateTime lastModifiedDate;
	@Column (nullable = true, length = CEntityConstants.MAX_LENGTH_NAME, unique = false)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME, message = ValidationMessages.NAME_MAX_LENGTH)
	@AMetaData (
			displayName = "Name", required = false, readOnly = false, defaultValue = "", description = "Name", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME, setBackgroundFromColor = true
	)
	private String name;

	/** Default constructor for JPA. */
	protected CEntityNamed() {
		super();
	}

	public CEntityNamed(final Class<EntityClass> clazz, final String name) {
		super(clazz);
		this.name = name != null ? name.trim() : null;
		initializeDefaults();
	}

	/** Copies entity fields to target using copyField pattern. Override in subclasses to add more fields. Always call super.copyEntityTo() first!
	 * @param target  The target entity
	 * @param options Clone options */
	@SuppressWarnings ("unchecked")
	@Override
	protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings ("rawtypes") CAbstractService serviceTarget,
			final CCloneOptions options) {
		// Always call parent first
		super.copyEntityTo(target, serviceTarget, options);
		// Copy named fields if target supports them
		if (target instanceof final CEntityNamed targetEntity) {
			copyField(this::getName, targetEntity::setName);
			copyField(this::getDescription, targetEntity::setDescription);
			// Copy dates based on options
			if (!options.isResetDates()) {
				copyField(this::getCreatedDate, (d) -> targetEntity.createdDate = d);
				copyField(this::getLastModifiedDate, (d) -> targetEntity.lastModifiedDate = d);
			}
		}
	}

	@Override
	public boolean equals(final Object obj) {
		// Use the superclass (CEntityDB) equals method which properly handles ID-based
		// equality and proxy classes. This is the standard approach for JPA entities.
		return super.equals(obj);
	}

	public LocalDateTime getCreatedDate() { return createdDate; }

	/** Returns the default ordering field for queries. Named entities are ordered by name by default.
	 * @return "name" as the default ordering field */
	@Override
	public String getDefaultOrderBy() { return "name"; }

	public String getDescription() { return description; }

	public String getDescriptionShort() {
		if (description == null || description.isBlank()) {
			return "No description";
		}
		if (description.length() <= 75) {
			return description;
		}
		return description.substring(0, 75) + "...";
	}

	public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }

	public String getName() { return name; }

	private final void initializeDefaults() {
		description = "";
		// Don't override name here - it's set by constructor parameter
		// name = "";  // REMOVED - constructor sets this
		createdDate = LocalDateTime.now();
		lastModifiedDate = LocalDateTime.now();
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends the base class to search in 'name'
	 * and 'description' fields in addition to inherited fields from CEntityDB. If no field names are specified, searches only in "name" field.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: "id",
	 *                    "active", "name", "description"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, @Nullable Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		// Ensure fieldNames is mutable for the entire traversal chain
		Collection<String> mutableFieldNames = fieldNames;
		if (fieldNames == null || fieldNames.isEmpty()) {
			// Default to searching in "name" field when no fields specified
			mutableFieldNames = new ArrayList<>();
			mutableFieldNames.add("name");
		} else if (!(fieldNames instanceof ArrayList)) {
			mutableFieldNames = new ArrayList<>(fieldNames);
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check ID field if requested
		Check.notNull(fieldNames, "fieldNames cannot be null here");
		final String name1 = getName();
		if (fieldNames.remove("name") && name1 != null && name1.toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		final String description1 = getDescription();
		if (fieldNames.remove("description") && description1 != null && description1.toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		return false;
	}

	public void setCreatedDate(final LocalDateTime createdDate) { this.createdDate = createdDate; }

	public void setDescription(final String description) {
		this.description = description;
		updateLastModified();
	}

	public void setLastModifiedDate(final LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

	public void setName(final String name) {
		// Name can be null or empty in base class - concrete classes enforce validation
		this.name = name;
		updateLastModified();
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
