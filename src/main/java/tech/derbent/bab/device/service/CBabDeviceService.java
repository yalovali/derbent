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
