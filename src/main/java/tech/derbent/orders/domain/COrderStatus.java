package tech.derbent.orders.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.base.domain.CStatus;
import tech.derbent.projects.domain.CProject;

/**
 * COrderStatus - Domain entity representing status states of orders in the system. Layer:
 * Domain (MVC) Defines the various status states an order can have during its lifecycle,
 * such as Draft, Submitted, Approved, In Progress, Completed, Cancelled, etc. This entity
 * follows the standard CEntityNamed pattern providing automatic name and description
 * fields with MetaData annotation support.
 */
@Entity
@Table (name = "corderstatus")
@AttributeOverride (name = "id", column = @Column (name = "order_status_id"))
public class COrderStatus extends CStatus<COrderStatus> {

	/**
	 * Constructor with name.
	 * @param name the name of the order status
	 */
	public COrderStatus(final String name, final CProject project) {
		super(COrderStatus.class, name, project);
	}
}