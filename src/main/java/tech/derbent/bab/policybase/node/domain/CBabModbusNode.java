package tech.derbent.bab.policybase.node.domain;

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
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.links.domain.CLink;

/** CBabModbusNode - Modbus RTU/ASCII virtual network node entity. Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete entity with @Entity annotation. JPA Inheritance: JOINED strategy with @DiscriminatorValue - Inherits common fields from cbab_node table -
 * Stores Modbus-specific fields in cnode_modbus table - node_type discriminator = "MODBUS" Represents Modbus serial communication nodes mapped to
 * physical serial interfaces. Example: /dev/ttyS0, /dev/ttyUSB0 for industrial device communication. Used in BAB Actions Dashboard policy rule engine
 * for Modbus traffic management. */
@Entity
@Table (name = "cnode_modbus", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		}), @UniqueConstraint (columnNames = {
				"project_id", "physical_interface", "slave_id"
		})
})
@DiscriminatorValue ("MODBUS")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabModbusNode extends CBabNodeEntity<CBabModbusNode> {
	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#9C27B0"; // Purple - Modbus
	public static final String DEFAULT_ICON = "vaadin:plug";
	public static final String ENTITY_TITLE_PLURAL = "Modbus Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "Modbus Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabModbusNode.class);
	public static final String VIEW_NAME = "Modbus Nodes View";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "modbus_node_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this Modbus node", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "modbus_node_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this Modbus node", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "modbus_node_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this Modbus node", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// Modbus specific fields
	@Column (name = "slave_id", nullable = false)
	@AMetaData (displayName = "Slave ID", required = true, readOnly = false, description = "Modbus slave ID (1-247)", hidden = false)
	private Integer slaveId = 1;
	@Column (name = "baudrate", nullable = false)
	@AMetaData (
				displayName = "Baudrate", required = true, readOnly = false, description = "Serial port baudrate (e.g., 9600, 19200, 115200)",
				hidden = false, dataProviderBean = "pageservice", dataProviderMethod = "getAvailableBaudrates"
	)
	private Integer baudrate = 9600;
	@Column (name = "data_bits", nullable = false)
	@AMetaData (
			displayName = "Data Bits", required = false, readOnly = false, description = "Number of data bits (7 or 8)", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getAvailableDataBits"
	)
	private Integer dataBits = 8;
	@Column (name = "stop_bits", nullable = false)
	@AMetaData (
			displayName = "Stop Bits", required = false, readOnly = false, description = "Number of stop bits (1 or 2)", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getAvailableStopBits"
	)
	private Integer stopBits = 1;
	@Column (name = "parity", length = 10, nullable = false)
	@AMetaData (
				displayName = "Parity", required = false, readOnly = false, description = "Parity checking (NONE, EVEN, ODD)", hidden = false,
				maxLength = 10, dataProviderBean = "pageservice", dataProviderMethod = "getAvailableParityTypes"
	)
	private String parity = "NONE";
	@Column (name = "modbus_mode", length = 10, nullable = false)
	@AMetaData (
				displayName = "Modbus Mode", required = true, readOnly = false, description = "Modbus protocol mode (RTU or ASCII)", hidden = false,
				maxLength = 10, dataProviderBean = "pageservice", dataProviderMethod = "getAvailableModbusModes"
	)
	private String modbusMode = "RTU";
	@Column (name = "timeout_ms", nullable = false)
	@AMetaData (
			displayName = "Timeout (ms)", required = false, readOnly = false, description = "Communication timeout in milliseconds", hidden = false,
			dataProviderBean = "pageservice", dataProviderMethod = "getAvailableTimeoutMs"
	)
	private Integer timeoutMs = 1000;

	/** Default constructor for JPA. */
	protected CBabModbusNode() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabModbusNode(final String name, final CProject<?> project) {
		super(CBabModbusNode.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public CBabModbusNode(final String name, final CProject<?> project, final String physicalInterface, final Integer slaveId) {
		super(CBabModbusNode.class, name, project);
		this.slaveId = slaveId;
		setPhysicalInterface(physicalInterface);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	public Integer getBaudrate() { return baudrate; }

	// IHasColor implementation
	@Override
	public String getColor() {
		return DEFAULT_COLOR; // Modbus nodes are purple
	}

	@Override
	public Set<CComment> getComments() { return comments; }

	public Integer getDataBits() { return dataBits; }

	public String getEntityColor() { return DEFAULT_COLOR; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public String getModbusMode() { return modbusMode; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	public String getParity() { return parity; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	// Modbus specific getters and setters
	public Integer getSlaveId() { return slaveId; }

	public Integer getStopBits() { return stopBits; }

	public Integer getTimeoutMs() { return timeoutMs; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults
		if (slaveId == null) {
			slaveId = 1;
		}
		if (baudrate == null) {
			baudrate = 9600;
		}
		if (dataBits == null) {
			dataBits = 8;
		}
		if (stopBits == null) {
			stopBits = 1;
		}
		if ((parity == null) || parity.isEmpty()) {
			parity = "NONE";
		}
		if ((modbusMode == null) || modbusMode.isEmpty()) {
			modbusMode = "RTU";
		}
		if (timeoutMs == null) {
			timeoutMs = 1000;
		}
		// Set default physical interface if not set
		if ((getPhysicalInterface() == null) || getPhysicalInterface().isEmpty()) {
			setPhysicalInterface("/dev/ttyS0");
		}
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if this Modbus node is configured for RTU mode.
	 * @return true if mode is RTU */
	public boolean isRtuMode() { return "RTU".equalsIgnoreCase(modbusMode); }

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	public void setBaudrate(final Integer baudrate) {
		this.baudrate = baudrate;
		updateLastModified();
	}

	@Override
	public void setColor(final String color) {
		// Color is static for node types, determined by node type constant
		// Not configurable per instance for consistency
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setDataBits(final Integer dataBits) {
		this.dataBits = dataBits;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setModbusMode(final String modbusMode) {
		this.modbusMode = modbusMode;
		updateLastModified();
	}

	public void setParity(final String parity) {
		this.parity = parity;
		updateLastModified();
	}

	public void setSlaveId(final Integer slaveId) {
		this.slaveId = slaveId;
		updateLastModified();
	}

	public void setStopBits(final Integer stopBits) {
		this.stopBits = stopBits;
		updateLastModified();
	}

	public void setTimeoutMs(final Integer timeoutMs) {
		this.timeoutMs = timeoutMs;
		updateLastModified();
	}
}
