package tech.derbent.bab.device.domain;

import java.util.Arrays;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.utils.Check;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.base.users.domain.CUser;

/**
 * CBabItem - Base class for all BAB (IoT Gateway) entities.
 * Similar to CProjectItem but scoped to company and device instead of project.
 * Provides common fields and behavior for device nodes and configurations.
 */
@MappedSuperclass
public abstract class CBabItem<EntityClass> extends CEntityNamed<EntityClass> {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "company_id", nullable = false)
	@AMetaData(displayName = "Company", required = true, readOnly = true, description = "Company owning this entity", hidden = false)
	private CCompany company;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "device_id", nullable = true)
	@AMetaData(displayName = "Device", required = false, readOnly = false, description = "Device this entity belongs to", hidden = false)
	private CBabDevice device;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "created_by_id", nullable = true)
	@AMetaData(
		displayName = "Created By", required = false, readOnly = true, description = "User who created this entity", hidden = false,
		dataProviderBean = "CUserService"
	)
	private CUser createdBy;

	/** Default constructor for JPA. */
	protected CBabItem() {
		super();
	}

	public CBabItem(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name);
		Check.notNull(company, "Company cannot be null for " + getClass().getSimpleName());
		this.company = company;
	}

	public CBabItem(final Class<EntityClass> clazz, final String name, final CBabDevice device) {
		super(clazz, name);
		Check.notNull(device, "Device cannot be null for " + getClass().getSimpleName());
		this.device = device;
		this.company = device.getCompany();
	}

	/** Gets the company this entity belongs to.
	 * @return the company */
	public CCompany getCompany() {
		return company;
	}

	/** Gets the device this entity belongs to.
	 * @return the device, or null if not device-specific */
	public CBabDevice getDevice() {
		return device;
	}

	/** Gets the user who created this entity.
	 * @return the creator user */
	public CUser getCreatedBy() {
		return createdBy;
	}

	/** Sets the company for this entity.
	 * @param company the company to set */
	public void setCompany(final CCompany company) {
		this.company = company;
		updateLastModified();
	}

	/** Sets the device for this entity.
	 * @param device the device to set */
	public void setDevice(final CBabDevice device) {
		this.device = device;
		if (device != null) {
			this.company = device.getCompany();
		}
		updateLastModified();
	}

	/** Sets the creator user for this entity.
	 * @param createdBy the creator user to set */
	public void setCreatedBy(final CUser createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public boolean matchesFilter(final String searchValue, @Nullable Collection<String> fieldNames) {
		if (searchValue == null || searchValue.isBlank()) {
			return true;
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		// Search in company name
		if (company != null && company.getName() != null) {
			if (company.getName().toLowerCase().contains(searchValue.toLowerCase())) {
				return true;
			}
		}
		// Search in device name
		if (device != null && device.getName() != null) {
			if (device.getName().toLowerCase().contains(searchValue.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}
}
