package tech.derbent.bab.device.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;

/**
 * CBabDevice - Concrete IoT gateway device entity.
 * Represents the single unique device instance per database/company.
 * Contains configurations, nodes, and runtime status.
 */
@Entity
@Table(name = "cbab_device", uniqueConstraints = {
	@UniqueConstraint(columnNames = { "company_id" }) // Only one device per company
})
public class CBabDevice extends CBabDeviceBase {

	public static final String DEFAULT_COLOR = "#6B5FA7";
	public static final String DEFAULT_ICON = "vaadin:server";
	public static final String ENTITY_TITLE_PLURAL = "Devices";
	public static final String ENTITY_TITLE_SINGULAR = "Device";
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabDevice.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Device Management";

	/** Default constructor for JPA. */
	public CBabDevice() {
		super();
	}

	public CBabDevice(final String name, final CCompany company) {
		super((Class) CBabDevice.class, name, company);
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		// Set default device status on creation
		if (getDeviceStatus() == null) {
			setDeviceStatus("Offline");
		}
	}
}
