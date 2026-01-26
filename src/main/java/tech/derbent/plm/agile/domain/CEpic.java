package tech.derbent.plm.agile.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;

@Entity
@Table (name = "cepic")
@AttributeOverride (name = "id", column = @Column (name = "epic_id"))
public class CEpic extends CAgileEntity<CEpic, CEpicType> {

	public static final String DEFAULT_COLOR = "#6F42C1";
	public static final String DEFAULT_ICON = "vaadin:records";
	public static final String ENTITY_TITLE_PLURAL = "Epics";
	public static final String ENTITY_TITLE_SINGULAR = "Epic";
	public static final String VIEW_NAME = "Epics View";

	@ManyToOne
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Epic Type", required = false, readOnly = false, description = "Type category of the epic", hidden = false,
			dataProviderBean = "CEpicTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CEpicType entityType;

	protected CEpic() {
		super();
	}

	public CEpic(final String name, final CProject<?> project) {
		super(CEpic.class, name, project);
		// initializeDefaults() already called by parent CAgileEntity constructor
	}

	@Override
	protected CEpicType getTypedEntityType() { return entityType; }

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	public CEpicType getEntityTypeEpic() { return entityType; }

	public void setEntityType(final CEpicType entityType) {
		this.entityType = entityType;
		updateLastModified();
	}
}
