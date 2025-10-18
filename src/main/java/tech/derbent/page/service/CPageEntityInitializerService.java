package tech.derbent.page.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.service.CInitializerServiceBase;

public class CPageEntityInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Page Information";
	private static final Class<?> clazz = CPageEntity.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageEntityInitializerService.class);

	public static CDetailSection createBasicView(final CProject project) {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Navigation"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "menuTitle"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "menuOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "pageTitle"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "icon"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Layout Configuration"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "gridEntity"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "detailSection"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "content"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Security & Behavior"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "requiresAuthentication"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "showInQuickToolbar"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeReadonly"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeNonDeletable"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating page entity view.");
			return null;
		}
	}

	private static String createDashboardContent() {
		return "<div style=\"padding: 24px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\">"
				+ "<div style=\"display: flex; align-items: center; margin-bottom: 32px;\">"
				+ "<span style=\"font-size: 32px; margin-right: 16px;\">📊</span>"
				+ "<h1 style=\"margin: 0; color: #1976D2; font-size: 28px;\">Project Dashboard</h1>" + "</div>"
				+ "<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px; margin-bottom: 32px;\">"
				+ "<div style=\"background: linear-gradient(135deg, #2196F3, #21CBF3); color: white; padding: 24px; border-radius: 12px; box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);\">"
				+ "<h3 style=\"margin: 0 0 8px 0; font-size: 16px; opacity: 0.9;\">📋 Active Tasks</h3>"
				+ "<div style=\"font-size: 32px; font-weight: bold;\">24</div>" + "</div>"
				+ "<div style=\"background: linear-gradient(135deg, #4CAF50, #81C784); color: white; padding: 24px; border-radius: 12px; box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);\">"
				+ "<h3 style=\"margin: 0 0 8px 0; font-size: 16px; opacity: 0.9;\">✅ Completed</h3>"
				+ "<div style=\"font-size: 32px; font-weight: bold;\">157</div>" + "</div>"
				+ "<div style=\"background: linear-gradient(135deg, #FF9800, #FFB74D); color: white; padding: 24px; border-radius: 12px; box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);\">"
				+ "<h3 style=\"margin: 0 0 8px 0; font-size: 16px; opacity: 0.9;\">⏰ Due Soon</h3>"
				+ "<div style=\"font-size: 32px; font-weight: bold;\">7</div>" + "</div>" + "</div>"
				+ "<div style=\"background: #f8f9fa; padding: 24px; border-radius: 12px; border-left: 4px solid #2196F3;\">"
				+ "<h3 style=\"margin: 0 0 16px 0; color: #333; display: flex; align-items: center;\">"
				+ "<span style=\"margin-right: 8px;\">🎯</span>Project Overview</h3>"
				+ "<p style=\"color: #666; line-height: 1.6; margin: 0;\">Welcome to your project dashboard. "
				+ "Here you can monitor project progress, track team performance, and access key metrics at a glance.</p>" + "</div></div>";
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(
				"id,name,menuTitle,pageTitle,menuOrder,requiresAuthentication,showInQuickToolbar,attributeReadonly,attributeNonDeletable");
		return grid;
	}

	private static String createQuickActionsContent() {
		return "<div style=\"padding: 24px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\">"
				+ "<div style=\"display: flex; align-items: center; margin-bottom: 32px;\">"
				+ "<span style=\"font-size: 32px; margin-right: 16px;\">⚡</span>"
				+ "<h1 style=\"margin: 0; color: #F57C00; font-size: 28px;\">Quick Actions</h1>" + "</div>"
				+ "<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px;\">"
				+ "<button style=\"background: #2196F3; color: white; border: none; padding: 20px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; box-shadow: 0 2px 8px rgba(33, 150, 243, 0.3); transition: transform 0.2s;\" onmouseover=\"this.style.transform='translateY(-2px)'\" onmouseout=\"this.style.transform='translateY(0)'\">"
				+ "📋 Create New Task</button>"
				+ "<button style=\"background: #4CAF50; color: white; border: none; padding: 20px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3); transition: transform 0.2s;\" onmouseover=\"this.style.transform='translateY(-2px)'\" onmouseout=\"this.style.transform='translateY(0)'\">"
				+ "👥 Schedule Meeting</button>"
				+ "<button style=\"background: #FF9800; color: white; border: none; padding: 20px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3); transition: transform 0.2s;\" onmouseover=\"this.style.transform='translateY(-2px)'\" onmouseout=\"this.style.transform='translateY(0)'\">"
				+ "📊 Generate Report</button>"
				+ "<button style=\"background: #9C27B0; color: white; border: none; padding: 20px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; box-shadow: 0 2px 8px rgba(156, 39, 176, 0.3); transition: transform 0.2s;\" onmouseover=\"this.style.transform='translateY(-2px)'\" onmouseout=\"this.style.transform='translateY(0)'\">"
				+ "⚙️ Project Settings</button>"
				+ "<button style=\"background: #607D8B; color: white; border: none; padding: 20px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; box-shadow: 0 2px 8px rgba(96, 125, 139, 0.3); transition: transform 0.2s;\" onmouseover=\"this.style.transform='translateY(-2px)'\" onmouseout=\"this.style.transform='translateY(0)'\">"
				+ "📁 File Manager</button>"
				+ "<button style=\"background: #E91E63; color: white; border: none; padding: 20px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; box-shadow: 0 2px 8px rgba(233, 30, 99, 0.3); transition: transform 0.2s;\" onmouseover=\"this.style.transform='translateY(-2px)'\" onmouseout=\"this.style.transform='translateY(0)'\">"
				+ "⚠️ Risk Assessment</button>" + "</div></div>";
	}

	private static String createReportsContent() {
		return "<div style=\"padding: 24px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\">"
				+ "<div style=\"display: flex; align-items: center; margin-bottom: 32px;\">"
				+ "<span style=\"font-size: 32px; margin-right: 16px;\">📈</span>"
				+ "<h1 style=\"margin: 0; color: #388E3C; font-size: 28px;\">Reports & Analytics</h1>" + "</div>"
				+ "<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px;\">"
				+ "<div style=\"background: linear-gradient(135deg, #4CAF50, #66BB6A); color: white; padding: 24px; border-radius: 12px; box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);\">"
				+ "<h3 style=\"margin: 0 0 16px 0; font-size: 18px; opacity: 0.9;\">📊 Performance Metrics</h3>"
				+ "<div style=\"margin-bottom: 12px;\">Sprint Velocity: <strong>32 points</strong></div>"
				+ "<div style=\"margin-bottom: 12px;\">Team Efficiency: <strong>87%</strong></div>"
				+ "<div>Quality Score: <strong>9.2/10</strong></div>" + "</div>"
				+ "<div style=\"background: linear-gradient(135deg, #2196F3, #42A5F5); color: white; padding: 24px; border-radius: 12px; box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);\">"
				+ "<h3 style=\"margin: 0 0 16px 0; font-size: 18px; opacity: 0.9;\">📋 Project Status</h3>"
				+ "<div style=\"margin-bottom: 12px;\">Completion: <strong>73%</strong></div>"
				+ "<div style=\"margin-bottom: 12px;\">On Schedule: <strong>Yes</strong></div>" + "<div>Budget: <strong>Within limits</strong></div>"
				+ "</div>"
				+ "<div style=\"background: linear-gradient(135deg, #FF9800, #FFA726); color: white; padding: 24px; border-radius: 12px; box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);\">"
				+ "<h3 style=\"margin: 0 0 16px 0; font-size: 18px; opacity: 0.9;\">⚠️ Attention Needed</h3>"
				+ "<div style=\"margin-bottom: 12px;\">Overdue Tasks: <strong>3</strong></div>"
				+ "<div style=\"margin-bottom: 12px;\">Risk Items: <strong>2</strong></div>" + "<div>Blockers: <strong>1</strong></div>" + "</div>"
				+ "</div></div>";
	}

	/** Create sample dashboard pages with good icons, colors, and organization */
	private static void createSampleDashboardPages(final CProject project, final CPageEntityService pageEntityService) {
		try {
			// Dashboard Overview
			CPageEntity dashboard = new CPageEntity("Project Dashboard", project);
			dashboard.setMenuTitle("Dashboard.Overview");
			dashboard.setPageTitle("📊 Project Dashboard");
			dashboard.setMenuOrder("1.0");
			dashboard.setIcon("vaadin:dashboard");
			dashboard.setDescription("Main project dashboard with key metrics and status overview");
			dashboard.setContent(createDashboardContent());
			dashboard.setRequiresAuthentication(true);
			dashboard.setAttributeShowInQuickToolbar(true); // Mark this page to appear in quick toolbar
			pageEntityService.save(dashboard);
			// Team Collaboration Hub
			CPageEntity teamHub = new CPageEntity("Team Hub", project);
			teamHub.setMenuTitle("Dashboard.Team Hub");
			teamHub.setPageTitle("👥 Team Collaboration");
			teamHub.setMenuOrder("1.1");
			teamHub.setIcon("vaadin:users");
			teamHub.setDescription("Team collaboration workspace with communication tools");
			teamHub.setContent(createTeamHubContent());
			teamHub.setRequiresAuthentication(true);
			pageEntityService.save(teamHub);
			// Reports Center
			CPageEntity reports = new CPageEntity("Reports Center", project);
			reports.setMenuTitle("Dashboard.Reports");
			reports.setPageTitle("📈 Reports & Analytics");
			reports.setMenuOrder("1.2");
			reports.setIcon("vaadin:chart");
			reports.setDescription("Comprehensive reports and analytics dashboard");
			reports.setContent(createReportsContent());
			reports.setRequiresAuthentication(true);
			pageEntityService.save(reports);
			// Quick Actions
			CPageEntity quickActions = new CPageEntity("Quick Actions", project);
			quickActions.setMenuTitle("Tools.Quick Actions");
			quickActions.setPageTitle("⚡ Quick Actions");
			quickActions.setMenuOrder("80.0");
			quickActions.setIcon("vaadin:flash");
			quickActions.setDescription("Quick access to common project actions and workflows");
			quickActions.setContent(createQuickActionsContent());
			quickActions.setRequiresAuthentication(true);
			quickActions.setAttributeShowInQuickToolbar(true); // Mark this page to appear in quick toolbar
			pageEntityService.save(quickActions);
			LOGGER.info("Created {} sample dashboard pages for project: {}", 4, project.getName());
		} catch (Exception e) {
			LOGGER.error("Error creating sample dashboard pages.");
		}
	}

	private static String createTeamHubContent() {
		return "<div style=\"padding: 24px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\">"
				+ "<div style=\"display: flex; align-items: center; margin-bottom: 32px;\">"
				+ "<span style=\"font-size: 32px; margin-right: 16px;\">👥</span>"
				+ "<h1 style=\"margin: 0; color: #7B1FA2; font-size: 28px;\">Team Collaboration Hub</h1>" + "</div>"
				+ "<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 24px;\">"
				+ "<div style=\"background: white; padding: 24px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-left: 4px solid #9C27B0;\">"
				+ "<h3 style=\"margin: 0 0 16px 0; color: #9C27B0; display: flex; align-items: center;\">"
				+ "<span style=\"margin-right: 8px;\">💬</span>Recent Discussions</h3>" + "<ul style=\"list-style: none; padding: 0; margin: 0;\">"
				+ "<li style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Project milestone review</li>"
				+ "<li style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">UI/UX design feedback</li>"
				+ "<li style=\"padding: 8px 0;\">Sprint planning session</li>" + "</ul>" + "</div>"
				+ "<div style=\"background: white; padding: 24px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); border-left: 4px solid #FF5722;\">"
				+ "<h3 style=\"margin: 0 0 16px 0; color: #FF5722; display: flex; align-items: center;\">"
				+ "<span style=\"margin-right: 8px;\">📅</span>Upcoming Events</h3>" + "<ul style=\"list-style: none; padding: 0; margin: 0;\">"
				+ "<li style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Stand-up meeting (2 hours)</li>"
				+ "<li style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Design review (Tomorrow)</li>"
				+ "<li style=\"padding: 8px 0;\">Client presentation (Friday)</li>" + "</ul>" + "</div>" + "</div></div>";
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		Check.notNull(project, "project cannot be null");
		Check.notNull(gridEntityService, "gridEntityService cannot be null");
		Check.notNull(detailSectionService, "detailSectionService cannot be null");
		Check.notNull(pageEntityService, "pageEntityService cannot be null");
		final CDetailSection detailSection = createBasicView(project);
		detailSectionService.save(detailSection);
		final CGridEntity grid = createGridEntity(project);
		gridEntityService.save(grid);
		// Create the main page entity management page
		final CPageEntity mainPage = createPageEntity(clazz, project, grid, detailSection, "System.Pages", "Dynamic Page Management",
				"Manage dynamic page configurations and navigation metadata", "1.1");
		pageEntityService.save(mainPage);
		// Create sample dashboard pages with good icons and organization
		createSampleDashboardPages(project, pageEntityService);
	}
}
