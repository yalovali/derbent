package tech.derbent.bab.policybase.filter.domain;

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
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterROSService;
import tech.derbent.bab.policybase.filter.service.CPageServiceBabPolicyFilterROS;
import tech.derbent.bab.policybase.node.ros.CBabROSNode;

/** ROS-specific policy filter entity. */
@Entity
@Table (name = "cbab_policy_filter_ros")
@DiscriminatorValue ("ROS")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public final class CBabPolicyFilterROS extends CBabPolicyFilterBase<CBabPolicyFilterROS> {

	public static final String DEFAULT_COLOR = "#009688";
	public static final String DEFAULT_ICON = "vaadin:automation";
	public static final String DEFAULT_MESSAGE_TYPE_PATTERN = ".*";
	public static final String DEFAULT_NAMESPACE_FILTER = "/";
	public static final String DEFAULT_TOPIC_REGULAR_EXPRESSION = "/.*";
	public static final String ENTITY_TITLE_PLURAL = "ROS Policy Filters";
	public static final String ENTITY_TITLE_SINGULAR = "ROS Policy Filter";
	public static final String FILTER_KIND = "ROS";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterROS.class);
	public static final String VIEW_NAME = "ROS Policy Filters View";

	@Column (name = "message_type_pattern", length = 120, nullable = false)
	@AMetaData (
			displayName = "Message Type Pattern", required = false, readOnly = false,
			description = "Pattern for allowed ROS message types", hidden = false, maxLength = 120
	)
	private String messageTypePattern = DEFAULT_MESSAGE_TYPE_PATTERN;

	@Column (name = "namespace_filter", length = 120, nullable = false)
	@AMetaData (
			displayName = "Namespace Filter", required = false, readOnly = false,
			description = "ROS namespace that this filter applies to", hidden = false, maxLength = 120
	)
	private String namespaceFilter = DEFAULT_NAMESPACE_FILTER;

	@Column (name = "topic_regular_expression", length = 255, nullable = false)
	@AMetaData (
			displayName = "Topic Regex", required = false, readOnly = false,
			description = "Regex used to match ROS topics", hidden = false, maxLength = 255
	)
	private String topicRegularExpression = DEFAULT_TOPIC_REGULAR_EXPRESSION;

	/** Default constructor for JPA. */
	protected CBabPolicyFilterROS() {
		// JPA constructor must not initialize business defaults.
	}

	public CBabPolicyFilterROS(final String name, final CProject<?> project) {
		super(CBabPolicyFilterROS.class, name, project);
		initializeDefaults();
	}

	@Override
	public String getFilterKind() { return FILTER_KIND; }

	@Override
	public Class<CBabROSNode> getAllowedNodeType() { return CBabROSNode.class; }

	public String getMessageTypePattern() {
		return messageTypePattern != null && !messageTypePattern.isBlank() ? messageTypePattern : DEFAULT_MESSAGE_TYPE_PATTERN;
	}

	public String getNamespaceFilter() {
		return namespaceFilter != null && !namespaceFilter.isBlank() ? namespaceFilter : DEFAULT_NAMESPACE_FILTER;
	}

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilterROS.class; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyFilterROSService.class; }

	public String getTopicRegularExpression() {
		return topicRegularExpression != null && !topicRegularExpression.isBlank() ? topicRegularExpression : DEFAULT_TOPIC_REGULAR_EXPRESSION;
	}

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setMessageTypePattern(final String messageTypePattern) {
		this.messageTypePattern =
				messageTypePattern == null || messageTypePattern.isBlank() ? DEFAULT_MESSAGE_TYPE_PATTERN : messageTypePattern.trim();
		updateLastModified();
	}

	public void setNamespaceFilter(final String namespaceFilter) {
		this.namespaceFilter = namespaceFilter == null || namespaceFilter.isBlank() ? DEFAULT_NAMESPACE_FILTER : namespaceFilter.trim();
		updateLastModified();
	}

	public void setTopicRegularExpression(final String topicRegularExpression) {
		this.topicRegularExpression = topicRegularExpression == null || topicRegularExpression.isBlank() ? DEFAULT_TOPIC_REGULAR_EXPRESSION
				: topicRegularExpression.trim();
		updateLastModified();
	}
}
