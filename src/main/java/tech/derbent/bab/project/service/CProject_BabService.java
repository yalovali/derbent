package tech.derbent.bab.project.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.projects.service.CProjectTypeService;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.base.session.service.ISessionService;

@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CProject_BabService extends CProjectService<CProject_Bab> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CProject_BabService.class);

	public CProject_BabService(final IProject_BabRepository repository, final Clock clock, final ISessionService sessionService,
			final ApplicationEventPublisher eventPublisher, final CProjectTypeService projectTypeService,
			final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, eventPublisher, projectTypeService, statusService);
	}

	@Override
	public Class<CProject_Bab> getEntityClass() { return CProject_Bab.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProject_BabInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProject_Bab.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public CProject_Bab newEntity() {
		// Constructor already calls initializeDefaults() which calls initializeNewEntity()
		// No need to call initializeNewEntity() again - that would be double initialization
		return new CProject_Bab("New BAB Project",
				sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company for BAB project creation")));
	}

	@Override
	protected void validateEntity(final CProject_Bab entity) {
		super.validateEntity(entity);
		// IP Address Validation
		if (!(entity.getIpAddress() != null && !entity.getIpAddress().isBlank())) {
			return;
		}
		// Use validateStringLength helper for length validation
		validateStringLength(entity.getIpAddress(), "IP Address", 45);
		// Regex for IPv4 or IPv6
		final String ipRegex =
				"^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{0,4}:){7}[0-9a-fA-F]{0,4}$";
		if (!entity.getIpAddress().matches(ipRegex)) {
			throw new CValidationException("Invalid IP address format (IPv4 or IPv6)");
		}
	}
}
