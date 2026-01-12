package tech.derbent.app.decisions.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.companies.domain.CCompany;

/** CDecisionType - Domain entity representing decision categorization types. Provides classification for project decisions to support decision
 * tracking and analysis. Layer: Domain (MVC) Standard decision types: STRATEGIC, TACTICAL, OPERATIONAL, TECHNICAL, BUDGET
 * @author Derbent Team
 * @since 1.0 */
@Entity
@Table (name = "cdecisiontype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cdecisiontype_id"))
public class CDecisionType extends CTypeEntity<CDecisionType> {

	public static final String DEFAULT_COLOR = "#17a2b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Decision Types";
	public static final String ENTITY_TITLE_SINGULAR = "Decision Type";
	public static final String VIEW_NAME = "Decision Types View";
	@Column (name = "requires_approval", nullable = false)
	@NotNull
	@AMetaData (
			displayName = "Requires Approval", required = true, readOnly = false, defaultValue = "false",
			description = "Whether decisions of this type require approval to proceed", hidden = false
	)
	private Boolean requiresApproval = false;

	public CDecisionType() {
		super();
		requiresApproval = false;
	}

	public CDecisionType(final String name, final CCompany company) {
		super(CDecisionType.class, name, company);
	}

	public Boolean getRequiresApproval() { return requiresApproval; }

	public boolean requiresApproval() {
		return Boolean.TRUE.equals(requiresApproval);
	}

	public void setRequiresApproval(final Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
}
