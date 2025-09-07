package tech.derbent.orders.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cordertype")
@AttributeOverride (name = "id", column = @Column (name = "order_type_id"))
public class COrderType extends CEntityOfProject<COrderType> {

	/** Constructor with name and project.
	 * @param name    the name of the order type
	 * @param project the project this type belongs to */
	public COrderType(final String name, final CProject project) {
		super(COrderType.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}
}
