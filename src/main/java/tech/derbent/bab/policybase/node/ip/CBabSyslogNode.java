package tech.derbent.bab.policybase.node.ip;

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

/** CBabSyslogNode - Syslog server virtual network node entity. Layer: Domain (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete entity with @Entity annotation. JPA Inheritance: JOINED strategy with @DiscriminatorValue - Inherits common fields from cbab_node table -
 * Stores Syslog-specific fields in cnode_syslog table - node_type discriminator = "SYSLOG" Represents Syslog receiver nodes for system logging and
 * monitoring. Example: UDP/TCP syslog servers for centralized log collection. Used in BAB Actions Dashboard policy rule engine for log traffic
 * management. */
@Entity
@Table (name = "cnode_syslog", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		}), @UniqueConstraint (columnNames = {
				"project_id", "physical_interface", "listen_port"
		})
})
@DiscriminatorValue ("SYSLOG")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabSyslogNode extends CBabNodeEntity<CBabSyslogNode> {
	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#607D8B"; // Blue-grey - Syslog
	public static final String DEFAULT_ICON = "vaadin:file-text-o";
	public static final String ENTITY_TITLE_PLURAL = "Syslog Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "Syslog Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabSyslogNode.class);
	public static final String VIEW_NAME = "Syslog Nodes View";
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "syslog_node_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this Syslog node", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "syslog_node_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this Syslog node", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "syslog_node_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this Syslog node", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	// Syslog specific fields
	@Column (name = "listen_port", nullable = false)
	@AMetaData (
			displayName = "Listen Port", required = true, readOnly = false, description = "Syslog server listening port (default: 514)",
			hidden = false
	)
	private Integer listenPort = 514;
	@Column (name = "protocol", length = 10, nullable = false)
	@AMetaData (
				displayName = "Protocol", required = true, readOnly = false, description = "Transport protocol (UDP or TCP)", hidden = false,
				maxLength = 10, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfProtocolType", setBackgroundFromColor = true,
				useIcon = true
	)
	private String protocol = "UDP";
	@Column (name = "facility", length = 20, nullable = false)
	@AMetaData (
				displayName = "Facility", required = false, readOnly = false, description = "Default syslog facility (LOCAL0-LOCAL7, USER, KERN)",
				hidden = false, maxLength = 20, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfSyslogFacility",
				setBackgroundFromColor = true, useIcon = true
	)
	private String facility = "LOCAL0";
	@Column (name = "severity_level", length = 20, nullable = false)
	@AMetaData (
				displayName = "Min Severity Level", required = false, readOnly = false,
				description = "Minimum severity to log (DEBUG, INFO, NOTICE, WARNING, ERROR, CRIT, ALERT, EMERG)", hidden = false, maxLength = 20,
				dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfSeverityLevel", setBackgroundFromColor = true, useIcon = true
	)
	private String severityLevel = "INFO";
	@Column (name = "log_file_path", length = 255, nullable = false)
	@AMetaData (
			displayName = "Log File Path", required = false, readOnly = false, description = "Path to store syslog messages", hidden = false,
			maxLength = 255
	)
	private String logFilePath = "/var/log/syslog";
	@Column (name = "max_message_size", nullable = false)
	@AMetaData (
			displayName = "Max Message Size (bytes)", required = false, readOnly = false, description = "Maximum syslog message size in bytes",
			hidden = false
	)
	private Integer maxMessageSize = 2048;
	@Column (name = "enable_tls", nullable = false)
	@AMetaData (
			displayName = "Enable TLS", required = false, readOnly = false, description = "Enable TLS encryption for TCP connections", hidden = false
	)
	private Boolean enableTls = false;

	/** Default constructor for JPA. */
	protected CBabSyslogNode() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabSyslogNode(final String name, final CProject<?> project) {
		super(CBabSyslogNode.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public CBabSyslogNode(final String name, final CProject<?> project, final String physicalInterface, final Integer listenPort) {
		super(CBabSyslogNode.class, name, project);
		this.listenPort = listenPort;
		setPhysicalInterface(physicalInterface);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// IHasColor implementation
	@Override
	public String getColor() {
		return DEFAULT_COLOR; // Syslog nodes are blue-grey
	}

	@Override
	public Set<CComment> getComments() { return comments; }

	public Boolean getEnableTls() { return enableTls; }

	public String getEntityColor() { return DEFAULT_COLOR; }

	public String getFacility() { return facility; }

	@Override
	public Set<CLink> getLinks() { return links; }

	// Syslog specific getters and setters
	public Integer getListenPort() { return listenPort; }

	public String getLogFilePath() { return logFilePath; }

	public Integer getMaxMessageSize() { return maxMessageSize; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	public String getProtocol() { return protocol; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	public String getSeverityLevel() { return severityLevel; }

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		// Initialize nullable=false fields with defaults
		if (listenPort == null) {
			listenPort = 514;
		}
		if ((protocol == null) || protocol.isEmpty()) {
			protocol = "UDP";
		}
		if ((facility == null) || facility.isEmpty()) {
			facility = "LOCAL0";
		}
		if ((severityLevel == null) || severityLevel.isEmpty()) {
			severityLevel = "INFO";
		}
		if ((logFilePath == null) || logFilePath.isEmpty()) {
			logFilePath = "/var/log/syslog";
		}
		if (maxMessageSize == null) {
			maxMessageSize = 2048;
		}
		if (enableTls == null) {
			enableTls = false;
		}
		// Set default physical interface if not set
		if ((getPhysicalInterface() == null) || getPhysicalInterface().isEmpty()) {
			setPhysicalInterface("eth0");
		}
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	/** Check if this Syslog node uses secure transport.
	 * @return true if TCP with TLS enabled */
	public boolean isSecureTransport() {
		return "TCP".equalsIgnoreCase(protocol) && (enableTls != null) && enableTls;
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setColor(final String color) {
		// Color is static for node types, determined by node type constant
		// Not configurable per instance for consistency
	}

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	public void setEnableTls(final Boolean enableTls) {
		this.enableTls = enableTls;
		updateLastModified();
	}

	public void setFacility(final String facility) {
		this.facility = facility;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setListenPort(final Integer listenPort) {
		this.listenPort = listenPort;
		updateLastModified();
	}

	public void setLogFilePath(final String logFilePath) {
		this.logFilePath = logFilePath;
		updateLastModified();
	}

	public void setMaxMessageSize(final Integer maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
		updateLastModified();
	}

	public void setProtocol(final String protocol) {
		this.protocol = protocol;
		updateLastModified();
	}

	public void setSeverityLevel(final String severityLevel) {
		this.severityLevel = severityLevel;
		updateLastModified();
	}
}
