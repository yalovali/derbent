package tech.derbent.bab.policybase.filter.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterROS;

/** Service for ROS policy filters. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyFilterROSService extends CBabPolicyFilterBaseService<CBabPolicyFilterROS> implements IEntityRegistrable, IEntityWithView {

	public CBabPolicyFilterROSService(final IBabPolicyFilterROSRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void copyTypeSpecificFieldsTo(final CBabPolicyFilterROS source, final CBabPolicyFilterROS target, final CCloneOptions options) {
		target.setTopicRegularExpression(source.getTopicRegularExpression());
		target.setMessageTypePattern(source.getMessageTypePattern());
		target.setNamespaceFilter(source.getNamespaceFilter());
	}

	@Override
	public Class<CBabPolicyFilterROS> getEntityClass() { return CBabPolicyFilterROS.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyFilterROSInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilterROS.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateTypeSpecificFields(final CBabPolicyFilterROS entity) {
		validateStringLength(entity.getTopicRegularExpression(), "Topic Regex", 255);
		validateStringLength(entity.getMessageTypePattern(), "Message Type Pattern", 120);
		validateStringLength(entity.getNamespaceFilter(), "Namespace Filter", 120);
		validateRegularExpression(entity.getTopicRegularExpression(), "Topic regular expression");
		validateRegularExpression(entity.getMessageTypePattern(), "Message type pattern");
	}
}
