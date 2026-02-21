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
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;

@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyActionMaskCANService extends CBabPolicyActionMaskBaseService<CBabPolicyActionMaskCAN>
		implements IEntityRegistrable, IEntityWithView {

	public CBabPolicyActionMaskCANService(final IBabPolicyActionMaskCANRepository repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void copyTypeSpecificFieldsTo(final CBabPolicyActionMaskCAN source, final CBabPolicyActionMaskCAN target,
			final CCloneOptions options) {
		target.setTargetFrameIdHex(source.getTargetFrameIdHex());
		target.setPayloadTemplateJson(source.getPayloadTemplateJson());
	}

	@Override
	public Class<CBabPolicyActionMaskCAN> getEntityClass() { return CBabPolicyActionMaskCAN.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyActionMaskCANInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyActionMaskCAN.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateTypeSpecificFields(final CBabPolicyActionMaskCAN entity) {
		Check.notBlank(entity.getTargetFrameIdHex(), "Target frame id hex is required");
	}
}
