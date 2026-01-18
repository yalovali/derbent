package tech.derbent.api.entityOfCompany.domain;

import java.util.Arrays;
import java.util.Collection;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.CSpringAuxillaries;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.CCloneOptions;

@MappedSuperclass
public abstract class CEntityOfCompany<EntityClass> extends CEntityNamed<EntityClass> {

	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "company_id", nullable = true)
	@OnDelete (action = OnDeleteAction.CASCADE)
	@AMetaData (
			displayName = "Company", required = false, readOnly = false, description = "User's company", hidden = false,
			setBackgroundFromColor = true, useIcon = true
	)
	private CCompany company;

	/** Default constructor for JPA. */
	protected CEntityOfCompany() {
		super();
		// Initialize with default values for JPA
		company = null;
	}

	public CEntityOfCompany(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name);
		this.company = company;
	}

	/** Copies entity fields to target entity. Override to add CUser-specific fields.
	 * @param target  The target entity
	 * @param options Clone options to control copying behavior */
	@Override
	protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings ("rawtypes") CAbstractService serviceTarget,
			final CCloneOptions options) {
		// Always call parent first
		super.copyEntityTo(target, serviceTarget, options);
		// Copy CUser-specific fields if target is also a CUser
		if (target instanceof final CEntityOfCompany targetEntity) {
			copyField(this::getCompany, targetEntity::setCompany);
		}
	}

	/** Creates a clone of this entity with the specified options. This implementation clones company-specific fields. Subclasses must override to add
	 * their specific fields.
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the entity with cloned data
	 * @throws CloneNotSupportedException if cloning fails */
	@Override
	public EntityClass createClone(final CCloneOptions options) throws Exception {
		// Get parent's clone (CEntityNamed -> CEntityDB)
		final EntityClass clone = super.createClone(options);
		if (clone instanceof CEntityOfCompany) {
			final CEntityOfCompany<?> cloneEntity = (CEntityOfCompany<?>) clone;
			// Always clone company (required field)
			cloneEntity.setCompany(this.getCompany());
		}
		return clone;
	}

	public CCompany getCompany() { return company; }

	@Override
	public void initializeAllFields() {
		if (company != null && !CSpringAuxillaries.isLoaded(company)) {
			CSpringAuxillaries.initializeLazily(company);
		}
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CEntityNamed to also search in
	 * company field. For the company field, only the company name is searched.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: "id",
	 *                    "active", "name", "description", "company"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, @Nullable Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		// Ensure fieldNames is mutable for
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		if (fieldNames.remove("company") && getCompany() != null && getCompany().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		return false;
	}

	public void setCompany(final CCompany company) { this.company = company; }
}
