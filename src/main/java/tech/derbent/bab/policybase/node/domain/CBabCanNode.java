package tech.derbent.bab.policybase.node.domain;

import java.util.HashSet;
import java.util.Set;
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
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.links.domain.CLink;

/** CBabCanNode - CAN Bus virtual network node entity. Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent pattern: Concrete
 * entity with @Entity annotation. JPA Inheritance: JOINED strategy with @DiscriminatorValue - Inherits common fields from cbab_node table - Stores
 * CAN-specific fields in cnode_can table - node_type discriminator = "CAN_BUS" Represents CAN bus virtual nodes mapped to physical CAN interfaces.
 * Example: can0, can1 interfaces for vehicle communication. Used in BAB Actions Dashboard policy rule engine for CAN traffic management. */
@Entity
@Table (name = "cnode_can", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		}), @UniqueConstraint (columnNames = {
				"project_id", "physical_interface", "bitrate"
		})
})
@DiscriminatorValue ("CAN_BUS")
@Profile ("bab")
public class CBabCanNode extends CBabNodeEntity<CBabCanNode> {
	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#FF9800"; // Orange - CAN bus
	public static final String DEFAULT_ICON = "vaadin:car";
	public static final String ENTITY_TITLE_PLURAL = "CAN Bus Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "CAN Bus Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabCanNode.class);
	public static final String VIEW_NAME = "CAN Bus Nodes View";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "can_node_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this CAN node", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "can_node_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this CAN node", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "can_node_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this CAN node", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// CAN bus specific fields
	@Column (name = "bitrate", nullable = false)
	@AMetaData (
			displayName = "Bitrate (bps)", required = true, readOnly = false, description = "CAN bus bitrate (e.g., 250000, 500000, 1000000)",
			hidden = false
	)
	private Integer bitrate = 500000;
	@Column (name = "listen_only", nullable = false)
	@AMetaData (
			displayName = "Listen Only", required = false, readOnly = false, description = "Enable listen-only mode (no ACK sent)", hidden = false
	)
	private Boolean listenOnly = false;
	@Column (name = "loopback_mode", nullable = false)
	@AMetaData (displayName = "Loopback Mode", required = false, readOnly = false, description = "Enable loopback mode for testing", hidden = false)
	private Boolean loopbackMode = false;
	@Column (name = "error_warning_limit", nullable = false)
	@AMetaData (
			displayName = "Error Warning Limit", required = false, readOnly = false, description = "CAN error warning limit (default: 96)",
			hidden = false
	)
	private Integer errorWarningLimit = 96;
	@Column (name = "protocol_type", length = 50)
	@AMetaData (
			displayName = "Protocol Type", required = false, readOnly = false, description = "CAN bus protocol type (XCP, UDS)", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getAvailableProtocolTypes"
	)
	private String protocolType;
	@Column (name = "protocol_definition_file", length = 500)
	@AMetaData (
			displayName = "Protocol Definition File", required = false, readOnly = false,
			description = "Path to protocol definition file (e.g., XCP A2L, UDS ODX)", hidden = false, isFilePath = true
	)
	private String protocolDefinitionFile;

	/** Default constructor for JPA. */
	protected CBabCanNode() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabCanNode(final String name, final CProject<?> project) {
		super(CBabCanNode.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public CBabCanNode(final String name, final CProject<?> project, final String physicalInterface, final Integer bitrate) {
		super(CBabCanNode.class, name, project);
		this.bitrate = bitrate;
		setPhysicalInterface(physicalInterface);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	@Override
	protected String generateDefaultNodeConfig() {
		return """
				{
				    "nodeId": "%s",
				    "nodeType": "CAN_BUS",
				    "physicalInterface": "%s",
				    "active": %s,
				    "priority": %d,
				    "canConfig": {
				        "bitrate": %d,
				        "listenOnly": %s,
				        "loopbackMode": %s,
				        "errorWarningLimit": %d
				    }
				}
				""".formatted(getId(), getPhysicalInterface(), getIsActive(), getPriorityLevel(), bitrate, listenOnly, loopbackMode,
				errorWarningLimit);
	}

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// CAN bus specific getters and setters
	public Integer getBitrate() { return bitrate; }

	// IHasColor implementation
	@Override
	public String getColor() {
		return DEFAULT_COLOR; // CAN nodes are orange
	}

	@Override
	public Set<CComment> getComments() { return comments; }

	public String getEntityColor() { return DEFAULT_COLOR; }

	public Integer getErrorWarningLimit() { return errorWarningLimit; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public Boolean getListenOnly() { return listenOnly; }

	public Boolean getLoopbackMode() { return loopbackMode; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	public String getProtocolDefinitionFile() { return protocolDefinitionFile; }

	public String getProtocolType() { return protocolType; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults
		if (bitrate == null) {
			bitrate = 500000;
		}
		if (listenOnly == null) {
			listenOnly = false;
		}
		if (loopbackMode == null) {
			loopbackMode = false;
		}
		if (errorWarningLimit == null) {
			errorWarningLimit = 96;
		}
		// Set default physical interface if not set
		if ((getPhysicalInterface() == null) || getPhysicalInterface().isEmpty()) {
			setPhysicalInterface("can0");
		}
		// Generate initial node configuration JSON
		setNodeConfigJson(generateDefaultNodeConfig());
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if this CAN node is configured for high-speed CAN.
	 * @return true if bitrate >= 500000 */
	public boolean isHighSpeedCan() { return (bitrate != null) && (bitrate >= 500000); }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setBitrate(final Integer bitrate) {
		this.bitrate = bitrate;
		updateLastModified();
	}

	@Override
	public void setColor(final String color) {
		// Color is static for node types, determined by node type constant
		// Not configurable per instance for consistency
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setErrorWarningLimit(final Integer errorWarningLimit) {
		this.errorWarningLimit = errorWarningLimit;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setListenOnly(final Boolean listenOnly) {
		this.listenOnly = listenOnly;
		updateLastModified();
	}

	public void setLoopbackMode(final Boolean loopbackMode) {
		this.loopbackMode = loopbackMode;
		updateLastModified();
	}

	public void setProtocolDefinitionFile(final String protocolDefinitionFile) {
		this.protocolDefinitionFile = protocolDefinitionFile;
		updateLastModified();
	}

	public void setProtocolType(final String protocolType) {
		this.protocolType = protocolType;
		updateLastModified();
	}
}
