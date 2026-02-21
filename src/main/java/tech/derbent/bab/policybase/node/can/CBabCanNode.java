package tech.derbent.bab.policybase.node.can;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.annotation.JsonFilter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Basic;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;
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
				"project_id", "physical_interface"
		})
})
@DiscriminatorValue ("CAN_BUS")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabCanNode extends CBabNodeEntity<CBabCanNode> {

	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#FF9800"; // Orange - CAN bus
	public static final String DEFAULT_ICON = "vaadin:car";
	public static final String ENTITY_TITLE_PLURAL = "CAN Bus Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "CAN Bus Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabCanNode.class);
	public static final String VIEW_NAME = "CAN Bus Nodes View";
	private static final Map<String, Set<String>> EXCLUDED_FIELDS_BAB_POLICY = createExcludedFieldMap_BabPolicy();
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "can_node_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this CAN node", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// CAN bus specific fields
	@Column (name = "bitrate", nullable = false)
	@AMetaData (
			displayName = "Bitrate (bps)", required = true, readOnly = false, description = "CAN bus bitrate (e.g., 250000, 500000, 1000000)",
			hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfBitrate"
	)
	private Integer bitrate = 500000;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "can_node_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this CAN node", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "error_warning_limit", nullable = false)
	@AMetaData (
			displayName = "Error Warning Limit", required = false, readOnly = false, description = "CAN error warning limit (default: 96)",
			hidden = false
	)
	private Integer errorWarningLimit = 96;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "can_node_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this CAN node", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Transient
	@AMetaData (
			displayName = "Protocol File Upload", required = false, readOnly = false,
			description = "Upload protocol file content into protocolFileData and parse to protocolFileJson", hidden = false,
			dataProviderBean = "pageservice", createComponentMethod = "createComponentProtocolFileData", captionVisible = false
	)
	private CBabCanNode placeHolder_createComponentProtocolFileData = null;
	@Basic (fetch = FetchType.LAZY)
	@Column (name = "protocol_file_data", columnDefinition = "text")
	@AMetaData (
			displayName = "Protocol File Data", required = false, readOnly = true,
			description = "Raw protocol definition file content, lazily loaded on demand", hidden = true
	)
	private String protocolFileData;
	@Basic (fetch = FetchType.LAZY)
	@Column (name = "protocol_file_json", columnDefinition = "text")
	@AMetaData (
			displayName = "Protocol File JSON", required = false, readOnly = true,
			description = "Parsed protocol definition JSON, lazily loaded on demand", hidden = true
	)
	private String protocolFileJson;
	@Column (name = "protocol_file_summary_json", columnDefinition = "text")
	@AMetaData (
			displayName = "Protocol File Summary JSON", required = false, readOnly = true,
			description = "Persisted protocol file summary status JSON", hidden = true
	)
	private String protocolFileSummaryJson = "{\"status\":\"NO_FILE\",\"message\":\"No protocol file is loaded.\",\"loadedEntityCount\":0}";
	@Column (name = "protocol_type", length = 50)
	@AMetaData (
			displayName = "Protocol Type", required = false, readOnly = false, description = "CAN bus protocol type (XCP, UDS)", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfProtocolType", setBackgroundFromColor = true, useIcon = true
	)
	private String protocolType;

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

	public void clearProtocolFileCache() {
		protocolFileData = null;
		protocolFileJson = null;
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
	public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
		return mergeExcludedFieldMaps(super.getExcludedFieldMapForScenario(scenario),
				getScenarioExcludedFieldMap(scenario, Map.of(), EXCLUDED_FIELDS_BAB_POLICY));
	}

	@Override
	public Set<CLink> getLinks() { return links; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	public CBabCanNode getPlaceHolder_createComponentProtocolFileData() { return this; }

	public String getProtocolFileData() { return protocolFileData; }

	public String getProtocolFileJson() { return protocolFileJson; }

	public String getProtocolFileSummaryJson() { return protocolFileSummaryJson; }

	public String getProtocolType() { return protocolType; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults
		bitrate = 1000000;
		errorWarningLimit = 96;
		if ((getPhysicalInterface() == null) || getPhysicalInterface().isEmpty()) {
			setPhysicalInterface("can0");
		}
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

	/** Setter for transient placeholder field - required by binder, value is ignored because getter always returns this entity. */
	public void setPlaceHolder_createComponentProtocolFileData(final CBabCanNode value) {
		placeHolder_createComponentProtocolFileData = value;
	}

	public void setProtocolFileData(final String protocolFileData) { this.protocolFileData = protocolFileData; }

	public void setProtocolFileJson(final String protocolFileJson) { this.protocolFileJson = protocolFileJson; }

	public void setProtocolFileSummaryJson(final String protocolFileSummaryJson) {
		this.protocolFileSummaryJson = protocolFileSummaryJson;
	}

	public void setProtocolType(final String protocolType) {
		this.protocolType = protocolType;
		updateLastModified();
	}

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		final Map<String, Set<String>> map = new java.util.HashMap<>();
		map.put("CBabCanNode", Set.of("nodeConfigJson", "connectionStatus", "protocolFileSummaryJson", "protocolFileJson", "protocolFileData",
				"placeHolder_createComponentProtocolFileData", "bitrate"));
		return Map.copyOf(map);
	}
}
