package tech.derbent.bab.policybase.filter.domain;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.annotation.JsonFilter;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCANService;
import tech.derbent.bab.policybase.filter.service.CPageServiceBabPolicyFilterCAN;
import tech.derbent.bab.policybase.node.can.CBabCanNode;

/** CAN-specific policy filter entity. */
@Entity
@Table (name = "cbab_policy_filter_can")
@DiscriminatorValue ("CAN")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public final class CBabPolicyFilterCAN extends CBabPolicyFilterBase<CBabPolicyFilterCAN> {

	public static final String DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION = ".*";
	public static final String DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION = ".*";
	public static final String DEFAULT_COLOR = "#FF9800";
	public static final String DEFAULT_ICON = "vaadin:car";
	public static final String ENTITY_TITLE_PLURAL = "CAN Policy Filters";
	public static final String ENTITY_TITLE_SINGULAR = "CAN Policy Filter";
	public static final String FILTER_KIND = "CAN";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterCAN.class);
	public static final String VIEW_NAME = "CAN Policy Filters View";
	@Column (name = "can_frame_id_regular_expression", length = 100, nullable = false)
	@AMetaData (
			displayName = "Frame-ID Regex", required = false, readOnly = false, description = "Regex used to match CAN frame IDs", hidden = false,
			maxLength = 100
	)
	private String canFrameIdRegularExpression = DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION;
	@Column (name = "can_payload_regular_expression", length = 255, nullable = false)
	@AMetaData (
			displayName = "Payload Regex", required = false, readOnly = false, description = "Regex used to match CAN payload values", hidden = false,
			maxLength = 255
	)
	private String canPayloadRegularExpression = DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION;
	@Column (name = "protocol_variable_names", length = 4000)
	@AMetaData (
			displayName = "Protocol Variables", required = false, readOnly = false,
			description = "Protocol variable names selected from loaded CAN protocol JSON data", hidden = false, useGridSelection = true,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfProtocolVariableNames", dataProviderParamBean = "context",
			dataProviderParamMethod = "getValue"
	)
	private List<String> protocolVariableNames = new ArrayList<>();
	@Column (name = "require_extended_frame", nullable = false)
	@AMetaData (
			displayName = "Require Extended Frame", required = false, readOnly = false,
			description = "If enabled, only CAN extended-frame packets are accepted", hidden = false
	)
	private Boolean requireExtendedFrame = false;

	/** Default constructor for JPA. */
	protected CBabPolicyFilterCAN() {
		// JPA constructor must not initialize business defaults.
	}

	public CBabPolicyFilterCAN(final String name, final CBabCanNode parentNode) {
		super(CBabPolicyFilterCAN.class, name, parentNode);
		initializeDefaults();
	}

	@Override
	public Class<CBabCanNode> getAllowedNodeType() { return CBabCanNode.class; }

	public String getCanFrameIdRegularExpression() {
		return canFrameIdRegularExpression != null && !canFrameIdRegularExpression.isBlank() ? canFrameIdRegularExpression
				: DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION;
	}

	public String getCanPayloadRegularExpression() {
		return canPayloadRegularExpression != null && !canPayloadRegularExpression.isBlank() ? canPayloadRegularExpression
				: DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION;
	}

	@Override
	public String getFilterKind() { return FILTER_KIND; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilterCAN.class; }

	public List<String> getProtocolVariableNames() { return protocolVariableNames; }

	public Boolean getRequireExtendedFrame() { return requireExtendedFrame; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyFilterCANService.class; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setCanFrameIdRegularExpression(final String canFrameIdRegularExpression) {
		this.canFrameIdRegularExpression = canFrameIdRegularExpression == null || canFrameIdRegularExpression.isBlank()
				? DEFAULT_CAN_FRAME_ID_REGULAR_EXPRESSION : canFrameIdRegularExpression.trim();
		updateLastModified();
	}

	public void setCanPayloadRegularExpression(final String canPayloadRegularExpression) {
		this.canPayloadRegularExpression = canPayloadRegularExpression == null || canPayloadRegularExpression.isBlank()
				? DEFAULT_CAN_PAYLOAD_REGULAR_EXPRESSION : canPayloadRegularExpression.trim();
		updateLastModified();
	}

	public void setProtocolVariableNames(final List<String> protocolVariableNames) {
		if (protocolVariableNames == null) {
			this.protocolVariableNames = new ArrayList<>();
			updateLastModified();
			return;
		}
		this.protocolVariableNames = new ArrayList<>(protocolVariableNames.stream()
				.filter(variableName -> variableName != null && !variableName.isBlank()).map(String::trim).distinct().toList());
		updateLastModified();
	}

	public void setRequireExtendedFrame(final Boolean requireExtendedFrame) {
		this.requireExtendedFrame = requireExtendedFrame;
		updateLastModified();
	}
}
