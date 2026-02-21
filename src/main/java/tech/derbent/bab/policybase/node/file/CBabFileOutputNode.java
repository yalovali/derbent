package tech.derbent.bab.policybase.node.file;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.links.domain.CLink;

/** CBabFileOutputNode - File Output virtual network node entity for file sink/export operations. Layer: Domain (MVC) Active when: 'bab' profile is
 * active Following Derbent pattern: Concrete entity with @Entity annotation. JPA Inheritance: JOINED strategy with @DiscriminatorValue - Inherits
 * common fields from cbab_node table - Stores file-specific fields in cnode_file_output table - node_type discriminator = "FILE_OUTPUT"
 * Represents file output virtual nodes mapped to file system paths. Example: fileOutput mapped to target file system for export/archiving. Used in
 * BAB Actions Dashboard policy rule engine for file-based data routing and sink management in IoT gateway scenarios. */
@Entity
@Table (name = "cnode_file_output", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"project_id", "name"
		}), @UniqueConstraint (columnNames = {
				"project_id", "file_path"
		})
})
@DiscriminatorValue ("FILE_OUTPUT")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public class CBabFileOutputNode extends CBabNodeEntity<CBabFileOutputNode> {

	// Entity constants (MANDATORY - overriding base class constants)
	public static final String DEFAULT_COLOR = "#3F51B5"; // Indigo - File sink/output
	public static final String DEFAULT_ICON = "vaadin:download-alt";
	public static final String ENTITY_TITLE_PLURAL = "File Output Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "File Output Node";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabFileOutputNode.class);
	public static final String VIEW_NAME = "File Output Nodes View";
	private static final Map<String, Set<String>> EXCLUDED_FIELDS_BAB_POLICY = createExcludedFieldMap_BabPolicy();
	// Standard composition fields - initialized at declaration (RULE 5)
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "file_output_node_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this file output node",
			hidden = false, dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "file_output_node_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this file output node", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponentComment"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (name = "file_format", length = 20, nullable = false)
	@AMetaData (
			displayName = "File Format", required = true, readOnly = false, description = "Target file format (JSON, XML, CSV, TXT, BINARY)",
			hidden = false, maxLength = 20, dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfFileFormat"
	)
	private String fileFormat = "JSON";
	// File output specific fields
	@Column (name = "file_path", length = 500, nullable = false)
	@AMetaData (
			displayName = "File Path", required = true, readOnly = false, description = "File system path to write output data",
			hidden = false, maxLength = 500
	)
	private String filePath = "/var/data/output";
	@Column (name = "file_pattern", length = 100)
	@AMetaData (
			displayName = "File Pattern", required = false, readOnly = false,
			description = "Output file naming pattern (for example: export_*.csv)", hidden = false, maxLength = 100
	)
	private String filePattern;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "file_output_node_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related links for this file output node", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();
	@Column (name = "max_file_size_mb", nullable = false)
	@AMetaData (
			displayName = "Max File Size (MB)", required = false, readOnly = false,
			description = "Maximum output file size to write (in megabytes)", hidden = false
	)
	private Integer maxFileSizeMb = 10;

	/** Default constructor for JPA. */
	protected CBabFileOutputNode() {
		// JPA constructors do NOT call initializeDefaults() (RULE 1)
	}

	public CBabFileOutputNode(final String name, final CProject<?> project) {
		super(CBabFileOutputNode.class, name, project);
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	public CBabFileOutputNode(final String name, final CProject<?> project, final String filePath) {
		super(CBabFileOutputNode.class, name, project);
		this.filePath = filePath;
		initializeDefaults(); // Business constructors MUST call this (RULE 2)
	}

	// Interface implementations
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// IHasColor implementation
	@Override
	public String getColor() {
		return DEFAULT_COLOR; // File outputs are indigo
	}

	@Override
	public Set<CComment> getComments() { return comments; }

	/** Get the effective file pattern for output naming.
	 * @return file pattern or default wildcard */
	public String getEffectiveFilePattern() { return hasFilePattern() ? filePattern : "*"; }

	public String getEntityColor() { return DEFAULT_COLOR; }

	public String getFileFormat() { return fileFormat; }

	// File output specific getters and setters
	public String getFilePath() { return filePath; }

	@Override
	public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
		return mergeExcludedFieldMaps(super.getExcludedFieldMapForScenario(scenario),
				getScenarioExcludedFieldMap(scenario, Map.of(), EXCLUDED_FIELDS_BAB_POLICY));
	}

	public String getFilePattern() { return filePattern; }

	@Override
	public Set<CLink> getLinks() { return links; }

	public Integer getMaxFileSizeMb() { return maxFileSizeMb; }

	@Override
	public Class<?> getPageServiceClass() { return Object.class; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getServiceClass() { return Object.class; }

	/** Check if file pattern is defined for output file naming.
	 * @return true if file pattern is configured */
	public boolean hasFilePattern() {
		return (filePattern != null) && !filePattern.trim().isEmpty();
	}

	/** Initialize intrinsic defaults (RULE 3). */
	private final void initializeDefaults() {
		if ((getPhysicalInterface() == null) || getPhysicalInterface().isEmpty()) {
			setPhysicalInterface("file");
		}
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
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

	public void setFileFormat(final String fileFormat) {
		this.fileFormat = fileFormat;
		updateLastModified();
	}

	public void setFilePath(final String filePath) {
		this.filePath = filePath;
		updateLastModified();
	}

	public void setFilePattern(final String filePattern) {
		this.filePattern = filePattern;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }

	public void setMaxFileSizeMb(final Integer maxFileSizeMb) {
		this.maxFileSizeMb = maxFileSizeMb;
		updateLastModified();
	}

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		final Map<String, Set<String>> map = new java.util.HashMap<>();
		map.put("CBabFileOutputNode", Set.of("filePattern", "maxFileSizeMb"));
		return Map.copyOf(map);
	}
}
