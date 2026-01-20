package tech.derbent.bab.device.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.base.session.service.ISessionService;

import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;

/** Service class for CBabDevice entity. Provides business logic for BAB IoT gateway device management. Following Derbent pattern: Service with
 * IEntityRegistrable and IEntityWithView. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabDeviceService extends CAbstractService<CBabDevice> implements IEntityRegistrable, IEntityWithView {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabDeviceService.class);

	public CBabDeviceService(final IBabDeviceRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void validateEntity(final CBabDevice entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
		
		// 2. Length Checks
		if (entity.getName() != null && entity.getName().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, 255));
		}
		if (entity.getSerialNumber() != null && entity.getSerialNumber().length() > 255) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Serial Number cannot exceed %d characters", 255));
		}
		if (entity.getFirmwareVersion() != null && entity.getFirmwareVersion().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Firmware Version cannot exceed %d characters", 100));
		}
		if (entity.getHardwareRevision() != null && entity.getHardwareRevision().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Hardware Revision cannot exceed %d characters", 100));
		}
		if (entity.getDeviceStatus() != null && entity.getDeviceStatus().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Device Status cannot exceed %d characters", 50));
		}
		if (entity.getIpAddress() != null && entity.getIpAddress().length() > 45) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("IP Address cannot exceed %d characters", 45));
		}
		if (entity.getMacAddress() != null && entity.getMacAddress().length() > 17) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("MAC Address cannot exceed %d characters", 17));
		}
		
		// 3. Unique Checks
		// Serial Number unique check (if set)
		if (entity.getSerialNumber() != null) {
			final Optional<CBabDevice> existingSerial = ((IBabDeviceRepository) repository).findBySerialNumber(entity.getSerialNumber());
			if (existingSerial.isPresent() && !existingSerial.get().getId().equals(entity.getId())) {
				throw new IllegalArgumentException("Device with this Serial Number already exists");
			}
		}
		
		// One device per company check
		final Optional<CBabDevice> existingCompanyDevice = ((IBabDeviceRepository) repository).findByCompanyId(entity.getCompany().getId());
		if (existingCompanyDevice.isPresent() && !existingCompanyDevice.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException("A device already exists for this company. Only one device per company is allowed.");
		}
	}

	/** Find device by serial number.
	 * @param serialNumber the serial number
	 * @return optional device */
	@Transactional (readOnly = true)
	public Optional<CBabDevice> findBySerialNumber(final String serialNumber) {
		Objects.requireNonNull(serialNumber, "Serial number cannot be null");
		return ((CBabDeviceService) repository).findBySerialNumber(serialNumber);
	}

	private CCompany getCurrentCompany() {
		Objects.requireNonNull(sessionService, "Session service required");
		final CCompany company = sessionService.getCurrentCompany();
		Objects.requireNonNull(company, "No active company");
		return company;
	}

	@Override
	public Class<CBabDevice> getEntityClass() { return CBabDevice.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabDeviceInitializerService.class; }

	/** Get or create the unique device for the current company.
	 * @return the device (existing or newly created) */
	@Transactional
	public CBabDevice getOrCreateDevice() {
		return getUniqueDevice().orElseGet(() -> {
			final CBabDevice device = new CBabDevice("Default Gateway", getCurrentCompany());
			device.setSerialNumber("SN-" + System.currentTimeMillis());
			device.setFirmwareVersion("1.0.0");
			device.setHardwareRevision("1.0");
			device.setDeviceStatus("Offline");
			return save(device);
		});
	}

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabDevice.class; }

	@Override
	public IAbstractRepository<CBabDevice> getRepository() { return repository; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Get the unique device for the current company.
	 * @return optional device */
	@Transactional (readOnly = true)
	public Optional<CBabDevice> getUniqueDevice() {
		final CCompany currentCompany = getCurrentCompany();
		return ((IBabDeviceRepository) repository).findByCompanyId(currentCompany.getId());
	}

	/** Get the unique device for a specific company. Used during initialization when no session context exists.
	 * @param company the company
	 * @return optional device */
	@Transactional (readOnly = true)
	public Optional<CBabDevice> getUniqueDevice(final CCompany company) {
		Objects.requireNonNull(company, "Company cannot be null");
		return ((IBabDeviceRepository) repository).findByCompanyId(company.getId());
	}

	/** Update device status and last seen timestamp.
	 * @param device the device
	 * @param status the new status */
	@Transactional
	public void updateDeviceStatus(final CBabDevice device, final String status) {
		Objects.requireNonNull(device, "Device cannot be null");
		Objects.requireNonNull(status, "Status cannot be null");
		device.setDeviceStatus(status);
		device.setLastSeen(LocalDateTime.now(clock));
		save(device);
	}
}
