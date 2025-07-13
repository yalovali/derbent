package tech.derbent.taskmanagement.domain;

import java.time.Instant;
import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.base.domain.AbstractEntity;

@Entity
@Table(name = "task")
public class Task extends AbstractEntity<Long> {

	public static final int DESCRIPTION_MAX_LENGTH = 255;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "task_id")
	private Long id;
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

	@Override
	public @Nullable Long getId() { return id; }

	public void setCreationDate(final Instant creationDate) { this.creationDate = creationDate; }

	public void setDescription(final String description) { this.description = description; }

	public void setDueDate(@Nullable final LocalDate dueDate) { this.dueDate = dueDate; }
}
