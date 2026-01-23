package tech.derbent.bab.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.bab.device.domain.CBabDevice;

/** CBabNodeROS - ROS (Robot Operating System) communication node. Following Derbent pattern: Concrete entity with specific fields. */
@Entity
@Table (name = "cbab_node_ros")
public class CBabNodeROS extends CBabNode<CBabNodeROS> {

	public static final String DEFAULT_COLOR = "#9C27B0";
	public static final String DEFAULT_ICON = "vaadin:automation";
	public static final String ENTITY_TITLE_PLURAL = "ROS Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "ROS Node";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeROS.class);
	
	public static final String VIEW_NAME = "ROS Node Configuration";
	@Column (name = "ros_master_uri", nullable = true, length = 255)
	@Size (max = 255)
	@AMetaData (
			displayName = "ROS Master URI", required = false, readOnly = false, description = "ROS Master URI (e.g., http://localhost:11311)",
			hidden = false, maxLength = 255
	)
	private String rosMasterUri;
	@Column (name = "node_name", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (displayName = "Node Name", required = false, readOnly = false, description = "ROS node name", hidden = false, maxLength = 100)
	private String nodeName;
	@Column (name = "namespace", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (displayName = "Namespace", required = false, readOnly = false, description = "ROS namespace", hidden = false, maxLength = 100)
	private String namespace;
	@Column (name = "ros_version", nullable = true, length = 20)
	@Size (max = 20)
	@AMetaData (
			displayName = "ROS Version", required = false, readOnly = false, description = "ROS version (ROS1 or ROS2)", hidden = false,
			maxLength = 20
	)
	private String rosVersion;

	/** Default constructor for JPA. */
	public CBabNodeROS() {
		super();
		initializeDefaults(); // ✅ MANDATORY call in concrete class constructor
	}

	public CBabNodeROS(final String name, final CBabDevice device) {
		super(CBabNodeROS.class, name, device, "ROS");
		initializeDefaults(); // ✅ MANDATORY call in concrete class constructor
	}

	public String getNamespace() { return namespace; }

	public String getNodeName() { return nodeName; }

	// Getters and Setters
	public String getRosMasterUri() { return rosMasterUri; }

	public String getRosVersion() { return rosVersion; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		rosMasterUri = "http://localhost:11311";
		rosVersion = "ROS1";
		namespace = "/";
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
		updateLastModified();
	}

	public void setNodeName(final String nodeName) {
		this.nodeName = nodeName;
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

	@Override
	protected void copyEntityTo(final tech.derbent.api.entity.domain.CEntityDB<?> target, @SuppressWarnings("rawtypes") final tech.derbent.api.entity.service.CAbstractService serviceTarget, final tech.derbent.api.interfaces.CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityTo(target, serviceTarget, options);
		
		// STEP 2: Type-check target
		if (target instanceof CBabNodeROS) {
			final CBabNodeROS targetNode = (CBabNodeROS) target;
			
			// STEP 3: Copy basic fields (always)
			copyField(this::getRosMasterUri, targetNode::setRosMasterUri);
			copyField(this::getRosVersion, targetNode::setRosVersion);
			copyField(this::getNamespace, targetNode::setNamespace);
			copyField(this::getNodeName, targetNode::setNodeName);
			
			// STEP 4: Handle relations (conditional)
			if (options.includesRelations()) {
				copyField(this::getDevice, targetNode::setDevice);
			}
			
			// STEP 5: Log for debugging
			LOGGER.debug("Copied ROS node {} with options: {}", getName(), options);
		}
	}
}
