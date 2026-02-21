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
import tech.derbent.bab.policybase.actionmask.service.CBabPolicyActionMaskROSService;
import tech.derbent.bab.policybase.actionmask.service.CPageServiceBabPolicyActionMaskROS;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;

/** ROS-specific action mask. */
@Entity
@Table (name = "cbab_policy_action_mask_ros")
@DiscriminatorValue ("ROS")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public final class CBabPolicyActionMaskROS extends CBabPolicyActionMaskBase<CBabPolicyActionMaskROS> {

	public static final String DEFAULT_COLOR = "#03A9F4";
	public static final String DEFAULT_ICON = "vaadin:connect-o";
	public static final String ENTITY_TITLE_PLURAL = "ROS Action Masks";
	public static final String ENTITY_TITLE_SINGULAR = "ROS Action Mask";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionMaskROS.class);
	public static final String MASK_KIND = "ROS";
	public static final String VIEW_NAME = "ROS Action Masks View";

	@Column (name = "target_topic", length = 255, nullable = false)
	@AMetaData (
			displayName = "Target Topic", required = false, readOnly = false,
			description = "ROS topic where action payload will be published", hidden = false, maxLength = 255
	)
	private String targetTopic = "/actions/event";

	@Column (name = "message_type", length = 120, nullable = false)
	@AMetaData (
			displayName = "Message Type", required = false, readOnly = false,
			description = "ROS message type used by this action mask", hidden = false, maxLength = 120
	)
	private String messageType = "std_msgs/String";

	@Column (name = "message_template_json", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Message Template JSON", required = false, readOnly = false,
			description = "Template payload mapped into outgoing ROS message", hidden = false
	)
	private String messageTemplateJson = "{}";

	protected CBabPolicyActionMaskROS() {}

	public CBabPolicyActionMaskROS(final String name, final CBabROSNode parentNode) {
		super(CBabPolicyActionMaskROS.class, name, parentNode);
		initializeDefaults();
	}

	@Override
	public Class<CBabROSNode> getAllowedNodeType() { return CBabROSNode.class; }

	@Override
	public String getMaskKind() { return MASK_KIND; }

	public String getMessageTemplateJson() { return messageTemplateJson; }

	public String getMessageType() { return messageType; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyActionMaskROS.class; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyActionMaskROSService.class; }

	public String getTargetTopic() { return targetTopic; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setMessageTemplateJson(final String messageTemplateJson) {
		this.messageTemplateJson = messageTemplateJson;
		updateLastModified();
	}

	public void setMessageType(final String messageType) {
		this.messageType = messageType;
		updateLastModified();
	}

	public void setTargetTopic(final String targetTopic) {
		this.targetTopic = targetTopic;
		updateLastModified();
	}
}
