package tech.derbent.plm.requirements.requirementtype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

/**
 * Requirement types carry the canonical level/canHaveChildren semantics used by generic hierarchy
 * selectors.
 */
@Entity
@Table(name = "crequirementtype", uniqueConstraints = @UniqueConstraint(columnNames = {
		"name", "company_id"
}))
@AttributeOverride(name = "id", column = @Column(name = "crequirementtype_id"))
public class CRequirementType extends CTypeEntity<CRequirementType> {

	public static final String DEFAULT_COLOR = "#7B5EA7";
	public static final String DEFAULT_ICON = "vaadin:clipboard-text";
	public static final String ENTITY_TITLE_PLURAL = "Requirement Types";
	public static final String ENTITY_TITLE_SINGULAR = "Requirement Type";
	public static final String VIEW_NAME = "Requirement Type Management";

	protected CRequirementType() {}

	public CRequirementType(final String name, final CCompany company) {
		super(CRequirementType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		// New requirement types start as leaf items so teams opt in to higher levels explicitly.
		setColor(DEFAULT_COLOR);
		setLevel(-1);
		setCanHaveChildren(false);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
