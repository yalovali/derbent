package tech.derbent.api.workflow.domain;

import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.companies.domain.CCompany;

public abstract class CWorkflowBase<EntityClass extends CWorkflowBase<?>> extends CEntityOfCompany<EntityClass> {

	/** Default constructor for JPA. */
	protected CWorkflowBase() {
		super();
	}

	/** Constructor with required fields.
	 * @param clazz   the entity class
	 * @param name    the name of the workflow
	 * @param company the company this workflow belongs to */
	protected CWorkflowBase(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name, company);
	}
}
