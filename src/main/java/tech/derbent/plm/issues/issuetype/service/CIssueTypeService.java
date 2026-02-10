package tech.derbent.plm.issues.issuetype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.issues.issuetype.domain.CIssueType;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CIssueTypeService extends CTypeEntityService<CIssueType> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CIssueTypeService.class);

	CIssueTypeService(final IIssueTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CIssueType issueType) {
		return super.checkDeleteAllowed(issueType);
	}

	@Override
	public Class<CIssueType> getEntityClass() { return CIssueType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CIssueTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceIssueType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CIssueType entity) {
		super.validateEntity(entity);
		// Unique Name Check - use base class helper
		validateUniqueNameInCompany((IIssueTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
