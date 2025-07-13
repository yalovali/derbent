package tech.derbent.taskmanagement.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.taskmanagement.domain.Task;
import tech.derbent.taskmanagement.domain.TaskRepository;

//a service for managing tasks in a task management application
// This service allows creating tasks with a description and an optional due date,
// and listing tasks with pagination support.
//service annotation indicates that this class is a Spring service component,
// and the PreAuthorize annotation ensures that only authenticated users can access its methods.
//a service class is created when you need to encapsulate business logic related to a specific domain entity or functionality.
/*
 * TaskService.java
 * This service class provides methods to create and list tasks.
 * It uses a TaskRepository to interact with the database.
 * The createTask method allows adding a new task with a description and an optional due date.
 * The list method retrieves all tasks with pagination support.
 */
@Service // TaskService.java
@PreAuthorize("isAuthenticated()") // Ensures that only authenticated users can access the methods of this service
public class TaskService extends CAbstractService {

	private final TaskRepository taskRepository;

	TaskService(final TaskRepository taskRepository, final Clock clock) {
		super(clock);
		this.taskRepository = taskRepository;
	}

	@Transactional
	public void createTask(final String description, @Nullable final LocalDate dueDate) {
		if ("fail".equals(description)) {
			throw new RuntimeException("This is for testing the error handler");
		}
		final var task = new Task();
		task.setDescription(description);
		task.setCreationDate(clock.instant());
		task.setDueDate(dueDate);
		taskRepository.saveAndFlush(task);
	}

	@Transactional(readOnly = true)
	public List<Task> list(final Pageable pageable) {
		return taskRepository.findAllBy(pageable).toList();
	}
}
