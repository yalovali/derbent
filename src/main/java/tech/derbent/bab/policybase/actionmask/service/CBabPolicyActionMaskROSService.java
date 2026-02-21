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
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;

@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyActionMaskROSService extends CBabPolicyActionMaskBaseService<CBabPolicyActionMaskROS>
		implements IEntityRegistrable, IEntityWithView {

	public CBabPolicyActionMaskROSService(final IBabPolicyActionMaskROSRepository repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void copyTypeSpecificFieldsTo(final CBabPolicyActionMaskROS source, final CBabPolicyActionMaskROS target,
			final CCloneOptions options) {
		target.setTargetTopic(source.getTargetTopic());
		target.setMessageType(source.getMessageType());
		target.setMessageTemplateJson(source.getMessageTemplateJson());
	}

	@Override
	public Class<CBabPolicyActionMaskROS> getEntityClass() { return CBabPolicyActionMaskROS.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyActionMaskROSInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyActionMaskROS.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateTypeSpecificFields(final CBabPolicyActionMaskROS entity) {
		Check.notBlank(entity.getTargetTopic(), "Target topic is required");
		Check.notBlank(entity.getMessageType(), "Message type is required");
	}
}
