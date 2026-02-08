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

/**
 * CBabCanNode - CAN Bus virtual network node entity.
 * 
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete entity with @Entity annotation.
 * 
 * JPA Inheritance: JOINED strategy with @DiscriminatorValue
 * - Inherits common fields from cbab_node table
 * - Stores CAN-specific fields in cnode_can table
 * - node_type discriminator = "CAN_BUS"
 * 
 * Represents CAN bus virtual nodes mapped to physical CAN interfaces.
 * Example: can0, can1 interfaces for vehicle communication.
 * Used in BAB Actions Dashboard policy rule engine for CAN traffic management.
 */
@Entity
@Table(name = "cnode_can", uniqueConstraints = {
	@UniqueConstraint(columnNames = {
		"project_id", "name"
	}),
	@UniqueConstraint(columnNames = {
		"project_id", "physical_interface", "bitrate"
	})
})
@DiscriminatorValue("CAN_BUS")
@Profile("bab")
public class CBabCanNode extends CBabNodeEntity<CBabCanNode> {
	
	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#FF9800"; // Orange - CAN bus
	public static final String DEFAULT_ICON = "vaadin:car";
	public static final String ENTITY_TITLE_PLURAL = "CAN Bus Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "CAN Bus Node";
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabCanNode.class);
	public static final String VIEW_NAME = "CAN Bus Nodes View";
	
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "can_node_id")
	@AMetaData(
		displayName = "Attachments",
		required = false,
		readOnly = false,
		description = "File attachments for this CAN node",
		hidden = false,
		dataProviderBean = "CAttachmentService",
		createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "can_node_id")
	@AMetaData(
		displayName = "Comments",
		required = false,
		readOnly = false,
		description = "Comments for this CAN node",
		hidden = false,
		dataProviderBean = "CCommentService",
		createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "can_node_id")
	@AMetaData(
		displayName = "Links",
		required = false,
		readOnly = false,
		description = "Related links for this CAN node",
		hidden = false,
		dataProviderBean = "CLinkService",
		createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	
	// CAN bus specific fields
	@Column(name = "bitrate", nullable = false)
	@AMetaData(
		displayName = "Bitrate (bps)",
		required = true,
		readOnly = false,
		description = "CAN bus bitrate (e.g., 250000, 500000, 1000000)",
		hidden = false
	)
	private Integer bitrate = 500000;
	
	@Column(name = "sample_point", nullable = false)
	@AMetaData(
		displayName = "Sample Point (%)",
		required = false,
		readOnly = false,
		description = "CAN sample point percentage (typically 87.5%)",
		hidden = false
	)
	private Double samplePoint = 87.5;
	
	@Column(name = "listen_only", nullable = false)
	@AMetaData(
		displayName = "Listen Only",
		required = false,
		readOnly = false,
		description = "Enable listen-only mode (no ACK sent)",
		hidden = false
	)
	private Boolean listenOnly = false;
	
	@Column(name = "loopback_mode", nullable = false)
	@AMetaData(
		displayName = "Loopback Mode",
		required = false,
		readOnly = false,
		description = "Enable loopback mode for testing",
		hidden = false
	)
	private Boolean loopbackMode = false;
	
	@Column(name = "triple_sampling", nullable = false)
	@AMetaData(
		displayName = "Triple Sampling",
		required = false,
		readOnly = false,
		description = "Enable triple sampling for improved noise immunity",
		hidden = false
	)
	private Boolean tripleSampling = false;
	
	@Column(name = "error_warning_limit", nullable = false)
	@AMetaData(
		displayName = "Error Warning Limit",
		required = false,
		readOnly = false,
		description = "CAN error warning limit (default: 96)",
		hidden = false
	)
	private Integer errorWarningLimit = 96;
	
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
			        "samplePoint": %.1f,
			        "listenOnly": %s,
			        "loopbackMode": %s,
			        "tripleSampling": %s,
			        "errorWarningLimit": %d
			    }
			}
			""".formatted(
				getId(),
				getPhysicalInterface(),
				getIsActive(),
				getPriorityLevel(),
				bitrate,
				samplePoint,
				listenOnly,
				loopbackMode,
				tripleSampling,
				errorWarningLimit
			);
	}
	
	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() {
		return attachments;
	}
	
	// IHasColor implementation
	@Override
	public String getColor() {
		return DEFAULT_COLOR; // CAN nodes are orange
	}
	
	@Override
	public Set<CComment> getComments() {
		return comments;
	}
	
	public String getEntityColor() {
		return DEFAULT_COLOR;
	}
	
	@Override
	public Set<CLink> getLinks() {
		return links;
	}
	
	@Override
	public Class<?> getPageServiceClass() {
		return Object.class;
	}
	
	// CAN bus specific getters and setters
	public Integer getBitrate() {
		return bitrate;
	}
	
	public Double getSamplePoint() {
		return samplePoint;
	}
	
	public Boolean getListenOnly() {
		return listenOnly;
	}
	
	public Boolean getLoopbackMode() {
		return loopbackMode;
	}
	
	public Boolean getTripleSampling() {
		return tripleSampling;
	}
	
	public Integer getErrorWarningLimit() {
		return errorWarningLimit;
	}
	
	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() {
		return Object.class;
	}
	
	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults
		if (bitrate == null) {
			bitrate = 500000;
		}
		if (samplePoint == null) {
			samplePoint = 87.5;
		}
		if (listenOnly == null) {
			listenOnly = false;
		}
		if (loopbackMode == null) {
			loopbackMode = false;
		}
		if (tripleSampling == null) {
			tripleSampling = false;
		}
		if (errorWarningLimit == null) {
			errorWarningLimit = 96;
		}
		
		// Set default physical interface if not set
		if (getPhysicalInterface() == null || getPhysicalInterface().isEmpty()) {
			setPhysicalInterface("can0");
		}
		
		// Generate initial node configuration JSON
		setNodeConfigJson(generateDefaultNodeConfig());
		
		// MANDATORY: Call service initialization at end (RULE 3)
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
	
	@Override
	public void setAttachments(Set<CAttachment> attachments) {
		this.attachments = attachments;
	}
	
	@Override
	public void setColor(final String color) {
		// Color is static for node types, determined by node type constant
		// Not configurable per instance for consistency
	}
	
	@Override
	public void setComments(Set<CComment> comments) {
		this.comments = comments;
	}
	
	@Override
	public void setLinks(Set<CLink> links) {
		this.links = links;
	}
	
	public void setBitrate(Integer bitrate) {
		this.bitrate = bitrate;
		updateLastModified();
	}
	
	public void setSamplePoint(Double samplePoint) {
		this.samplePoint = samplePoint;
		updateLastModified();
	}
	
	public void setListenOnly(Boolean listenOnly) {
		this.listenOnly = listenOnly;
		updateLastModified();
	}
	
	public void setLoopbackMode(Boolean loopbackMode) {
		this.loopbackMode = loopbackMode;
		updateLastModified();
	}
	
	public void setTripleSampling(Boolean tripleSampling) {
		this.tripleSampling = tripleSampling;
		updateLastModified();
	}
	
	public void setErrorWarningLimit(Integer errorWarningLimit) {
		this.errorWarningLimit = errorWarningLimit;
		updateLastModified();
	}
	
	/**
	 * Check if this CAN node is configured for high-speed CAN.
	 * @return true if bitrate >= 500000
	 */
	public boolean isHighSpeedCan() {
		return bitrate != null && bitrate >= 500000;
	}
}
