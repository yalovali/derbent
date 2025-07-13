package tech.derbent.taskmanagement.ui.view;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import tech.derbent.base.ui.component.ViewToolbar;
import tech.derbent.taskmanagement.domain.Task;
import tech.derbent.taskmanagement.service.TaskService;
import views.CAbstractPage;

@Route("task-list")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
@PermitAll // When security is enabled, allow all authenticated users
public class TaskListView extends CAbstractPage {

	private static final long serialVersionUID = 1L;
	private final TaskService taskService;
	TextField description;
	DatePicker dueDate;
	Button createBtn;
	Grid<Task> taskGrid;
	Clock clock;

	/**
	 * Constructs a new TaskListView.
	 * @param taskService the service to manage tasks
	 * @param clock       the clock to use for date and time operations
	 */
	public TaskListView(final TaskService taskService, final Clock clock) {
		super();
		this.clock = clock;
		this.taskService = taskService;
		taskGrid = new Grid<>();
		taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
		taskGrid.addColumn(Task::getDescription).setHeader("Description");
		final var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());
		final var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(clock.getZone()).withLocale(getLocale());
		taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Never")).setHeader("Due Date");
		taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Creation Date");
		taskGrid.setSizeFull();
		add(taskGrid);
	}

	private void createTask() {
		taskService.createTask(description.getValue(), dueDate.getValue());
		taskGrid.getDataProvider().refreshAll();
		description.clear();
		dueDate.clear();
		Notification.show("Task added", 3000, Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	}

	@Override
	protected void initPage() {
		// Initialize the page components and layout
		setSizeFull();
		addClassNames("task-list-view");
	}

	@Override
	protected void setupToolbar() {
		description = new TextField();
		description.setPlaceholder("What do you want to do?");
		description.setAriaLabel("Task description");
		description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
		description.setMinWidth("20em");
		dueDate = new DatePicker();
		dueDate.setPlaceholder("Due date");
		dueDate.setAriaLabel("Due date");
		createBtn = new Button("Create", event -> createTask());
		createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn)));
	}
}
