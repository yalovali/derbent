package tech.derbent.plm.agile.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.projects.domain.CProject;

/**
 * Middle hierarchy agile item that groups user stories under a parent epic.
 *
 * <p>This class deliberately keeps only the typed entity field because the inherited agile base
 * already owns schedule, finance, hierarchy, sprint, and composition state.</p>
 */
@Entity
@Table (name = "cfeature")
@AttributeOverride (name = "id", column = @Column (name = "feature_id"))
public class CFeature extends CAgileEntity<CFeature, CFeatureType> {

	public static final String DEFAULT_COLOR = "#28A745";
	public static final String DEFAULT_ICON = "vaadin:flash";
	public static final String ENTITY_TITLE_PLURAL = "Features";
	public static final String ENTITY_TITLE_SINGULAR = "Feature";
	public static final String VIEW_NAME = "Features View";
	@ManyToOne
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Feature Type", required = false, readOnly = false, description = "Type category of the feature", hidden = false,
			dataProviderBean = "CFeatureTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CFeatureType entityType;

	protected CFeature() {}

	public CFeature(final String name, final CProject<?> project) {
		super(CFeature.class, name, project);
	}

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	@Override
	public CFeatureType getTypedEntityType() { return entityType; }

	@Override
	protected void setTypedEntityType(final CFeatureType entityType) {
		this.entityType = entityType;
		updateLastModified();
	}

}
