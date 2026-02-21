package tech.derbent.bab.policybase.actionmask.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskFile;

@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyActionMaskFileService extends CBabPolicyActionMaskBaseService<CBabPolicyActionMaskFile>
		implements IEntityRegistrable, IEntityWithView {

	public CBabPolicyActionMaskFileService(final IBabPolicyActionMaskFileRepository repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void copyTypeSpecificFieldsTo(final CBabPolicyActionMaskFile source, final CBabPolicyActionMaskFile target,
			final CCloneOptions options) {
		target.setOutputFilePattern(source.getOutputFilePattern());
		target.setSerializationMode(source.getSerializationMode());
	}

	@Override
	public Class<CBabPolicyActionMaskFile> getEntityClass() { return CBabPolicyActionMaskFile.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyActionMaskFileInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyActionMaskFile.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateTypeSpecificFields(final CBabPolicyActionMaskFile entity) {
		Check.notBlank(entity.getOutputFilePattern(), "Output file pattern is required");
		Check.notBlank(entity.getSerializationMode(), "Serialization mode is required");
	}
}
