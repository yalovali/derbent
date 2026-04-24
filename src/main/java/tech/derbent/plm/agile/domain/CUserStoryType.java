package tech.derbent.plm.agile.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

/**
 * Default agile leaf type for execution-ready user stories.
 *
 * <p>User stories remain leaf items by default so the hierarchy engine treats them as the bridge to
 * activities, milestones, and other execution artifacts rather than further agile decomposition.</p>
 */
@Entity
@Table (name = "cuserstorytype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cuserstorytype_id"))
public class CUserStoryType extends CTypeEntity<CUserStoryType> {

	public static final String DEFAULT_COLOR = "#1F8EFA";
	public static final String DEFAULT_ICON = "vaadin:comment";
	public static final String ENTITY_TITLE_PLURAL = "User Story Types";
	public static final String ENTITY_TITLE_SINGULAR = "User Story Type";
	public static final String VIEW_NAME = "User Story Type Management";

	protected CUserStoryType() {
		super();
	}

	public CUserStoryType(final String name, final CCompany company) {
		super(CUserStoryType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		setLevel(2); // User stories are deeper than epics/features but still part of the planning ladder.
		setCanHaveChildren(false);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
