package tech.derbent.orders.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

/**
 * COrderType - Domain entity representing types of orders in the system. Layer: Domain (MVC) Defines the various
 * categories of orders that can be created in the system, such as Service Orders, Purchase Orders, Maintenance Orders,
 * etc. This entity follows the CEntityOfProject pattern providing project-aware functionality with automatic name and
 * description fields.
 */
@Entity
@Table(name = "cordertype")
@AttributeOverride(name = "id", column = @Column(name = "order_type_id"))
public class COrderType extends CEntityOfProject<COrderType> {

    /**
     * Constructor with name and project.
     * 
     * @param name
     *            the name of the order type
     * @param project
     *            the project this type belongs to
     */
    public COrderType(final String name, final CProject project) {
        super(COrderType.class, name, project);
    }
}