package tech.derbent.plm.agile.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.interfaces.IHasEpicParent;
import tech.derbent.api.projects.domain.CProject;

@Entity
@Table (name = "cfeature")
@AttributeOverride (name = "id", column = @Column (name = "feature_id"))
public class CFeature extends CAgileEntity<CFeature, CFeatureType> implements IHasEpicParent {

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

	protected CFeature() {
		super();
	}

	public CFeature(final String name, final CProject<?> project) {
		super(CFeature.class, name, project);
		initializeDefaults();
	}

	public CFeatureType getTypedEntityType() { return entityType; }
	
	protected void setTypedEntityType(final CFeatureType entityType) {
		this.entityType = entityType;
		updateLastModified();
	}

	public String getColor() { return DEFAULT_COLOR; }

	public String getIconString() { return DEFAULT_ICON; }

	public CFeatureType getEntityTypeFeature() { return entityType; }

	private final void initializeDefaults() {
		// Feature-specific initialization can be added here if needed
		// Parent CAgileEntity.initializeDefaults() is called by parent constructor
		// This method is for Feature-specific field initialization only
	}
}
