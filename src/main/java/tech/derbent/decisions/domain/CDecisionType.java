package tech.derbent.decisions.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionType - Domain entity representing decision categorization types. Provides classification for project
 * decisions to support decision tracking and analysis. Layer: Domain (MVC) Standard decision types: STRATEGIC,
 * TACTICAL, OPERATIONAL, TECHNICAL, BUDGET
 * 
 * @author Derbent Team
 * @since 1.0
 */
@Entity
@Table(name = "cdecisiontype")
@AttributeOverride(name = "id", column = @Column(name = "cdecisiontype_id"))
public class CDecisionType extends CTypeEntity<CDecisionType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionType.class);

    @Column(name = "requires_approval", nullable = false)
    @NotNull
    @MetaData(displayName = "Requires Approval", required = true, readOnly = false, defaultValue = "false", description = "Whether decisions of this type require approval to proceed", hidden = false, order = 7)
    private Boolean requiresApproval = false;

    /**
     * Default constructor for JPA.
     */
    public CDecisionType() {
        super();
        // Initialize with default values for JPA
        this.requiresApproval = false;
    }

    /**
     * Constructor with required fields only.
     * 
     * @param name
     *            the name of the decision type (e.g., "STRATEGIC", "TECHNICAL")
     * @param project
     *            the project this decision type belongs to
     */
    public CDecisionType(final String name, final CProject project) {
        super(CDecisionType.class, name, project);
    }

    /**
     * Constructor with all fields including requires approval.
     * 
     * @param name
     *            the name of the decision type
     * @param project
     *            the project this decision type belongs to
     * @param color
     *            the hex color code for UI display
     * @param sortOrder
     *            the display order
     * @param requiresApproval
     *            whether decisions of this type require approval
     */
    public CDecisionType(final String name, final CProject project, final String color, final Integer sortOrder,
            final Boolean requiresApproval) {
        super(CDecisionType.class, name, project);
        LOGGER.debug(
                "CDecisionType constructor called with name: {}, project: {}, color: {}, sortOrder: {}, requiresApproval: {}",
                name, project, color, sortOrder, requiresApproval);
        setColor(color);
        setSortOrder(sortOrder);
        this.requiresApproval = requiresApproval;
    }

    /**
     * Gets the requires approval flag.
     * 
     * @return true if decisions of this type require approval
     */
    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    /**
     * Convenience method to check if this decision type requires approval.
     * 
     * @return true if approval is required, false otherwise
     */
    public boolean requiresApproval() {
        return Boolean.TRUE.equals(requiresApproval);
    }

    /**
     * Sets the requires approval flag.
     * 
     * @param requiresApproval
     *            whether decisions of this type require approval
     */
    public void setRequiresApproval(final Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    @Override
    public String toString() {
        return String.format(
                "CDecisionType{id=%d, name='%s', color='%s', sortOrder=%d, isActive=%s, requiresApproval=%s, project=%s}",
                getId(), getName(), getColor(), getSortOrder(), getIsActive(), requiresApproval,
                getProject() != null ? getProject().getName() : "null");
    }
}