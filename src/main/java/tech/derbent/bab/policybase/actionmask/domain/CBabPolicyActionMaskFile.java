package tech.derbent.bab.policybase.actionmask.domain;

import java.util.List;
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
import tech.derbent.bab.policybase.actionmask.service.CBabPolicyActionMaskFileService;
import tech.derbent.bab.policybase.actionmask.service.CPageServiceBabPolicyActionMaskFile;
import tech.derbent.bab.policybase.node.file.CBabFileOutputNode;

/** File-output-specific action mask. */
@Entity
@Table (name = "cbab_policy_action_mask_file")
@DiscriminatorValue ("FILE")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public final class CBabPolicyActionMaskFile extends CBabPolicyActionMaskBase<CBabPolicyActionMaskFile> {

	public static final String DEFAULT_COLOR = "#8BC34A";
	public static final String DEFAULT_ICON = "vaadin:file-text";
	public static final String ENTITY_TITLE_PLURAL = "File Action Masks";
	public static final String ENTITY_TITLE_SINGULAR = "File Action Mask";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyActionMaskFile.class);
	public static final String MASK_KIND = "FILE";
	public static final String VIEW_NAME = "File Action Masks View";

	@Column (name = "output_file_pattern", length = 255, nullable = false)
	@AMetaData (
			displayName = "Output File Pattern", required = false, readOnly = false,
			description = "Destination file pattern used by this mask", hidden = false, maxLength = 255
	)
	private String outputFilePattern = "action_*.json";

	@Column (name = "serialization_mode", length = 40, nullable = false)
	@AMetaData (
			displayName = "Serialization Mode", required = false, readOnly = false,
			description = "Mask serialization mode for destination file", hidden = false, maxLength = 40,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfSerializationMode"
	)
	private String serializationMode = "JSON_APPEND";

	protected CBabPolicyActionMaskFile() {}

	public CBabPolicyActionMaskFile(final String name, final CBabFileOutputNode parentNode) {
		super(CBabPolicyActionMaskFile.class, name, parentNode);
		initializeDefaults();
	}

	@Override
	public Class<CBabFileOutputNode> getAllowedNodeType() { return CBabFileOutputNode.class; }

	@Override
	public String getMaskKind() { return MASK_KIND; }

	public String getOutputFilePattern() { return outputFilePattern; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyActionMaskFile.class; }

	public List<String> getSerializationModes() {
		return List.of("JSON_APPEND", "CSV_APPEND", "ROLLING_ARCHIVE");
	}

	public String getSerializationMode() { return serializationMode; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyActionMaskFileService.class; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setOutputFilePattern(final String outputFilePattern) {
		this.outputFilePattern = outputFilePattern;
		updateLastModified();
	}

	public void setSerializationMode(final String serializationMode) {
		this.serializationMode = serializationMode;
		updateLastModified();
	}
}
