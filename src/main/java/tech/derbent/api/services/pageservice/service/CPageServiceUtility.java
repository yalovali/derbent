package tech.derbent.api.services.pageservice.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.screens.service.CViewsService;
import tech.derbent.api.services.pageservice.implementations.CPageServiceActivity;
import tech.derbent.api.services.pageservice.implementations.CPageServiceActivityPriority;
import tech.derbent.api.services.pageservice.implementations.CPageServiceActivityType;
import tech.derbent.api.services.pageservice.implementations.CPageServiceApprovalStatus;
import tech.derbent.api.services.pageservice.implementations.CPageServiceComment;
import tech.derbent.api.services.pageservice.implementations.CPageServiceCommentPriority;
import tech.derbent.api.services.pageservice.implementations.CPageServiceCompany;
import tech.derbent.api.services.pageservice.implementations.CPageServiceCurrency;
import tech.derbent.api.services.pageservice.implementations.CPageServiceDecision;
import tech.derbent.api.services.pageservice.implementations.CPageServiceDecisionType;
import tech.derbent.api.services.pageservice.implementations.CPageServiceGridEntity;
import tech.derbent.api.services.pageservice.implementations.CPageServiceMeeting;
import tech.derbent.api.services.pageservice.implementations.CPageServiceMeetingType;
import tech.derbent.api.services.pageservice.implementations.CPageServiceOrder;
import tech.derbent.api.services.pageservice.implementations.CPageServiceOrderApproval;
import tech.derbent.api.services.pageservice.implementations.CPageServiceOrderType;
import tech.derbent.api.services.pageservice.implementations.CPageServicePageEntity;
import tech.derbent.api.services.pageservice.implementations.CPageServiceProject;
import tech.derbent.api.services.pageservice.implementations.CPageServiceProjectItemStatus;
import tech.derbent.api.services.pageservice.implementations.CPageServiceRisk;
import tech.derbent.api.services.pageservice.implementations.CPageServiceRiskType;
import tech.derbent.api.services.pageservice.implementations.CPageServiceSystemSettings;
import tech.derbent.api.services.pageservice.implementations.CPageServiceUser;
import tech.derbent.api.services.pageservice.implementations.CPageServiceUserCompanyRole;
import tech.derbent.api.services.pageservice.implementations.CPageServiceUserCompanySetting;
import tech.derbent.api.services.pageservice.implementations.CPageServiceUserProjectRole;
import tech.derbent.api.services.pageservice.implementations.CPageServiceUserProjectSettings;
import tech.derbent.api.services.pageservice.implementations.CPageServiceWorkflowEntity;

@Service
public class CPageServiceUtility {

	private static final List<String> availablePageServices = List.of("CPageServiceActivity", "CPageServiceComment", "CPageServiceCompany",
			"CPageServiceDecision", "CPageServiceMeeting", "CPageServiceOrder", "CPageServiceProject", "CPageServiceRisk", "CPageServiceUser",
			"CPageServiceSystemSettings", "CPageServiceActivityPriority", "CPageServiceProjectItemStatus", "CPageServiceActivityType",
			"CPageServiceRiskType", "CPageServiceCommentPriority", "CPageServiceDecisionStatus", "CPageServiceDecisionType",
			"CPageServiceMeetingStatus", "CPageServiceMeetingType", "CPageServiceOrderStatus", "CPageServiceOrderType", "CPageServiceOrderApproval",
			"CPageServiceApprovalStatus", "CPageServiceCurrency", "CPageServiceRiskStatus", "CPageServiceUserCompanyRole",
			"CPageServiceUserCompanySetting", "CPageServiceUserProjectRole", "CPageServiceUserProjectSettings", "CPageServicePageEntity",
			"CPageServiceGridEntity");
	private static Logger LOGGER = LoggerFactory.getLogger(CPageServiceUtility.class);

