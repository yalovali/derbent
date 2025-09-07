package tech.derbent.examples.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.components.CSearchToolbar;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import java.util.ArrayList;
import java.util.List;

/** CSearchDemoView - Demonstration view showing search functionality. Layer: View (MVC) A standalone demo page that shows the search toolbar and grid
 * functionality without requiring database setup. */
@Route ("search-demo")
@PermitAll
public class CSearchDemoView extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private CGrid<CProject> projectGrid;
	private CGrid<CUser> userGrid;
	private final List<CProject> allProjects;
	private final List<CUser> allUsers;

	public CSearchDemoView() {
		setSizeFull();
		setPadding(true);
		setSpacing(true);
		// Create title
		add(new H2("Search Functionality Demo"));
		add(new Paragraph("This page demonstrates the search functionality implemented for grids and toolbars."));
		// Create sample data
		allProjects = createSampleProjects();
		allUsers = createSampleUsers();
		// Create project section
		add(new H2("Projects with Search"));
		createProjectSection();
		// Create user section
		add(new H2("Users with Search"));
		createUserSection();
		// Instructions
		add(new Paragraph("Try typing in the search fields above to filter the data in real-time. "
				+ "The search is case-insensitive and searches across multiple fields including names, descriptions, emails, etc."));
	}

	private void createProjectSection() {
		CSearchToolbar projectSearch = new CSearchToolbar("Search projects...");
		projectGrid = new CGrid<>(CProject.class);
		projectGrid.getColumns().forEach(projectGrid::removeColumn);
		projectGrid.addShortTextColumn(CProject::getName, "Name", "name");
		projectGrid.addColumn(project -> {
			String desc = project.getDescription();
			return desc != null ? (desc.length() > 50 ? desc.substring(0, 50) + "..." : desc) : "";
		}, "Description", null);
		projectGrid.setHeight("200px");
		// Initial data load
		projectGrid.setItems(allProjects);
		// Search functionality
		projectSearch.addSearchListener(event -> {
			String searchText = event.getSearchText();
			if (searchText == null || searchText.trim().isEmpty()) {
				projectGrid.setItems(allProjects);
			} else {
				List<CProject> filtered = allProjects.stream().filter(project -> project.matches(searchText)).toList();
				projectGrid.setItems(filtered);
			}
		});
		add(projectSearch, projectGrid);
	}

	private void createUserSection() {
		CSearchToolbar userSearch = new CSearchToolbar("Search users...");
		userGrid = new CGrid<>(CUser.class);
		userGrid.getColumns().forEach(userGrid::removeColumn);
		userGrid.addShortTextColumn(CUser::getName, "First Name", "name");
		userGrid.addShortTextColumn(CUser::getLastname, "Last Name", "lastname");
		userGrid.addShortTextColumn(CUser::getLogin, "Login", "login");
		userGrid.addShortTextColumn(CUser::getEmail, "Email", "email");
		userGrid.setHeight("200px");
		// Initial data load
		userGrid.setItems(allUsers);
		// Search functionality
		userSearch.addSearchListener(event -> {
			String searchText = event.getSearchText();
			if (searchText == null || searchText.trim().isEmpty()) {
				userGrid.setItems(allUsers);
			} else {
				List<CUser> filtered = allUsers.stream().filter(user -> user.matches(searchText)).toList();
				userGrid.setItems(filtered);
			}
		});
		add(userSearch, userGrid);
	}

	private List<CProject> createSampleProjects() {
		List<CProject> projects = new ArrayList<>();
		CProject p1 = new CProject("Website Redesign");
		p1.setDescription("Complete redesign of the company website using modern technologies");
		projects.add(p1);
		CProject p2 = new CProject("Mobile App Development");
		p2.setDescription("Native mobile application for iOS and Android platforms");
		projects.add(p2);
		CProject p3 = new CProject("Database Migration");
		p3.setDescription("Migration from legacy database system to modern cloud infrastructure");
		projects.add(p3);
		CProject p4 = new CProject("API Integration");
		p4.setDescription("Integration with third-party APIs for enhanced functionality");
		projects.add(p4);
		CProject p5 = new CProject("Security Audit");
		p5.setDescription("Comprehensive security assessment and vulnerability testing");
		projects.add(p5);
		return projects;
	}

	private List<CUser> createSampleUsers() {
		List<CUser> users = new ArrayList<>();
		CUser u1 = new CUser();
		u1.setName("Alice");
		u1.setLastname("Johnson");
		u1.setLogin("ajohnson");
		u1.setEmail("alice.johnson@company.com");
		u1.setDescription("Senior Software Developer");
		users.add(u1);
		CUser u2 = new CUser();
		u2.setName("Bob");
		u2.setLastname("Smith");
		u2.setLogin("bsmith");
		u2.setEmail("bob.smith@company.com");
		u2.setDescription("Project Manager");
		users.add(u2);
		CUser u3 = new CUser();
		u3.setName("Carol");
		u3.setLastname("Davis");
		u3.setLogin("cdavis");
		u3.setEmail("carol.davis@company.com");
		u3.setDescription("UX Designer");
		users.add(u3);
		CUser u4 = new CUser();
		u4.setName("David");
		u4.setLastname("Wilson");
		u4.setLogin("dwilson");
		u4.setEmail("david.wilson@company.com");
		u4.setDescription("DevOps Engineer");
		users.add(u4);
		CUser u5 = new CUser();
		u5.setName("Emma");
		u5.setLastname("Brown");
		u5.setLogin("ebrown");
		u5.setEmail("emma.brown@company.com");
		u5.setDescription("Quality Assurance Specialist");
		users.add(u5);
		return users;
	}
}
