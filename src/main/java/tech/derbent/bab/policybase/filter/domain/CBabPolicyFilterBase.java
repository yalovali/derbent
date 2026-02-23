package tech.derbent.bab.policybase.filter.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/** Abstract base entity for all BAB policy filter types. Holds common operational and node-compatibility settings while concrete subclasses carry
 * protocol-specific filter configuration. */
@Entity
@Table (name = "cbab_policy_filter", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"parent_node_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_filter_id"))
@Inheritance (strategy = InheritanceType.JOINED)
@DiscriminatorColumn (name = "filter_kind", discriminatorType = DiscriminatorType.STRING)
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public abstract class CBabPolicyFilterBase<EntityClass extends CBabPolicyFilterBase<EntityClass>> extends CEntityNamed<EntityClass>
		implements IHasComments, IHasAttachments, IHasLinks, IEntityRegistrable {

	private static final Map<String, Set<String>> EXCLUDED_FIELDS_BAB_POLICY = createExcludedFieldMap_BabPolicy();

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		final Map<String, Set<String>> map = new java.util.HashMap<>();
		map.put("CBabPolicyFilterBase", Set.of("parentNode", "canNodeEnabled", "fileNodeEnabled", "httpNodeEnabled", "modbusNodeEnabled",
				"rosNodeEnabled", "syslogNodeEnabled"));
		return Map.copyOf(map);
	}

	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_filter_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this filter", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@Column (name = "can_node_enabled", nullable = false)
	@AMetaData (displayName = "CAN Nodes", required = false, readOnly = false, description = "Enable this filter for CAN nodes", hidden = false)
	private Boolean canNodeEnabled = true;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_filter_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments and notes for this filter", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "file_node_enabled", nullable = false)
	@AMetaData (
			displayName = "File Nodes", required = false, readOnly = false, description = "Enable this filter for file input nodes", hidden = false
	)
	private Boolean fileNodeEnabled = true;
	@Column (name = "http_node_enabled", nullable = false)
	@AMetaData (
			displayName = "HTTP Nodes", required = false, readOnly = false, description = "Enable this filter for HTTP server nodes", hidden = false
	)
	private Boolean httpNodeEnabled = true;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "bab_policy_filter_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this filter", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Column (name = "log_execution", nullable = false)
	@AMetaData (displayName = "Log Execution", required = false, readOnly = false, description = "Log execution", hidden = false)
	private Boolean logExecution = false;
	@Column (name = "modbus_node_enabled", nullable = false)
	@AMetaData (displayName = "Modbus Nodes", required = false, readOnly = false, description = "Enable this filter for Modbus nodes", hidden = false)
	private Boolean modbusNodeEnabled = true;
	@ManyToOne (fetch = FetchType.EAGER, optional = false)
	@JoinColumn (name = "parent_node_id", nullable = false)
	@OnDelete (action = OnDeleteAction.CASCADE)
	@AMetaData (
			displayName = "Parent Node", required = true, readOnly = true, description = "Owning BAB node for this filter", hidden = true,
			dataProviderBean = "none"
	)
	@JsonIgnore
	private CBabNodeEntity<?> parentNode;
	@Column (name = "ros_node_enabled", nullable = false)
	@AMetaData (displayName = "ROS Nodes", required = false, readOnly = false, description = "Enable this filter for ROS nodes", hidden = false)
	private Boolean rosNodeEnabled = true;
	@Column (name = "syslog_node_enabled", nullable = false)
	@AMetaData (displayName = "Syslog Nodes", required = false, readOnly = false, description = "Enable this filter for syslog nodes", hidden = false)
	private Boolean syslogNodeEnabled = true;

	/** Default constructor for JPA. */
	protected CBabPolicyFilterBase() {
		// JPA constructor must not initialize business defaults.
	}

	protected CBabPolicyFilterBase(final Class<EntityClass> clazz, final String name, final CBabNodeEntity<?> parentNode) {
		super(clazz, name);
		setParentNode(parentNode);
	}

	public abstract Class<? extends CBabNodeEntity<?>> getAllowedNodeType();

	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public Boolean getCanNodeEnabled() { return canNodeEnabled; }

	@Override
	public Set<CComment> getComments() { return comments; }

	@Override
	public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
		return mergeExcludedFieldMaps(super.getExcludedFieldMapForScenario(scenario),
				getScenarioExcludedFieldMap(scenario, Map.of(), EXCLUDED_FIELDS_BAB_POLICY));
	}

	public Boolean getFileNodeEnabled() { return fileNodeEnabled; }

	public abstract String getFilterKind();

	public Boolean getHttpNodeEnabled() { return httpNodeEnabled; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public Boolean getLogExecution() { return logExecution; }

	public Boolean getModbusNodeEnabled() { return modbusNodeEnabled; }

	public abstract List<ROutputStructure> getOutputStructure();

	public CBabNodeEntity<?> getParentNode() { return parentNode; }

	public Boolean getRosNodeEnabled() { return rosNodeEnabled; }

	public Boolean getSyslogNodeEnabled() { return syslogNodeEnabled; }

	public boolean isEnabledForNodeType(final String nodeType) {
		if (nodeType == null) {
			return false;
		}
		return switch (nodeType.toLowerCase()) {
		case "can" -> Boolean.TRUE.equals(canNodeEnabled);
		case "modbus", "tcp_modbus" -> Boolean.TRUE.equals(modbusNodeEnabled);
		case "http", "http_server" -> Boolean.TRUE.equals(httpNodeEnabled);
		case "file", "file_input" -> Boolean.TRUE.equals(fileNodeEnabled);
		case "syslog" -> Boolean.TRUE.equals(syslogNodeEnabled);
		case "ros" -> Boolean.TRUE.equals(rosNodeEnabled);
		default -> false;
		};
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setCanNodeEnabled(final Boolean canNodeEnabled) {
		this.canNodeEnabled = canNodeEnabled;
		updateLastModified();
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setFileNodeEnabled(final Boolean fileNodeEnabled) {
		this.fileNodeEnabled = fileNodeEnabled;
		updateLastModified();
	}

	public void setHttpNodeEnabled(final Boolean httpNodeEnabled) {
		this.httpNodeEnabled = httpNodeEnabled;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setLogExecution(final Boolean logExecution) {
		this.logExecution = logExecution;
		updateLastModified();
	}

	public void setModbusNodeEnabled(final Boolean modbusNodeEnabled) {
		this.modbusNodeEnabled = modbusNodeEnabled;
		updateLastModified();
	}

	public void setParentNode(final CBabNodeEntity<?> parentNode) {
		this.parentNode = parentNode;
		updateLastModified();
	}

	public void setRosNodeEnabled(final Boolean rosNodeEnabled) {
		this.rosNodeEnabled = rosNodeEnabled;
		updateLastModified();
	}

	public void setSyslogNodeEnabled(final Boolean syslogNodeEnabled) {
		this.syslogNodeEnabled = syslogNodeEnabled;
		updateLastModified();
	}
}
