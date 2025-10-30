package tech.derbent.api.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.domains.CEntity;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityPriority;
import tech.derbent.app.activities.domain.CActivityType;
import tech.derbent.app.activities.service.CActivityInitializerService;
import tech.derbent.app.activities.service.CActivityPriorityInitializerService;
import tech.derbent.app.activities.service.CActivityPriorityService;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.activities.service.CActivityTypeInitializerService;
import tech.derbent.app.activities.service.CActivityTypeService;
import tech.derbent.app.activities.service.CProjectItemStatusInitializerService;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.comments.domain.CCommentPriority;
import tech.derbent.app.comments.service.CCommentPriorityService;
import tech.derbent.app.comments.view.CCommentPriorityInitializerService;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.companies.service.CCompanyInitializerService;
import tech.derbent.app.companies.service.CCompanyService;
import tech.derbent.app.decisions.domain.CDecision;
import tech.derbent.app.decisions.domain.CDecisionType;
import tech.derbent.app.decisions.service.CDecisionInitializerService;
import tech.derbent.app.decisions.service.CDecisionService;
import tech.derbent.app.decisions.service.CDecisionTypeInitializerService;
import tech.derbent.app.decisions.service.CDecisionTypeService;
import tech.derbent.app.gannt.view.CProjectGanntView;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.meetings.domain.CMeetingType;
import tech.derbent.app.meetings.service.CMeetingInitializerService;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.meetings.service.CMeetingTypeInitializerService;
import tech.derbent.app.meetings.service.CMeetingTypeService;
import tech.derbent.app.orders.domain.CApprovalStatus;
import tech.derbent.app.orders.domain.CCurrency;
import tech.derbent.app.orders.domain.COrder;
import tech.derbent.app.orders.domain.COrderType;
import tech.derbent.app.orders.service.CApprovalStatusInitializerService;
import tech.derbent.app.orders.service.CApprovalStatusService;
import tech.derbent.app.orders.service.CCurrencyInitializerService;
import tech.derbent.app.orders.service.CCurrencyService;
import tech.derbent.app.orders.service.COrderInitializerService;
import tech.derbent.app.orders.service.COrderService;
import tech.derbent.app.orders.service.COrderTypeInitializerService;
import tech.derbent.app.orders.service.COrderTypeService;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.service.CPageEntityInitializerService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.projects.service.CProjectInitializerService;
import tech.derbent.app.projects.service.CProjectService;
import tech.derbent.app.risks.domain.CRisk;
import tech.derbent.app.risks.domain.CRiskType;
import tech.derbent.app.risks.service.CRiskInitializerService;
import tech.derbent.app.risks.service.CRiskService;
import tech.derbent.app.risks.service.CRiskTypeService;
import tech.derbent.app.roles.domain.CUserCompanyRole;
import tech.derbent.app.roles.domain.CUserProjectRole;
import tech.derbent.app.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.app.roles.service.CUserCompanyRoleService;
import tech.derbent.app.roles.service.CUserProjectRoleInitializerService;
import tech.derbent.app.roles.service.CUserProjectRoleService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.CWorkflowEntityService;
import tech.derbent.base.setup.domain.CSystemSettings;
import tech.derbent.base.setup.service.CSystemSettingsInitializerService;
import tech.derbent.base.setup.service.CSystemSettingsService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserInitializerService;
import tech.derbent.base.users.service.CUserService;

public class CAuxillaries {

	public static final Logger LOGGER = LoggerFactory.getLogger(CAuxillaries.class);

	public static String formatWidthPx(int i) {
		if (i <= 0) {
			return null;
		}
		return i + "px";
	}

	public static String generateId(final Component component) {
		final String prefix = component.getClass().getSimpleName().toLowerCase();
		String suffix;
		final String text = getComponentText(component);
		if ((text != null) && !text.trim().isEmpty()) {
			suffix = text.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
		} else {
			final String tag = component.getElement().getTag();
			if ((tag != null) && !tag.trim().isEmpty()) {
				suffix = tag.toLowerCase() + "-" + System.currentTimeMillis();
			} else {
				suffix = String.valueOf(System.currentTimeMillis());
			}
		}
		return prefix + "-" + suffix;
	}

	/** Get available entity types for screen configuration.
	 * @return list of entity types */
	public static List<String> getAvailableEntityTypes() {
		return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
	}

