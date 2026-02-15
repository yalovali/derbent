package tech.derbent.bab.policybase.node.ros;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.links.domain.CLink;

/** CBabROSNode - ROS (Robot Operating System) virtual network node entity. Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Concrete entity with @Entity annotation. JPA Inheritance: JOINED strategy with @DiscriminatorValue - Inherits common fields from cbab_node
 * table - Stores ROS-specific fields in cnode_ros table - node_type discriminator = "ROS" Represents ROS communication nodes for robotics
 * applications. Example: ROS master, ROS nodes for topic/service communication. Used in BAB Actions Dashboard policy rule engine for ROS traffic
 * management. */
@Entity
@Table (name = "cnode_ros", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		}), @UniqueConstraint (columnNames = {
				"project_id", "physical_interface", "ros_master_port"
		})
})
@DiscriminatorValue ("ROS")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabROSNode extends CBabNodeEntity<CBabROSNode> {
	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#009688"; // Teal - ROS
	public static final String DEFAULT_ICON = "vaadin:automation";
	public static final String ENTITY_TITLE_PLURAL = "ROS Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "ROS Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabROSNode.class);
	public static final String VIEW_NAME = "ROS Nodes View";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ros_node_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this ROS node", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ros_node_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this ROS node", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ros_node_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this ROS node", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// ROS specific fields
	@Column (name = "ros_master_uri", length = 255, nullable = false)
	@AMetaData (
			displayName = "ROS Master URI", required = true, readOnly = false, description = "ROS master URI (e.g., http://localhost:11311)",
			hidden = false, maxLength = 255
	)
	private String rosMasterUri = "http://localhost:11311";
	@Column (name = "ros_master_port", nullable = false)
	@AMetaData (displayName = "ROS Master Port", required = true, readOnly = false, description = "ROS master port (default: 11311)", hidden = false)
	private Integer rosMasterPort = 11311;
	@Column (name = "ros_version", length = 20, nullable = false)
	@AMetaData (
			displayName = "ROS Version", required = false, readOnly = false, description = "ROS version (ROS1 or ROS2)", hidden = false,
			maxLength = 20, dataProviderBean = "pageservice", dataProviderMethod = "getAvailableRosVersions"
	)
	private String rosVersion = "ROS1";
	@Column (name = "node_namespace", length = 100, nullable = false)
	@AMetaData (
			displayName = "Node Namespace", required = false, readOnly = false, description = "ROS node namespace (e.g., /robot1)", hidden = false,
			maxLength = 100
	)
	private String nodeNamespace = "/";
	@Column (name = "topics", length = 500, nullable = false)
	@AMetaData (
			displayName = "Topics", required = false, readOnly = false, description = "Comma-separated list of ROS topics", hidden = false,
			maxLength = 500
	)
	private String topics = "/cmd_vel,/odom";
	@Column (name = "services", length = 500, nullable = false)
	@AMetaData (
			displayName = "Services", required = false, readOnly = false, description = "Comma-separated list of ROS services", hidden = false,
			maxLength = 500
	)
	private String services = "/get_state";
	@Column (name = "queue_size", nullable = false)
	@AMetaData (displayName = "Queue Size", required = false, readOnly = false, description = "Message queue size for topics", hidden = false)
	private Integer queueSize = 10;

	/** Default constructor for JPA. */
	protected CBabROSNode() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabROSNode(final String name, final CProject<?> project) {
		super(CBabROSNode.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public CBabROSNode(final String name, final CProject<?> project, final String physicalInterface, final String rosMasterUri) {
		super(CBabROSNode.class, name, project);
		this.rosMasterUri = rosMasterUri;
		setPhysicalInterface(physicalInterface);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// IHasColor implementation
	@Override
	public String getColor() {
		return DEFAULT_COLOR; // ROS nodes are teal
	}

	@Override
	public Set<CComment> getComments() { return comments; }

	public String getEntityColor() { return DEFAULT_COLOR; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public String getNodeNamespace() { return nodeNamespace; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	public Integer getQueueSize() { return queueSize; }

	public Integer getRosMasterPort() { return rosMasterPort; }

	// ROS specific getters and setters
	public String getRosMasterUri() { return rosMasterUri; }

	public String getRosVersion() { return rosVersion; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	public String getServices() { return services; }

	public String getTopics() { return topics; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults
		if ((rosMasterUri == null) || rosMasterUri.isEmpty()) {
			rosMasterUri = "http://localhost:11311";
		}
		if (rosMasterPort == null) {
			rosMasterPort = 11311;
		}
		if ((rosVersion == null) || rosVersion.isEmpty()) {
			rosVersion = "ROS1";
		}
		if ((nodeNamespace == null) || nodeNamespace.isEmpty()) {
			nodeNamespace = "/";
		}
		if ((topics == null) || topics.isEmpty()) {
			topics = "/cmd_vel,/odom";
		}
		if ((services == null) || services.isEmpty()) {
			services = "/get_state";
		}
		if (queueSize == null) {
			queueSize = 10;
		}
		// Set default physical interface if not set
		if ((getPhysicalInterface() == null) || getPhysicalInterface().isEmpty()) {
			setPhysicalInterface("eth0");
		}
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if this ROS node uses ROS2.
	 * @return true if ROS version is ROS2 */
	public boolean isRos2() { return "ROS2".equalsIgnoreCase(rosVersion); }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setColor(final String color) {
		// Color is static for node types, determined by node type constant
		// Not configurable per instance for consistency
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setNodeNamespace(final String nodeNamespace) {
		this.nodeNamespace = nodeNamespace;
		updateLastModified();
	}

	public void setQueueSize(final Integer queueSize) {
		this.queueSize = queueSize;
		updateLastModified();
	}

	public void setRosMasterPort(final Integer rosMasterPort) {
		this.rosMasterPort = rosMasterPort;
		updateLastModified();
	}

	public void setRosMasterUri(final String rosMasterUri) {
		this.rosMasterUri = rosMasterUri;
		updateLastModified();
	}

	public void setRosVersion(final String rosVersion) {
		this.rosVersion = rosVersion;
		updateLastModified();
	}

	public void setServices(final String services) {
		this.services = services;
		updateLastModified();
	}

	public void setTopics(final String topics) {
		this.topics = topics;
		updateLastModified();
	}
}