	public static Class<?> getPageServiceClassByName(String serviceName) {
		switch (serviceName) {
		// Main entities
		case "CPageServiceActivity":
			return CPageServiceActivity.class;
		case "CPageServiceComment":
			return CPageServiceComment.class;
		case "CPageServiceCompany":
			return CPageServiceCompany.class;
		case "CPageServiceDecision":
			return CPageServiceDecision.class;
		case "CPageServiceMeeting":
			return CPageServiceMeeting.class;
		case "CPageServiceOrder":
			return CPageServiceOrder.class;
		case "CPageServiceProject":
			return CPageServiceProject.class;
		case "CPageServiceRisk":
			return CPageServiceRisk.class;
		case "CPageServiceUser":
			return CPageServiceUser.class;
		case "CPageServiceSystemSettings":
			return CPageServiceSystemSettings.class;
		case "CPageServiceActivityPriority":
			return CPageServiceActivityPriority.class;
		case "CPageServiceProjectItemStatus":
			return CPageServiceProjectItemStatus.class;
		case "CPageServiceActivityType":
			return CPageServiceActivityType.class;
		case "CPageServiceRiskType":
			return CPageServiceRiskType.class;
		case "CPageServiceCommentPriority":
			return CPageServiceCommentPriority.class;
		case "CPageServiceDecisionType":
			return CPageServiceDecisionType.class;
		case "CPageServiceMeetingType":
			return CPageServiceMeetingType.class;
		case "CPageServiceOrderType":
			return CPageServiceOrderType.class;
		case "CPageServiceOrderApproval":
			return CPageServiceOrderApproval.class;
		case "CPageServiceApprovalStatus":
			return CPageServiceApprovalStatus.class;
		case "CPageServiceCurrency":
			return CPageServiceCurrency.class;
		case "CPageServiceUserCompanyRole":
			return CPageServiceUserCompanyRole.class;
		case "CPageServiceUserCompanySetting":
			return CPageServiceUserCompanySetting.class;
		case "CPageServiceUserProjectRole":
			return CPageServiceUserProjectRole.class;
		case "CPageServiceUserProjectSettings":
			return CPageServiceUserProjectSettings.class;
		case "CPageServicePageEntity":
			return CPageServicePageEntity.class;
		case "CPageServiceGridEntity":
			return CPageServiceGridEntity.class;
		case "CPageServiceWorkflowEntity":
			return CPageServiceWorkflowEntity.class;
		default:
			LOGGER.error("Page service '{}' not implemented", serviceName);
			throw new IllegalArgumentException("Page service not implemented: " + serviceName);
		}
	}

	/** Maps entity class to corresponding PageService class name
	 * @param entityClass The entity class
	 * @return The PageService class name, or null if not mapped */
	public static String getPageServiceNameForEntityClass(Class<?> entityClass) {
		String className = entityClass.getSimpleName();
		switch (className) {
		// Main entities
		case "CActivity":
			return "CPageServiceActivity";
		case "CComment":
			return "CPageServiceComment";
		case "CCompany":
			return "CPageServiceCompany";
		case "CDecision":
			return "CPageServiceDecision";
		case "CMeeting":
			return "CPageServiceMeeting";
		case "COrder":
			return "CPageServiceOrder";
		case "CProject":
			return "CPageServiceProject";
		case "CRisk":
			return "CPageServiceRisk";
		case "CUser":
			return "CPageServiceUser";
		case "CSystemSettings":
			return "CPageServiceSystemSettings";
		// Type/Status entities
		case "CActivityPriority":
			return "CPageServiceActivityPriority";
		case "CProjectItemStatus":
			return "CPageServiceProjectItemStatus";
		case "CRiskType":
			return "CPageServiceRiskType";
		case "CActivityType":
			return "CPageServiceActivityType";
		case "CCommentPriority":
			return "CPageServiceCommentPriority";
		case "CDecisionStatus":
			return "CPageServiceDecisionStatus";
		case "CDecisionType":
			return "CPageServiceDecisionType";
		case "CMeetingStatus":
			return "CPageServiceMeetingStatus";
		case "CMeetingType":
			return "CPageServiceMeetingType";
		case "COrderStatus":
			return "CPageServiceOrderStatus";
		case "COrderType":
			return "CPageServiceOrderType";
		case "COrderApproval":
			return "CPageServiceOrderApproval";
		case "CApprovalStatus":
			return "CPageServiceApprovalStatus";
		case "CCurrency":
			return "CPageServiceCurrency";
		case "CRiskStatus":
			return "CPageServiceRiskStatus";
		case "CUserCompanyRole":
			return "CPageServiceUserCompanyRole";
		case "CUserCompanySetting":
			return "CPageServiceUserCompanySetting";
		case "CUserProjectRole":
			return "CPageServiceUserProjectRole";
		case "CUserProjectSettings":
			return "CPageServiceUserProjectSettings";
		// System entities
		case "CPageEntity":
			return "CPageServicePageEntity";
		case "CGridEntity":
			return "CPageServiceGridEntity";
		case "CWorkflowEntity":
			return "CPageServiceWorkflowEntity";
		default:
			// Return null for entities that don't have a PageService yet
			return null;
		}
	}

	public CPageServiceUtility(CViewsService viewsService) {
		super();
	}

	public List<String> getPageServiceList() { return CPageServiceUtility.availablePageServices; }
}
