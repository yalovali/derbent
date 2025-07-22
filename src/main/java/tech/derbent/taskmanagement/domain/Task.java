package tech.derbent.taskmanagement.domain;

import java.time.Instant;
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;

/**
 * Task - Domain entity representing tasks. Layer: Domain (MVC) Inherits from CEntityDB to provide database
 * functionality.
 */
@Entity
@Table(name = "task")
public class Task extends CEntityDB {

    public static final int DESCRIPTION_MAX_LENGTH = 255;

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    @Size(max = DESCRIPTION_MAX_LENGTH)
    @MetaData(displayName = "Description", required = true, readOnly = false, defaultValue = "", description = "Task description", hidden = false, order = 1, maxLength = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(name = "creation_date", nullable = false)
    @MetaData(displayName = "Creation Date", required = true, readOnly = true, description = "Date when the task was created", hidden = false, order = 2)
    private Instant creationDate;

    @Column(name = "due_date")
    @Nullable
    @MetaData(displayName = "Due Date", required = false, readOnly = false, description = "Due date for the task", hidden = false, order = 3)
    private LocalDate dueDate;

    public Instant getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }

    public void setCreationDate(final Instant creationDate) {
        this.creationDate = creationDate;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDueDate(@Nullable final LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
