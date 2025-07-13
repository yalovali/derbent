package tech.derbent.users.view;

import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.views.CAbstractMDPage;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

@Route("users/:user_id?/:action?(edit)")
@PageTitle("User Master Detail")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.Users")
@PermitAll // When security is enabled, allow all authenticated users
public class CUsersView extends CAbstractMDPage {

	private static final long serialVersionUID = 1L;
	private final String SAMPLEPERSON_ID = "user_id";
	private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "users/%s/edit";
	private TextField name;
	private final Button cancel = new Button("Cancel");
	private final Button save = new Button("Save");
	private final BeanValidationBinder<CUser> binder;
	private CUser user;
	private final CUserService userService;
	private final Grid<CUser> grid;// = new Grid<>(CUser.class, false);

	public CUsersView(final CUserService userService) {
		super();
		addClassNames("users-view");
		setSizeFull();
		this.userService = userService;
		addClassNames("users-view");
		// Create UI
		final SplitLayout splitLayout = new SplitLayout();
		grid = new Grid<>(CUser.class, false);
		// Configure Grid
		createGridLayout(splitLayout);
		createEditorLayout(splitLayout);
		add(splitLayout);
		grid.getColumns().forEach(grid::removeColumn);
		// property name must match the field name in CUser
		grid.addColumn("name").setAutoWidth(true);
		grid.setItems(query -> userService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(String.format(SAMPLEPERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
			}
			else {
				clearForm();
				UI.getCurrent().navigate(CUsersView.class);
			}
		});
		// Configure Form
		binder = new BeanValidationBinder<>(CUser.class);
		// Bind fields. This is where you'd define e.g. validation rules
		binder.bindInstanceFields(this);
		cancel.addClickListener(e -> {
			clearForm();
			refreshGrid();
		});
		save.addClickListener(e -> {
			try {
				if (user == null) {
					user = new CUser();
				}
				binder.writeBean(user);
				userService.save(user);
				clearForm();
				refreshGrid();
				Notification.show("Data updated");
				UI.getCurrent().navigate(CUsersView.class);
			} catch (final ObjectOptimisticLockingFailureException exception) {
				final Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
				n.setPosition(Position.MIDDLE);
				n.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} catch (final ValidationException validationException) {
				Notification.show("Failed to update the data. Check again that all values are valid");
			}
		});
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		final Optional<Long> samplePersonId = event.getRouteParameters().get(SAMPLEPERSON_ID).map(Long::parseLong);
		if (samplePersonId.isPresent()) {
			final Optional<CUser> samplePersonFromBackend = userService.get(samplePersonId.get());
			if (samplePersonFromBackend.isPresent()) {
				populateForm(samplePersonFromBackend.get());
			}
			else {
				Notification.show(String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000, Notification.Position.BOTTOM_START);
				// when a row is selected but the data is no longer available, refresh grid
				refreshGrid();
				event.forwardTo(CUsersView.class);
			}
		}
	}

	private void clearForm() {
		populateForm(null);
	}

	private void createButtonLayout(final Div editorLayoutDiv) {
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("button-layout");
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonLayout.add(save, cancel);
		editorLayoutDiv.add(buttonLayout);
	}

	private void createEditorLayout(final SplitLayout splitLayout) {
		final Div editorLayoutDiv = new Div();
		editorLayoutDiv.setClassName("editor-layout");
		final Div editorDiv = new Div();
		editorDiv.setClassName("editor");
		editorLayoutDiv.add(editorDiv);
		final FormLayout formLayout = new FormLayout();
		name = new TextField("First Name");
		// formLayout.add(firstName, lastName, email, phone, dateOfBirth, occupation,
		// role, important);
		formLayout.add(name);
		editorDiv.add(formLayout);
		createButtonLayout(editorLayoutDiv);
		splitLayout.addToSecondary(editorLayoutDiv);
	}

	private void createGridLayout(final SplitLayout splitLayout) {
		final Div wrapper = new Div();
		wrapper.setClassName("grid-wrapper");
		splitLayout.addToPrimary(wrapper);
		wrapper.add(grid);
	}

	@Override
	protected void initPage() {
		// Initialize the page components and layout This method can be overridden to
		// set up the view's components
	}

	private void populateForm(final CUser value) {
		user = value;
		binder.readBean(user);
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getDataProvider().refreshAll();
	}

	@Override
	protected void setupContent() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
