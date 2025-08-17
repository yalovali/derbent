package tech.derbent.meetings.domain;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.annotations.StatusEntity;
import tech.derbent.abstracts.interfaces.CKanbanStatus;
import tech.derbent.base.domain.CStatus;
import tech.derbent.projects.domain.CProject;

/**
 * CMeetingStatus - Domain entity representing meeting status types. Layer: Domain (MVC) Inherits from CStatus to
 * provide status functionality for meetings. This entity defines the possible statuses a meeting can have (e.g.,
 * PLANNED, IN_PROGRESS, COMPLETED, CANCELLED).
 */
@StatusEntity(category = "meeting", colorField = "color", nameField = "name")
@Entity
@Table(name = "cmeetingstatus")
@AttributeOverride(name = "id", column = @Column(name = "cmeetingstatus_id"))
public class CMeetingStatus extends CStatus<CMeetingStatus> implements CKanbanStatus {

    @Column(name = "is_final", nullable = false)
    @MetaData(displayName = "Is Final Status", required = true, readOnly = false, defaultValue = "false", description = "Indicates if this is a final status (completed/cancelled)", hidden = false, order = 4)
    private Boolean finalStatus = false;

    /**
     * Default constructor for JPA.
     */
    public CMeetingStatus() {
        super();
        // Initialize with default values for JPA
        this.finalStatus = Boolean.FALSE;
    }

    public CMeetingStatus(final String name, final CProject project) {
        super(CMeetingStatus.class, name, project);
    }

    public CMeetingStatus(final String name, final CProject project, final String description, final String color,
            final Boolean finalStatus) {
        super(CMeetingStatus.class, name, project);
        setDescription(description);
        setColor(color != null ? color : "#808080");
        this.finalStatus = finalStatus;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof CMeetingStatus)) {
            return false;
        }
        final CMeetingStatus that = (CMeetingStatus) o;
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), finalStatus);
    }

    public Boolean getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(final Boolean finalStatus) {
        this.finalStatus = finalStatus;
    }

    @Override
    public String toString() {
        return getName() != null ? getName() : super.toString();
    }

    public static String getIconColorCode() {
        return "#28a745"; // Green color for meeting status entities
    }

    public static String getIconFilename() {
        return "vaadin:flag";
    }
}