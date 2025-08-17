package tech.derbent.screens.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CViewsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CViewsService.class);

    public List<String> getAvailableBaseTypes() {
        LOGGER.debug("Retrieving available base types for views");
        return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
    }

    /**
     * Get available entity line class types for a given base entity type. This includes the entity itself and related
     * entities accessible through relationships.
     * 
     * @param baseEntityType
     *            the base entity type (e.g., "CActivity")
     * @return list of entity line class types including direct and related entities
     */
    public List<String> getAvailableEntityLineTypes(final String baseEntityType) {
        LOGGER.debug("Retrieving available entity line types for base type: {}", baseEntityType);

        final List<String> entityLineTypes = new ArrayList<>();

        // Add the base entity itself
        entityLineTypes.add(baseEntityType);

        // Add related entity types based on the base entity
        switch (baseEntityType) {
        case "CActivity":
            entityLineTypes.add("Project of Activity");
            entityLineTypes.add("Assigned User of Activity");
            entityLineTypes.add("Created User of Activity");
            entityLineTypes.add("Activity Type of Activity");
            entityLineTypes.add("Activity Status of Activity");
            entityLineTypes.add("Activity Priority of Activity");
            entityLineTypes.add("Parent Activity of Activity");
            break;
        case "CMeeting":
            entityLineTypes.add("Project of Meeting");
            entityLineTypes.add("Assigned User of Meeting");
            entityLineTypes.add("Created User of Meeting");
            entityLineTypes.add("Meeting Type of Meeting");
            entityLineTypes.add("Meeting Status of Meeting");
            break;
        case "CRisk":
            entityLineTypes.add("Project of Risk");
            entityLineTypes.add("Assigned User of Risk");
            entityLineTypes.add("Created User of Risk");
            entityLineTypes.add("Risk Status of Risk");
            entityLineTypes.add("Risk Severity of Risk");
            break;
        case "CProject":
            entityLineTypes.add("Created User of Project");
            break;
        case "CUser":
            // User doesn't have many relationships in the current model
            break;
        default:
            LOGGER.warn("Unknown base entity type: {}", baseEntityType);
            break;
        }

        return entityLineTypes;
    }

    /**
     * Get the actual entity class name for a given entity line type. Maps descriptive names like "Project of Activity"
     * to actual class names like "CProject".
     * 
     * @param entityLineType
     *            the entity line type (e.g., "Project of Activity")
     * @return the actual entity class name (e.g., "CProject")
     */
    public String getEntityClassNameForLineType(final String entityLineType) {
        LOGGER.debug("Getting entity class name for line type: {}", entityLineType);

        // Direct entity types
        if (entityLineType.equals("CActivity") || entityLineType.equals("CMeeting") || entityLineType.equals("CRisk")
                || entityLineType.equals("CProject") || entityLineType.equals("CUser")) {
            return entityLineType;
        }

        // Related entity types
        if (entityLineType.contains("Project of")) {
            return "CProject";
        } else if (entityLineType.contains("User of") || entityLineType.contains("Assigned User")
                || entityLineType.contains("Created User")) {
            return "CUser";
        } else if (entityLineType.contains("Activity Type of")) {
            return "CActivityType";
        } else if (entityLineType.contains("Activity Status of")) {
            return "CActivityStatus";
        } else if (entityLineType.contains("Activity Priority of")) {
            return "CActivityPriority";
        } else if (entityLineType.contains("Parent Activity of")) {
            return "CActivity";
        } else if (entityLineType.contains("Meeting Type of")) {
            return "CMeetingType";
        } else if (entityLineType.contains("Meeting Status of")) {
            return "CMeetingStatus";
        } else if (entityLineType.contains("Risk Status of")) {
            return "CRiskStatus";
        } else if (entityLineType.contains("Risk Severity of")) {
            return "CRiskSeverity";
        }

        LOGGER.warn("Unknown entity line type: {}", entityLineType);
        return entityLineType; // fallback to the original type
    }
}