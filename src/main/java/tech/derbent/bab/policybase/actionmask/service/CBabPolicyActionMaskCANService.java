package tech.derbent.bab.policybase.actionmask.service;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.actionmask.domain.ROutputActionMapping;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;
import tech.derbent.bab.policybase.filter.domain.ROutputStructure;
import tech.derbent.bab.policybase.node.can.CBabCanNode;
import tech.derbent.bab.policybase.node.can.CBabCanNodeService;

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
		Check.notBlank(entity.getOutputMethod(), "Output method is required for CAN action masks");
		final String normalizedOutputMethod = entity.getOutputMethod().trim().toLowerCase(Locale.ROOT);
		final boolean isAllowedOutputMethod = normalizedOutputMethod.equals(CBabPolicyActionMaskCAN.OUTPUT_METHOD_XCP_DOWNLOAD.toLowerCase(Locale.ROOT))
				|| normalizedOutputMethod.equals(CBabPolicyActionMaskCAN.OUTPUT_METHOD_XCP_UPLOAD.toLowerCase(Locale.ROOT));
		if (!isAllowedOutputMethod) {
			throw new CValidationException("CAN output method must be either '%s' or '%s'"
					.formatted(CBabPolicyActionMaskCAN.OUTPUT_METHOD_XCP_DOWNLOAD, CBabPolicyActionMaskCAN.OUTPUT_METHOD_XCP_UPLOAD));
		}
		validateOutputActionMappings(entity);
	}

	private void validateOutputActionMappings(final CBabPolicyActionMaskCAN entity) {
		final List<ROutputActionMapping> mappings = entity.getOutputActionMappings();
		if (mappings == null || mappings.isEmpty()) {
			return;
		}
		Check.notNull(entity.getPolicyAction(), "Policy action is required for output mappings");
		Check.notNull(entity.getPolicyAction().getPolicyRule(), "Policy rule is required for output mappings");
		Check.notNull(entity.getPolicyAction().getPolicyRule().getFilter(), "A source filter is required for output mappings");
		Check.instanceOf(entity.getPolicyAction().getPolicyRule().getFilter(), CBabPolicyFilterCAN.class,
				"CAN action mask output mappings require a CAN source filter");
		Check.instanceOf(entity.getDestinationNode(), CBabCanNode.class, "CAN action mask output mappings require a CAN destination node");
		final CBabPolicyFilterCAN sourceFilter = (CBabPolicyFilterCAN) entity.getPolicyAction().getPolicyRule().getFilter();
		final Map<String, ROutputStructure> sourceOutputsByName = new LinkedHashMap<>();
		sourceFilter.getOutputStructure().forEach(output -> {
			if (output == null || output.name().isBlank()) {
				return;
			}
			sourceOutputsByName.putIfAbsent(CBabPolicyFilterCAN.normalizeVariableName(output.name()), output);
		});
		final CBabCanNode destinationNode = (CBabCanNode) entity.getDestinationNode();
		String destinationProtocolJson = destinationNode.getProtocolFileJson();
		if ((destinationProtocolJson == null || destinationProtocolJson.isBlank()) && destinationNode.getId() != null) {
			destinationProtocolJson = CSpringContext.getBean(CBabCanNodeService.class)
					.loadProtocolContentFromDb(destinationNode.getId(), CBabCanNodeService.EProtocolContentField.JSON);
		}
		final Map<String, ROutputStructure> destinationOutputsByName = CBabPolicyFilterCAN
				.getOutputStructureByVariableName(destinationProtocolJson);
		for (final ROutputActionMapping mapping : mappings) {
			if (mapping == null || mapping.outputName().isBlank()) {
				continue;
			}
			final ROutputStructure sourceOutput = sourceOutputsByName.get(CBabPolicyFilterCAN.normalizeVariableName(mapping.outputName()));
			if (sourceOutput == null) {
				throw new CValidationException("Mapped source output '%s' is not available in selected CAN filter"
						.formatted(mapping.outputName()));
			}
			Check.notBlank(mapping.targetProtocolVariableName(),
					"Target protocol variable is required for mapped output '%s'".formatted(mapping.outputName()));
			final ROutputStructure destinationOutput = destinationOutputsByName
					.get(CBabPolicyFilterCAN.normalizeVariableName(mapping.targetProtocolVariableName()));
			if (destinationOutput == null) {
				throw new CValidationException("Mapped destination variable '%s' is not available in destination CAN node protocol"
						.formatted(mapping.targetProtocolVariableName()));
			}
			final String sourceDataType = CBabPolicyFilterCAN.normalizeDataType(sourceOutput.dataType());
			final String destinationDataType = CBabPolicyFilterCAN.normalizeDataType(destinationOutput.dataType());
			if (sourceDataType.isBlank() || destinationDataType.isBlank() || !sourceDataType.equalsIgnoreCase(destinationDataType)) {
				throw new CValidationException("Data type mismatch for mapping '%s' -> '%s': source '%s', destination '%s'"
						.formatted(sourceOutput.name(), destinationOutput.name(), sourceOutput.dataType(), destinationOutput.dataType()));
			}
		}
	}
}
