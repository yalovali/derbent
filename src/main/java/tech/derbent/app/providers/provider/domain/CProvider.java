package tech.derbent.app.providers.provider.domain;

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
import tech.derbent.app.providers.providertype.domain.CProviderType;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cprovider\"")
@AttributeOverride (name = "id", column = @Column (name = "provider_id"))
public class CProvider extends CProjectItem<CProvider> implements IHasStatusAndWorkflow<CProvider> {

	public static final String DEFAULT_COLOR = "#B5B5B5"; // CDE Background Gray - external providers
	public static final String DEFAULT_ICON = "vaadin:handshake";
	public static final String ENTITY_TITLE_PLURAL = "Providers";
	public static final String ENTITY_TITLE_SINGULAR = "Provider";
	public static final String VIEW_NAME = "Provider View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Provider Type", required = false, readOnly = false, description = "Type category of the provider", hidden = false,
			 dataProviderBean = "CProviderTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CProviderType entityType;

	/** Default constructor for JPA. */
	public CProvider() {
		super();
		initializeDefaults();
	}

	public CProvider(final String name, final CProject project) {
		super(CProvider.class, name, project);
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
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CProviderType.class, "Type entity must be an instance of CProviderType");
		entityType = (CProviderType) typeEntity;
		updateLastModified();
	}
}
