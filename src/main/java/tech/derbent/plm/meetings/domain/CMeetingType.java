package tech.derbent.plm.meetings.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

/**
 * Meeting types are leaf execution categories in the generic hierarchy.
 *
 * <p>Meetings can be attached under planning anchors such as user stories or deliverables, but they do
 * not accept further hierarchy children by default.</p>
 */
@Entity
@Table (name = "cmeetingtype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cmeetingtype_id"))
public class CMeetingType extends CTypeEntity<CMeetingType> {

	public static final String DEFAULT_COLOR = "#17a2b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Meeting Types";
	public static final String ENTITY_TITLE_SINGULAR = "Meeting Type";
	public static final String VIEW_NAME = "Meeting Types View";

	/** Default constructor for JPA. */
	/** Default constructor for JPA. */
	protected CMeetingType() {
		super();
	}

	public CMeetingType(final String name, final CCompany company) {
		super(CMeetingType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		setLevel(-1);
		setCanHaveChildren(false);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
