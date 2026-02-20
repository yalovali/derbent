package tech.derbent.bab.policybase.filter.domain;

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
import tech.derbent.bab.policybase.node.file.CBabFileInputNode;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterCSVService;
import tech.derbent.bab.policybase.filter.service.CPageServiceBabPolicyFilterCSV;

/** CSV-specific policy filter entity. */
@Entity
@Table (name = "cbab_policy_filter_csv")
@DiscriminatorValue ("CSV")
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public final class CBabPolicyFilterCSV extends CBabPolicyFilterBase<CBabPolicyFilterCSV> {

	public static final String COLUMN_SEPARATOR_COMMA = ",";
	public static final String COLUMN_SEPARATOR_SEMICOLON = ";";
	public static final String DEFAULT_CAPTURE_COLUMN_RANGE = "1";
	public static final String DEFAULT_COLOR = "#9C27B0";
	public static final String DEFAULT_ICON = "vaadin:file-table";
	public static final String DEFAULT_LINE_REGULAR_EXPRESSION = ".*";
	public static final String ENTITY_TITLE_PLURAL = "CSV Policy Filters";
	public static final String ENTITY_TITLE_SINGULAR = "CSV Policy Filter";
	public static final String FILTER_KIND = "CSV";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicyFilterCSV.class);
	public static final String VIEW_NAME = "CSV Policy Filters View";

	@Column (name = "capture_column_range", length = 20)
	@AMetaData (
			displayName = "Capture Column Range", required = false, readOnly = false,
			description = "CSV capture column range in format N or N-M (for example: 3 or 3-6)", hidden = false, maxLength = 20
	)
	private String captureColumnRange = DEFAULT_CAPTURE_COLUMN_RANGE;

	@Column (name = "column_separator", length = 1)
	@AMetaData (
			displayName = "Column Separator", required = false, readOnly = false,
			description = "Separator character used to split columns (; or ,)", hidden = false, maxLength = 1,
			dataProviderBean = "pageservice", dataProviderMethod = "getComboValuesOfColumnSeparator"
	)
	private String columnSeparator = COLUMN_SEPARATOR_COMMA;

	@Column (name = "line_regular_expression", length = 255)
	@AMetaData (
			displayName = "Line Regular Expression", required = false, readOnly = false,
			description = "Regular expression that each CSV line must match", hidden = false, maxLength = 255
	)
	private String lineRegularExpression = DEFAULT_LINE_REGULAR_EXPRESSION;

	/** Default constructor for JPA. */
	protected CBabPolicyFilterCSV() {
		// JPA constructor must not initialize business defaults.
	}

	public CBabPolicyFilterCSV(final String name, final CBabFileInputNode parentNode) {
		super(CBabPolicyFilterCSV.class, name, parentNode);
		initializeDefaults();
	}

	public String getCaptureColumnRange() {
		return captureColumnRange != null && !captureColumnRange.isBlank() ? captureColumnRange : DEFAULT_CAPTURE_COLUMN_RANGE;
	}

	public String getColumnSeparator() {
		return COLUMN_SEPARATOR_SEMICOLON.equals(columnSeparator) ? COLUMN_SEPARATOR_SEMICOLON : COLUMN_SEPARATOR_COMMA;
	}

	@Override
	public String getFilterKind() { return FILTER_KIND; }

	@Override
	public Class<CBabFileInputNode> getAllowedNodeType() { return CBabFileInputNode.class; }

	public String getLineRegularExpression() {
		return lineRegularExpression != null && !lineRegularExpression.isBlank() ? lineRegularExpression : DEFAULT_LINE_REGULAR_EXPRESSION;
	}

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilterCSV.class; }

	@Override
	public Class<?> getServiceClass() { return CBabPolicyFilterCSVService.class; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setCaptureColumnRange(final String captureColumnRange) {
		this.captureColumnRange = captureColumnRange == null || captureColumnRange.isBlank() ? DEFAULT_CAPTURE_COLUMN_RANGE : captureColumnRange.trim();
		updateLastModified();
	}

	public void setColumnSeparator(final String columnSeparator) {
		this.columnSeparator = COLUMN_SEPARATOR_SEMICOLON.equals(columnSeparator) ? COLUMN_SEPARATOR_SEMICOLON : COLUMN_SEPARATOR_COMMA;
		updateLastModified();
	}

	public void setLineRegularExpression(final String lineRegularExpression) {
		this.lineRegularExpression =
				lineRegularExpression == null || lineRegularExpression.isBlank() ? DEFAULT_LINE_REGULAR_EXPRESSION : lineRegularExpression.trim();
		updateLastModified();
	}
}
