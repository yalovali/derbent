package tech.derbent.orders.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.base.domain.CStatus;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "corderstatus")
@AttributeOverride (name = "id", column = @Column (name = "order_status_id"))
public class COrderStatus extends CStatus<COrderStatus> {
	/** Constructor with name.
	 * @param name the name of the order status */
	public COrderStatus(final String name, final CProject project) {
		super(COrderStatus.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}
}
