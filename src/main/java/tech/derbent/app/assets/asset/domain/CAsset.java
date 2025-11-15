package tech.derbent.app.assets.asset.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.assets.assettype.domain.CAssetType;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"casset\"")
@AttributeOverride (name = "id", column = @Column (name = "asset_id"))
public class CAsset extends CProjectItem<CAsset> implements IHasStatusAndWorkflow<CAsset> {

	public static final String DEFAULT_COLOR = "#FF6B6B";
	public static final String DEFAULT_ICON = "vaadin:briefcase";
	public static final String VIEW_NAME = "Asset View";

	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Asset Type", required = false, readOnly = false, 
			description = "Type category of the asset", hidden = false, order = 2,
			dataProviderBean = "CAssetTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CAssetType entityType;

	/** Default constructor for JPA. */
	public CAsset() {
		super();
		initializeDefaults();
	}

	public CAsset(final String name, final CProject project) {
		super(CAsset.class, name, project);
		initializeDefaults();
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CAssetType.class, "Type entity must be an instance of CAssetType");
		entityType = (CAssetType) typeEntity;
		updateLastModified();
	}

	@Override
	public void initializeAllFields() {
		if (getProject() != null) {
			getProject().getName();
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin();
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin();
		}
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}
}
