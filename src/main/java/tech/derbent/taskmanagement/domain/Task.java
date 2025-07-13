package tech.derbent.taskmanagement.domain;

import java.time.Instant;
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.domains.CEntityDB;

@Entity
@Table(name = "task")
@AttributeOverride(name = "id", column = @Column(name = "task_id")) // Override the default column name for the ID field
public class Task extends CEntityDB {

	public static final int DESCRIPTION_MAX_LENGTH = 255;
	@Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
	@Size(max = DESCRIPTION_MAX_LENGTH)
	private String description;
	@Column(name = "creation_date", nullable = false)
	private Instant creationDate;
	@Column(name = "due_date")
	@Nullable
	private LocalDate dueDate;

	public Instant getCreationDate() { return creationDate; }

	public String getDescription() { return description; }

	public @Nullable LocalDate getDueDate() { return dueDate; }

	public void setCreationDate(final Instant creationDate) { this.creationDate = creationDate; }

	public void setDescription(final String description) { this.description = description; }

	public void setDueDate(@Nullable final LocalDate dueDate) { this.dueDate = dueDate; }
}
