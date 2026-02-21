package tech.derbent.bab.policybase.filter.service;

import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;

/** Service for CAN policy filters. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyFilterCANService extends CBabPolicyFilterBaseService<CBabPolicyFilterCAN> implements IEntityRegistrable, IEntityWithView {

	private static final int MAX_PROTOCOL_VARIABLE_NAME_LENGTH = 255;

	public CBabPolicyFilterCANService(final IBabPolicyFilterCANRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void copyTypeSpecificFieldsTo(final CBabPolicyFilterCAN source, final CBabPolicyFilterCAN target, final CCloneOptions options) {
		target.setCanFrameIdRegularExpression(source.getCanFrameIdRegularExpression());
		target.setCanPayloadRegularExpression(source.getCanPayloadRegularExpression());
		target.setProtocolVariableNames(source.getProtocolVariableNames());
		target.setRequireExtendedFrame(source.getRequireExtendedFrame());
	}

	@Override
	public Class<CBabPolicyFilterCAN> getEntityClass() { return CBabPolicyFilterCAN.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyFilterCANInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilterCAN.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateTypeSpecificFields(final CBabPolicyFilterCAN entity) {
		validateStringLength(entity.getCanFrameIdRegularExpression(), "Frame-ID Regex", 100);
		validateStringLength(entity.getCanPayloadRegularExpression(), "Payload Regex", 255);
		validateRegularExpression(entity.getCanFrameIdRegularExpression(), "Frame-ID regular expression");
		validateRegularExpression(entity.getCanPayloadRegularExpression(), "Payload regular expression");
		validateProtocolVariableNames(entity.getProtocolVariableNames());
	}

	private void validateProtocolVariableNames(final List<String> protocolVariableNames) {
		if (protocolVariableNames == null) {
			return;
		}
		protocolVariableNames.forEach((final String variableName) -> validateStringLength(variableName, "Protocol Variable Name", MAX_PROTOCOL_VARIABLE_NAME_LENGTH));
	}
}
