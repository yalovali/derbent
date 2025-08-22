package tech.derbent.meetings.view;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CInterfaceIconSet;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.service.CMeetingService;
import tech.derbent.meetings.service.CMeetingViewService;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

@Route ("cmeetingsview/:meeting_id?/:action?(edit)")
@PageTitle ("Meeting Master Detail")
@Menu (order = 1.4, icon = "class:tech.derbent.meetings.view.CMeetingsView", title = "Project.Meetings")
@PermitAll // When security is enabled, allow all authenticated users
public class CMeetingsView extends CProjectAwareMDPage<CMeeting> implements CInterfaceIconSet {

	private static final long serialVersionUID = 1L;

	public static String getIconColorCode() {
		return CMeeting.getIconColorCode(); // Use the static method from CMeeting
	}

	public static String getIconFilename() { return CMeeting.getIconFilename(); }

	private final String ENTITY_ID_FIELD = "meeting_id";
	private final String ENTITY_ROUTE_TEMPLATE_EDIT = "cmeetingsview/%s/edit";
	private TextField searchField;

	public CMeetingsView(final CMeetingService entityService, final CSessionService sessionService, final CScreenService screenService) {
		super(CMeeting.class, entityService, sessionService, screenService);
		addClassNames("meetings-view");
	}

	@Override
	protected void createDetailsLayout() throws Exception {
		final CScreen screen = screenService.findByNameAndProject(sessionService.getActiveProject().orElse(null), CMeetingViewService.BASE_VIEW_NAME);
		detailsBuilder.buildDetails(screen, getBinder(), getBaseDetailsLayout());
	}

	@Override
	protected void createGridForEntity() {
		LOGGER.info("Creating enhanced grid for meetings with project and participant details");
		// Project Name - Important for context
		grid.addColumn(meeting -> meeting.getProject() != null ? meeting.getProject().getName() : "No Project").setAutoWidth(true)
				.setHeader("Project").setSortable(true).setFlexGrow(0);
		// Meeting Name
		grid.addColumn(meeting -> meeting.getName()).setAutoWidth(true).setHeader("Meeting Name").setSortable(true).setFlexGrow(1);
		// Meeting Type
		grid.addColumn(meeting -> meeting.getMeetingType() != null ? meeting.getMeetingType().getName() : "No Type").setAutoWidth(true)
				.setHeader("Type").setSortable(true).setFlexGrow(0);
		// Start Time with proper formatting
		grid.addColumn(meeting -> {
			if (meeting.getMeetingDate() != null) {
				return meeting.getMeetingDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
			}
			return "Not set";
		}).setAutoWidth(true).setHeader("Start Time").setSortable(true).setFlexGrow(0);
		// End Time with proper formatting
		grid.addColumn(meeting -> {
			if (meeting.getEndDate() != null) {
				return meeting.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
			}
			return "Not set";
		}).setAutoWidth(true).setHeader("End Time").setSortable(true).setFlexGrow(0);
		// Participants with names instead of just count
		grid.addColumn(meeting -> {
			if (meeting.getParticipants().isEmpty()) {
				return "No participants";
			}
			return meeting.getParticipants().stream().map(user -> user.getName() != null ? user.getName() : "User #" + user.getId())
					.collect(java.util.stream.Collectors.joining(", "));
		}).setAutoWidth(true).setHeader("Participants").setSortable(false).setFlexGrow(1);
		// Description - shortened for grid display
		grid.addColumn(meeting -> {
			if ((meeting.getDescription() == null) || meeting.getDescription().trim().isEmpty()) {
				return "No description";
			}
			final String desc = meeting.getDescription().trim();
			return desc.length() > 50 ? desc.substring(0, 47) + "..." : desc;
		}).setAutoWidth(true).setHeader("Description").setSortable(false).setFlexGrow(1);
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected String getEntityRouteTemplateEdit() { return ENTITY_ROUTE_TEMPLATE_EDIT; }

	/** Checks if a meeting matches the search text by searching across all string fields. */
	private boolean matchesSearchText(final CMeeting meeting, final String searchText) {
		if ((meeting == null) || (searchText == null) || searchText.isEmpty()) {
			return true;
		}
		// Search in project name
		if ((meeting.getProject() != null) && (meeting.getProject().getName() != null)) {
			if (meeting.getProject().getName().toLowerCase().contains(searchText)) {
				return true;
			}
		}
		// Search in meeting name
		if ((meeting.getName() != null) && meeting.getName().toLowerCase().contains(searchText)) {
			return true;
		}
		// Search in meeting type name
		if ((meeting.getMeetingType() != null) && (meeting.getMeetingType().getName() != null)) {
			if (meeting.getMeetingType().getName().toLowerCase().contains(searchText)) {
				return true;
			}
		}
		// Search in formatted dates (start and end time)
		if (meeting.getMeetingDate() != null) {
			final String formattedStartTime = meeting.getMeetingDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
			if (formattedStartTime.toLowerCase().contains(searchText)) {
				return true;
			}
		}
		if (meeting.getEndDate() != null) {
			final String formattedEndTime = meeting.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
			if (formattedEndTime.toLowerCase().contains(searchText)) {
				return true;
			}
		}
		// Search in participants names
		if (!meeting.getParticipants().isEmpty()) {
			final String participants =
					meeting.getParticipants().stream().map(user -> user.getName() != null ? user.getName() : "User #" + user.getId())
							.collect(java.util.stream.Collectors.joining(", "));
			if (participants.toLowerCase().contains(searchText)) {
				return true;
			}
		}
		// Search in description
		if ((meeting.getDescription() != null) && meeting.getDescription().toLowerCase().contains(searchText)) {
			return true;
		}
		return false;
	}

	@Override
	protected void setupToolbar() {
		// Create search field
		searchField = new TextField();
		searchField.setPlaceholder("Search meetings...");
		searchField.setPrefixComponent(new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.SEARCH));
		searchField.setWidthFull();
		searchField.setValueChangeMode(ValueChangeMode.LAZY);
		searchField.setValueChangeTimeout(300); // 300ms delay
		// Add value change listener to filter the grid
		searchField.addValueChangeListener(event -> {
			final String searchText = event.getValue();
			if ((searchText == null) || searchText.trim().isEmpty()) {
				// Clear filter - use the existing grid refresh mechanism
				refreshProjectAwareGrid();
			} else // Apply filter
			if (entityService instanceof tech.derbent.abstracts.services.CEntityOfProjectService) {
				final tech.derbent.abstracts.services.CEntityOfProjectService<CMeeting> projectService =
						(tech.derbent.abstracts.services.CEntityOfProjectService<CMeeting>) entityService;
				final Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
				if (activeProject.isPresent()) {
					final List<CMeeting> allMeetings = projectService.findEntriesByProject(activeProject.get(), Pageable.unpaged());
					final List<CMeeting> filteredMeetings =
							allMeetings.stream().filter(meeting -> matchesSearchText(meeting, searchText.toLowerCase().trim()))
									.collect(java.util.stream.Collectors.toList());
					grid.setItems(filteredMeetings);
				} else {
					grid.setItems(Collections.emptyList());
				}
			}
		});
		// Create toolbar layout
		final HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		toolbar.setWidthFull();
		toolbar.setPadding(true);
		toolbar.add(searchField);
		// Add toolbar to the top of the page
		final Div toolbarWrapper = new Div();
		toolbarWrapper.setClassName("toolbar-wrapper");
		toolbarWrapper.add(toolbar);
		// Insert toolbar at the beginning of the page
		getElement().insertChild(0, toolbarWrapper.getElement());
		LOGGER.info("Search toolbar added to meetings view");
	}
}
