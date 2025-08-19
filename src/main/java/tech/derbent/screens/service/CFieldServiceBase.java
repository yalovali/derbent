package tech.derbent.screens.service;

import java.util.List;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityPriority;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.CRiskSeverity;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.users.domain.CUser;

public class CFieldServiceBase {

    public static Class<?> getEntityClass(final String entityType) {

        try {

            switch (entityType) {
            case "CActivity":
                return CActivity.class;
            case "CMeeting":
                return CMeeting.class;
            case "CRisk":
                return CRisk.class;
            case "CCompany":
                return CCompany.class;
            case "CProject":
                return CProject.class;
            case "CUser":
                return CUser.class;
            case "CActivityType":
                return CActivityType.class;
            case "CActivityStatus":
                return CActivityStatus.class;
            case "CActivityPriority":
                return CActivityPriority.class;
            case "CMeetingType":
                return CMeetingType.class;
            case "CMeetingStatus":
                return CMeetingStatus.class;
            case "CRiskStatus":
                return CRiskStatus.class;
            case "CRiskSeverity":
                return CRiskSeverity.class;
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
            }
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Get available entity types for screen configuration.
     * 
     * @return list of entity types
     */
    public List<String> getAvailableEntityTypes() {
        return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
    }
}
