package tech.derbent.app.orders.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CStatus;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "corderstatus")
@AttributeOverride (name = "id", column = @Column (name = "order_status_id"))
public class COrderStatus extends CStatus<COrderStatus> {

	public static final String DEFAULT_COLOR = "#28a745";
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String VIEW_NAME = "Order Status View";

	/** Constructor with name.
	 * @param name the name of the order status */
	public COrderStatus(final String name, final CProject project) {
		super(COrderStatus.class, name, project);
		this.setColor(DEFAULT_COLOR);
	}

	public COrderStatus() {
		super();
		this.setColor(DEFAULT_COLOR);
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships from parent class (CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}
}