	private static Method getClazzMethod(final Class<?> clazz, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(clazz, "clazz is null");
			final Method method = clazz.getMethod(methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + clazz.getName());
			Check.isTrue(method.getParameterCount() == 0, "Method " + methodName + " in class " + clazz.getName() + " has parameters");
			return method;
		} catch (final Exception e) {
			LOGGER.error("Error getting method " + methodName + " from class " + clazz.getName(), e);
			throw e;
		}
	}

	private static Method getClazzMethodStatic(final Class<?> clazz, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(clazz, "clazz is null");
			final Method method = clazz.getMethod(methodName);
			if (Modifier.isStatic(method.getModifiers())) {
				return method;
			} else {
				throw new IllegalArgumentException("Method " + methodName + " in class " + clazz.getName() + " is not statric");
			}
		} catch (final Exception e) {
			LOGGER.error("Error getting method " + methodName + " from class " + clazz.getName(), e);
			throw e;
		}
	}

	private static Method getClazzMethodStatic(final String className, final String methodName) throws Exception {
		Check.notBlank(className, "className is blank");
		Check.notBlank(methodName, "methodName is blank");
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Check.notNull(cl, "ClassLoader is null");
		final Class<?> clazz = Class.forName(className, true, cl);
		Check.notNull(clazz, "Class " + className + " not found");
		return getClazzMethodStatic(clazz, methodName);
	}

	private static String getComponentText(final Component component) {
		Check.notNull(component, "component is null");
		if (component instanceof com.vaadin.flow.component.HasText) {
			return ((com.vaadin.flow.component.HasText) component).getText();
		}
		return null;
	}

	// convert all cases to use this method instead of reflection
	public static Class<?> getEntityClass(final String simpleName) {
		Check.notBlank(simpleName, "Entity type must not be empty");
		switch (simpleName) {
		case "CEntity":
			return CEntity.class;
		case "CPageEntity":
			return CPageEntity.class;
		case "CActivity":
			return CActivity.class;
		case "CMeeting":
			return CMeeting.class;
		case "COrder":
			return COrder.class;
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
		case "CActivityType":
			return CActivityType.class;
		case "CRiskType":
			return CRiskType.class;
		case "CProjectItemStatus":
			return CProjectItemStatus.class;
		case "CActivityPriority":
			return CActivityPriority.class;
		case "CMeetingType":
			return CMeetingType.class;
		case "CProjectGanntView":
			return CProjectGanntView.class;
		case "CCommentPriority":
			return CCommentPriority.class;
		case "CCurrency":
			return CCurrency.class;
		case "CDecisionType":
			return CDecisionType.class;
		case "COrderType":
			return COrderType.class;
		case "CApprovalStatus":
			return CApprovalStatus.class;
		case "CDetailSection":
			return CDetailSection.class;
		case "CGridEntity":
			return CGridEntity.class;
		case "CSystemSettings":
			return CSystemSettings.class;
		case "CUserProjectRole":
			return CUserProjectRole.class;
		case "CUserCompanyRole":
			return CUserCompanyRole.class;
		case "CWorkflowEntity":
			return CWorkflowEntity.class;
		// ... add
		default:
			LOGGER.error("Unknown entity type: " + simpleName + " dont forget to update (CAuxillaries.java:234)");
			throw new IllegalArgumentException("Unknown entity type: " + simpleName);
		}
	}

	public static Class<?> getEntityServiceClasses(final String simpleName) {
		Check.notBlank(simpleName, "Entity type must not be empty");
		switch (simpleName) {
		case "CActivity":
			return CActivityService.class;
		case "CMeeting":
			return CMeetingService.class;
		case "COrder":
			return COrderService.class;
		case "CRisk":
			return CRiskService.class;
		case "CCompany":
			return CCompanyService.class;
		case "CProject":
			return CProjectService.class;
		case "CDecision":
			return CDecisionService.class;
		case "CUser":
			return CUserService.class;
		case "CActivityType":
			return CActivityTypeService.class;
		case "CRiskType":
			return CRiskTypeService.class;
		case "CProjectItemStatus":
			return CProjectItemStatusService.class;
		case "CActivityPriority":
			return CActivityPriorityService.class;
		case "CMeetingType":
			return CMeetingTypeService.class;
		case "CPageEntity":
			return CPageEntityService.class;
		case "CCommentPriority":
			return CCommentPriorityService.class;
		case "CCurrency":
			return CCurrencyService.class;
		case "CDecisionType":
			return CDecisionTypeService.class;
		case "COrderType":
			return COrderTypeService.class;
		case "CApprovalStatus":
			return CApprovalStatusService.class;
		case "CDetailSection":
			return CDetailSectionService.class;
		case "CGridEntity":
			return CGridEntityService.class;
		case "CSystemSettings":
			return CSystemSettingsService.class;
		case "CUserProjectRole":
			return CUserProjectRoleService.class;
		case "CUserCompanyRole":
			return CUserCompanyRoleService.class;
		case "CWorkflowEntity":
			return CWorkflowEntityService.class;
		// ... add more as needed ...
		default:
			LOGGER.error("Unknown entity type: " + simpleName + " dont forget to update CAuxillaries");
			throw new IllegalArgumentException("Unknown entity type: " + simpleName);
		}
	}

	public static Class<?> getInitializerService(final Class<?> entityClass) {
		if (entityClass == CActivity.class) {
			return CActivityInitializerService.class;
		} else if (entityClass == CMeeting.class) {
			return CMeetingInitializerService.class;
		} else if (entityClass == COrder.class) {
			return COrderInitializerService.class;
		} else if (entityClass == CRisk.class) {
			return CRiskInitializerService.class;
		} else if (entityClass == CCompany.class) {
			return CCompanyInitializerService.class;
		} else if (entityClass == CProject.class) {
			return CProjectInitializerService.class;
		} else if (entityClass == CDecision.class) {
			return CDecisionInitializerService.class;
		} else if (entityClass == CUser.class) {
			return CUserInitializerService.class;
		} else if (entityClass == CActivityType.class) {
			return CActivityTypeInitializerService.class;
		} else if (entityClass == CProjectItemStatus.class) {
			return CProjectItemStatusInitializerService.class;
		} else if (entityClass == CActivityPriority.class) {
			return CActivityPriorityInitializerService.class;
		} else if (entityClass == CMeetingType.class) {
			return CMeetingTypeInitializerService.class;
		} else if (entityClass == CPageEntity.class) {
			return CPageEntityInitializerService.class;
		} else if (entityClass == CCommentPriority.class) {
			return CCommentPriorityInitializerService.class;
		} else if (entityClass == CCurrency.class) {
			return CCurrencyInitializerService.class;
		} else if (entityClass == CDecisionType.class) {
			return CDecisionTypeInitializerService.class;
		} else if (entityClass == COrderType.class) {
			return COrderTypeInitializerService.class;
		} else if (entityClass == CApprovalStatus.class) {
			return CApprovalStatusInitializerService.class;
		} else if (entityClass == CDetailSection.class) {
			return CDetailSectionService.class;
		} else if (entityClass == CGridEntity.class) {
			return CGridEntityService.class;
		} else if (entityClass == CSystemSettings.class) {
			return CSystemSettingsInitializerService.class;
		} else if (entityClass == CUserProjectRole.class) {
			return CUserProjectRoleInitializerService.class;
		} else if (entityClass == CUserCompanyRole.class) {
			return CUserCompanyRoleInitializerService.class;
		} else if (entityClass == CWorkflowEntity.class) {
			return CWorkflowEntityService.class;
		} else {
			LOGGER.error("Unknown entity type: " + entityClass.getSimpleName() + " dont forget to update CAuxillaries");
			throw new IllegalArgumentException("Unknown entity type: " + entityClass.getSimpleName());
		}
	}

	public static Class<?> getInitializerService(final String entityType) {
		Check.notBlank(entityType, "Entity type must not be empty");
		Class<?> clazz = getEntityClass(entityType);
		return getInitializerService(clazz);
	}

	/** Get a method from a class without caching.
	 * @param clazz          the class containing the method
	 * @param methodName     the method name
	 * @param parameterTypes the parameter types (if any)
	 * @return the Method object or null if not found */
	public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
		try {
			Check.notNull(clazz, "clazz is null");
			Check.notBlank(methodName, "methodName is blank");
			final Method method = clazz.getMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return method;
		} catch (final NoSuchMethodException e) {
			LOGGER.info("Method not found: {}.{}", clazz.getSimpleName(), methodName);
			return null;
		}
	}

	public static Class<?> getServiceClassForEntity(final Class<?> entityClass) {
		if (entityClass == CActivity.class) {
			return CActivityService.class;
		} else if (entityClass == CMeeting.class) {
			return CMeetingService.class;
		} else if (entityClass == COrder.class) {
			return COrderService.class;
		} else if (entityClass == CRisk.class) {
			return CRiskService.class;
		} else if (entityClass == CCompany.class) {
			return CCompanyService.class;
		} else if (entityClass == CProject.class) {
			return CProjectService.class;
		} else if (entityClass == CDecision.class) {
			return CDecisionService.class;
		} else if (entityClass == CUser.class) {
			return CUserService.class;
		} else if (entityClass == CActivityType.class) {
			return CActivityTypeService.class;
		} else if (entityClass == CProjectItemStatus.class) {
			return CProjectItemStatusService.class;
		} else if (entityClass == CActivityPriority.class) {
			return CActivityPriorityService.class;
		} else if (entityClass == CMeetingType.class) {
			return CMeetingTypeService.class;
		} else if (entityClass == CPageEntity.class) {
			return CPageEntityService.class;
		} else if (entityClass == CCommentPriority.class) {
			return CCommentPriorityService.class;
		} else if (entityClass == CCurrency.class) {
			return CCurrencyService.class;
		} else if (entityClass == CDecisionType.class) {
			return CDecisionTypeService.class;
		} else if (entityClass == COrderType.class) {
			return COrderTypeService.class;
		} else if (entityClass == CApprovalStatus.class) {
			return CApprovalStatusService.class;
		} else if (entityClass == CDetailSection.class) {
			return CDetailSectionService.class;
		} else if (entityClass == CGridEntity.class) {
			return CGridEntityService.class;
		} else if (entityClass == CSystemSettings.class) {
			return CSystemSettingsService.class;
		} else if (entityClass == CUserProjectRole.class) {
			return CUserProjectRoleService.class;
		} else if (entityClass == CUserCompanyRole.class) {
			return CUserCompanyRoleService.class;
		} else if (entityClass == CWorkflowEntity.class) {
			return CWorkflowEntityService.class;
		} else {
			LOGGER.error("Unknown entity type: " + entityClass.getSimpleName() + " dont forget to update CAuxillaries");
			throw new IllegalArgumentException("Unknown entity type: " + entityClass.getSimpleName());
		}
	}

	public static Class<?> getServiceClassFromName(final String simpleName) {
		Check.notBlank(simpleName, "Entity type must not be empty");
		switch (simpleName) {
		case "CUserService":
			return CUserService.class;
		case "CActivityService":
			return CActivityService.class;
		case "CMeetingService":
			return CMeetingService.class;
		case "COrderService":
			return COrder.class;
		case "CRiskService":
			return CRiskService.class;
		case "CCompanyService":
			return CCompanyService.class;
		case "CProjectService":
			return CProjectService.class;
		case "CDecisionService":
			return CDecisionService.class;
		case "CActivityTypeService":
			return CActivityTypeService.class;
		case "CRiskTypeService":
			return CRiskTypeService.class;
		case "CProjectItemStatusService":
			return CProjectItemStatusService.class;
		case "CActivityPriorityService":
			return CActivityPriorityService.class;
		case "CMeetingTypeService":
			return CMeetingTypeService.class;
		case "CPageEntityService":
			return CPageEntityService.class;
		case "CCommentPriorityService":
			return CCommentPriorityService.class;
		case "CCurrencyService":
			return CCurrencyService.class;
		case "CDecisionTypeService":
			return CDecisionTypeService.class;
		case "COrderTypeService":
			return COrderTypeService.class;
		case "CApprovalStatusService":
			return CApprovalStatusService.class;
		case "CDetailSectionService":
			return CDetailSectionService.class;
		case "CGridEntityService":
			return CGridEntityService.class;
		case "CSystemSettingsService":
			return CSystemSettingsService.class;
		case "CUserProjectRoleService":
			return CUserProjectRoleService.class;
		case "CUserCompanyRoleService":
			return CUserCompanyRoleService.class;
		case "CWorkflowEntityService":
			return CWorkflowEntityService.class;
		default:
			LOGGER.error("Unknown service type: " + simpleName + " dont forget to update CAuxillaries");
			throw new IllegalArgumentException("Unknown service type: " + simpleName);
		}
	}

	public static Class<?> getViewClassForEntity(final String simpleName) {
		Check.notBlank(simpleName, "Entity type must not be empty");
		switch (simpleName) {
		case "CProjectGanntView":
			return CProjectGanntView.class;
		default:
			LOGGER.error("Unknown entity type: " + simpleName + " dont forget to update CAuxillaries");
			throw new IllegalArgumentException("Unknown entity type: " + simpleName);
		}
	}

	/** Safely invokes a method on an object.
	 * @param target     the target object
	 * @param methodName the method name
	 * @param args       the method arguments
	 * @return the method result or null if invocation failed
	 * @throws Exception */
	public static Object invokeMethod(final Object target, final String methodName, final Object... args) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(target, "target is null");
			Class<?>[] paramTypes = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
			}
			Method method = getMethod(target.getClass(), methodName, paramTypes);
			if (method != null) {
				return method.invoke(target, args);
			}
			method = getMethod(target.getClass(), methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + target.getClass().getName());
			return method.invoke(target);
		} catch (final Exception e) {
			LOGGER.error("Failed to invoke method {}.{}: {}", target.getClass().getSimpleName(), methodName, e.getMessage());
			throw e;
		}
	}

	public static String invokeMethodOfString(final Object entity, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(entity, "clazz is null");
			final Method method = getClazzMethod(entity.getClass(), methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + entity.getClass().getName());
			// check the method returns String
			Check.isTrue(method.getReturnType() == String.class,
					"Method " + methodName + " in class " + entity.getClass().getName() + " does not return String");
			// invoke the method and get the result
			final String result = (String) method.invoke(entity);
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error invoking method " + methodName + " of class " + entity.getClass().getName(), e);
			throw e;
		}
	}

	public static void invokeMethodOfVoid(final Class<?> clazz, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(clazz, "clazz is null");
			final Method method = getClazzMethod(clazz, methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + clazz.getName());
			if (method.getReturnType() != void.class) {
				throw new RuntimeException("Method " + methodName + " in class " + clazz.getName() + " does not return void");
			}
			method.invoke(null);
		} catch (final Exception e) {
			LOGGER.error("Error invoking method " + methodName + " of class " + clazz.getName(), e);
			throw e;
		}
	}

	public static List<String> invokeStaticMethodOfList(final String className, final String methodName) throws Exception {
		try {
			Check.notBlank(className, "className is blank");
			Check.notBlank(methodName, "methodName is blank");
			final Method method = getClazzMethodStatic(className, methodName);
			if (!List.class.isAssignableFrom(method.getReturnType())) {
				throw new IllegalArgumentException(
						"Method " + methodName + " in class " + className + " is not static or does not return List<String>");
			}
			@SuppressWarnings ("unchecked")
			final List<String> result = (List<String>) method.invoke(null);
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error invoking static method " + methodName + " of class " + className, e);
			throw e;
		}
	}

	public static String invokeStaticMethodOfStr(final Class<?> clazz, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(clazz, "clazz is null");
			final Method method = getClazzMethodStatic(clazz, methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + clazz.getName());
			if (method.getReturnType() != String.class) {
				throw new RuntimeException("Method " + methodName + " in class " + clazz.getName() + " does not return String");
			}
			return (String) method.invoke(null);
		} catch (final Exception e) {
			LOGGER.error("Error invoking static method " + methodName + " of class " + clazz.getName(), e);
			throw e;
		}
	}

	public static String invokeStaticMethodOfStr(final String className, final String methodName) throws Exception {
		try {
			Check.notBlank(className, "className is blank");
			Check.notBlank(methodName, "methodName is blank");
			final ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Check.notNull(cl, "ClassLoader is null");
			final Class<?> clazz = Class.forName(className, true, cl);
			Check.notNull(clazz, "Class " + className + " not found");
			return invokeStaticMethodOfStr(clazz, methodName);
		} catch (final Exception e) {
			LOGGER.error("Error invoking static method " + methodName + " of class " + className, e);
			throw e;
		}
	}

	public static void setId(final Component component) {
		Check.notNull(component, "component is null");
		final String id = generateId(component);
		component.setId(id);
	}
}
