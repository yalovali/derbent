package tech.derbent.app.risklevel.risklevel.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "\"crisklevel\"") // Using quoted identifiers for PostgreSQL
@AttributeOverride (name = "id", column = @Column (name = "risklevel_id"))
public class CRiskLevel extends CProjectItem<CRiskLevel> {

	public static final String DEFAULT_COLOR = "#9C27B0";
	public static final String DEFAULT_ICON = "vaadin:chart-3d";
	public static final String ENTITY_TITLE_PLURAL = "Risk Levels";
	public static final String ENTITY_TITLE_SINGULAR = "Risk Level";
	public static final String VIEW_NAME = "Risk Levels View";
	@Column (nullable = true)
	@AMetaData (
			displayName = "Risk Level", required = false, readOnly = false, defaultValue = "1", description = "Numeric risk level indicator (1-10)",
			hidden = false
	)
	private Integer riskLevel;

	/** Default constructor for JPA. */
	public CRiskLevel() {
		super();
		initializeDefaults();
	}

	public CRiskLevel(final String name, final CProject project) {
		super(CRiskLevel.class, name, project);
		initializeDefaults();
	}

	public Integer getRiskLevel() { return riskLevel; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (riskLevel == null) {
			riskLevel = 1;
		}
	}

	public void setRiskLevel(final Integer riskLevel) {
		this.riskLevel = riskLevel;
		updateLastModified();
	}
}
