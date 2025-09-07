package tech.derbent.screens.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.abstracts.domains.CEntity;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityPriority;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.gannt.view.CProjectGanntView;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.orders.domain.COrder;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;

public class CFieldServiceBase {

	static Logger LOGGER = LoggerFactory.getLogger(CFieldServiceBase.class);

	private static void findSubclasses(final File dir, final String packageName, final List<Class<?>> subclasses) {
		for (final File file : dir.listFiles()) {
			if (file.isDirectory()) {
				findSubclasses(file, packageName + "." + file.getName(), subclasses);
			} else if (file.getName().endsWith(".java")) {
				try {
					final String className = packageName + "." + file.getName().replace(".java", "");
					final Class<?> clazz = Class.forName(className);
					if (CEntity.class.isAssignableFrom(clazz) && !clazz.equals(CEntity.class)) {
						subclasses.add(clazz);
					}
				} catch (final Throwable e) {
					// Ignore classes that can't be loaded
				}
			}
		}
	}

	/** Get available entity types for screen configuration.
	 * @return list of entity types */
	public static List<String> getAvailableEntityTypes() {
		return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
	}

	public static Class<?> getEntityClass(final String entityType) {
		try {
			switch (entityType) {
			case "CActivity":
				return CActivity.class;
			case "CMeeting":
				return CMeeting.class;
			case "COrder":
				return COrder.class; // COrder.class;
			case "CRisk":
				return CRisk.class;
			case "CCompany":
				return CCompany.class;
			case "CProject":
				return CProject.class;
			case "CDecision":
				return CDecision.class;
			case "CUser":
				return CUser.class;
			case "CUserType":
				return CUserType.class;
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
			case "CProjectGanntView":
				return CProjectGanntView.class;
			default:
				LOGGER.error("Unknown entity type: " + entityType);
				throw new IllegalArgumentException("Unknown entity type: " + entityType);
			}
		} catch (final Exception e) {
			return null;
		}
	}

	public static void printAllClassesExtendingCEntity() {
		final String baseDir = "src"; // Adjust path to your source directory
		final List<Class<?>> subclasses = new ArrayList<>();
		findSubclasses(new File(baseDir), "your.package.name", subclasses);
		for (final Class<?> clazz : subclasses) {
			System.out.println(clazz.getName());
		}
	}
}
