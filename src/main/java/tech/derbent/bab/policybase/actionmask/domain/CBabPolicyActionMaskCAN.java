package tech.derbent.bab.policybase.actionmask.domain;

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
import tech.derbent.bab.policybase.actionmask.service.CBabPolicyActionMaskCANService;
import tech.derbent.bab.policybase.actionmask.service.CPageServiceBabPolicyActionMaskCAN;
import tech.derbent.bab.policybase.node.can.CBabCanNode;

/** CAN-specific action mask. */
@Entity
@Table (name = "cbab_policy_action_mask_can")
@DiscriminatorValue ("CAN")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public final class CBabPolicyActionMaskCAN extends CBabPolicyActionMaskBase<CBabPolicyActionMaskCAN> {

	public static final String DEFAULT_COLOR = "#FF9800";
	public static final String DEFAULT_ICON = "vaadin:car";
	public static final String ENTITY_TITLE_PLURAL = "CAN Action Masks";
	public static final String ENTITY_TITLE_SINGULAR = "CAN Action Mask";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionMaskCAN.class);
	public static final String MASK_KIND = "CAN";
	public static final String VIEW_NAME = "CAN Action Masks View";

	@Column (name = "target_frame_id_hex", length = 32, nullable = false)
	@AMetaData (
			displayName = "Target Frame Id Hex", required = false, readOnly = false,
			description = "Destination CAN frame id template (hex)", hidden = false, maxLength = 32
	)
	private String targetFrameIdHex = "0x100";

	@Column (name = "payload_template_json", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Payload Template JSON", required = false, readOnly = false,
			description = "Payload template mapped to destination CAN frame", hidden = false
	)
	private String payloadTemplateJson = "{}";

	protected CBabPolicyActionMaskCAN() {}

	public CBabPolicyActionMaskCAN(final String name, final CBabCanNode parentNode) {
		super(CBabPolicyActionMaskCAN.class, name, parentNode);
		initializeDefaults();
	}

	@Override
	public Class<CBabCanNode> getAllowedNodeType() { return CBabCanNode.class; }

	@Override
	public String getMaskKind() { return MASK_KIND; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyActionMaskCAN.class; }

	public String getPayloadTemplateJson() { return payloadTemplateJson; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyActionMaskCANService.class; }

	public String getTargetFrameIdHex() { return targetFrameIdHex; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setPayloadTemplateJson(final String payloadTemplateJson) {
		this.payloadTemplateJson = payloadTemplateJson;
		updateLastModified();
	}

	public void setTargetFrameIdHex(final String targetFrameIdHex) {
		this.targetFrameIdHex = targetFrameIdHex;
		updateLastModified();
	}
}
