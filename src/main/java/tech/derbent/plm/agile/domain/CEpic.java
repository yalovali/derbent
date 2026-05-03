package tech.derbent.plm.agile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.projects.domain.CProject;

/**
 * Top-level agile planning item.
 *
 * <p>The only field declared here is the typed entity reference because the rest of the planning,
 * sprint, hierarchy, and collaboration state already lives in {@link CAgileEntity}.</p>
 */
@Entity
@Table (name = "cepic")
@PrimaryKeyJoinColumn (name = "epic_id")
@DiscriminatorValue ("EPIC")
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
	}

	public CEpic(final String name, final CProject<?> project) {
		super(CEpic.class, name, project);
	}

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	@Override
	public CEpicType getTypedEntityType() { return entityType; }

	@Override
	protected void setTypedEntityType(final CEpicType entityType) {
		this.entityType = entityType;
		updateLastModified();
	}
}
