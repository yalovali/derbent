package tech.derbent.users.service;

import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.users.domain.CUser;

public class CUserViewService {

	public static final String USER_VIEW_NAME = "User View";

	public static final String BASE_PANEL_NAME = "User Information";

	public static CScreen createBasicView(final CProject project) {

		try {
			final CScreen screen = new CScreen();
			screen.setProject(project);
			screen.setEntityType(CUser.class.getSimpleName());
			screen.setHeaderText("User View");
			screen.setIsActive(Boolean.TRUE);
			screen.setScreenTitle("User View");
			screen.setName(USER_VIEW_NAME);
			screen.setDescription("View details for user");
			// create screen lines
			CScreenLines screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.SECTION);
			screenLine.setEntityProperty(CEntityFieldService.SECTION);
			screenLine.setSectionName(CUserViewService.BASE_PANEL_NAME);
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("name");
			screenLine.setFieldCaption("User Details");
			screenLine.setFieldDescription("User's name and details");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("lastname");
			screenLine.setFieldCaption("Last Name");
			screenLine.setFieldDescription("User's last name");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("login");
			screenLine.setFieldCaption("Login");
			screenLine.setFieldDescription("Login name for the system");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("phone");
			screenLine.setFieldCaption("Phone");
			screenLine.setFieldDescription("Phone number");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("email");
			screenLine.setFieldCaption("Email");
			screenLine.setFieldDescription("User's email address");
			screen.addScreenLine(screenLine);
			// create screen lines
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.SECTION);
			screenLine.setEntityProperty(CEntityFieldService.SECTION);
			screenLine.setSectionName("System Access");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("roles");
			screenLine.setFieldCaption("User Roles");
			screenLine.setFieldDescription("User's roles in the system");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("userRole");
			screenLine.setFieldCaption("User Role");
			screenLine.setFieldDescription("");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("enabled");
			screenLine.setFieldCaption("Enabled");
			screenLine.setFieldDescription("Is the user enabled?");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("company");
			screenLine.setFieldCaption("Company");
			screenLine.setFieldDescription("Company the user belongs to");
			screen.addScreenLine(screenLine);
			//
			screenLine = new CScreenLines();
			screenLine.setRelationFieldName(CEntityFieldService.THIS_CLASS);
			screenLine.setEntityProperty("userType");
			screenLine.setFieldCaption("User Type");
			screenLine.setFieldDescription("The category of user");
			screen.addScreenLine(screenLine);
			return screen;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
