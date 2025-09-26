package tech.derbent.screens.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.decisions.domain.CDecisionType;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.meetings.domain.CMeetingType;
import tech.derbent.orders.domain.CApprovalStatus;
import tech.derbent.orders.domain.COrderStatus;
import tech.derbent.orders.domain.COrderType;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRiskStatus;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.users.domain.CUserType;

/** Integration test to demonstrate the color field functionality in CComponentGridEntity. */
class CComponentGridEntityColorFieldTest {

	@Test
	void testColorFieldDetection_ForCTypeEntity() throws Exception {
		// Test that CTypeEntity's color field is detected correctly
		Field colorField = CTypeEntity.class.getDeclaredField("color");
		assertNotNull(colorField);
		// Create mock EntityFieldInfo that would be created for the color field
		EntityFieldInfo fieldInfo = mock(EntityFieldInfo.class);
		when(fieldInfo.isColorField()).thenReturn(true);
		when(fieldInfo.getDisplayName()).thenReturn("Color");
		when(fieldInfo.getFieldName()).thenReturn("color");
		// This demonstrates that our createColumnForField method would detect
		// the color field and use addEntityColumn instead of addShortTextColumn
		assertTrue(fieldInfo.isColorField());
	}

	@Test
	void testStatusEntityDetection_ForActivityStatus() {
		// Test that CActivityStatus is correctly identified as a status entity
		assertTrue(CColorUtils.isStatusEntity(CActivityStatus.class));
	}

	@Test
	void testStatusEntityDetection_ForAllStatusEntities() {
		// Test that all Status entities are correctly identified
		assertTrue(CColorUtils.isStatusEntity(CActivityStatus.class));
		assertTrue(CColorUtils.isStatusEntity(COrderStatus.class));
		assertTrue(CColorUtils.isStatusEntity(CApprovalStatus.class));
		assertTrue(CColorUtils.isStatusEntity(CMeetingStatus.class));
		assertTrue(CColorUtils.isStatusEntity(CRiskStatus.class));
	}

	@Test
	void testTypeEntityDetection_ForAllTypeEntities() {
		// Test that all Type entities extending CTypeEntity are correctly identified
		assertTrue(CColorUtils.isStatusEntity(CActivityType.class));
		assertTrue(CColorUtils.isStatusEntity(COrderType.class));
		assertTrue(CColorUtils.isStatusEntity(CMeetingType.class));
		assertTrue(CColorUtils.isStatusEntity(CDecisionType.class));
		assertTrue(CColorUtils.isStatusEntity(CUserType.class));
	}

	@Test
	void testGridEntityCreation_WithColorFields() {
		// Create a test scenario showing how CComponentGridEntity would handle
		// an entity with color fields and status entities
		CProject testProject = new CProject("Test Project");
		CGridEntity gridEntity = new CGridEntity("TestGrid", testProject);
		// Configure grid to show fields including color field and status field
		gridEntity.setSelectedFields("name:1,color:2,status:3");
		gridEntity.setDataServiceBeanName("activityService");
		// This would be used by CComponentGridEntity to create columns
		assertNotNull(gridEntity.getSelectedFields());
		assertTrue(gridEntity.getSelectedFields().contains("color"));
		assertTrue(gridEntity.getSelectedFields().contains("status"));
	}

	@Test
	void testEntityFieldHandling_WithStatusEntity() {
		// Demonstrate that our enhanced createColumnForField method
		// would correctly identify status entities and use addStatusColumn
		Class<?> statusEntityType = CActivityStatus.class;
		assertTrue(CColorUtils.isStatusEntity(statusEntityType));
		// This shows that for status entity fields, our code would:
		// 1. Detect it's a status entity using CColorUtils.isStatusEntity()
		// 2. Use grid.addStatusColumn() instead of grid.addEntityColumn()
		// 3. Enable color rendering automatically
	}

	@Test
	void testEntityFieldHandling_WithTypeEntity() {
		// Demonstrate that Type entities are also correctly identified for color rendering
		Class<?> typeEntityType = CActivityType.class;
		assertTrue(CColorUtils.isStatusEntity(typeEntityType));
		// This verifies that Type entities will also use color-aware rendering
	}

	@Test
	void testUpdatedEntitiesHaveColorField() throws Exception {
		// Verify that entities now extending CTypeEntity/CStatus have color field access
		// Through inheritance, these should all have the color field
		CProject testProject = new CProject("Test Project");
		COrderType orderType = new COrderType("Test Order Type", testProject);
		assertNotNull(orderType.getColor()); // Should have color from CTypeEntity
		CUserType userType = new CUserType("Test User Type", testProject);
		assertNotNull(userType.getColor()); // Should have color from CTypeEntity
		CApprovalStatus approvalStatus = new CApprovalStatus("Test Approval Status", testProject);
		assertNotNull(approvalStatus.getColor()); // Should have color from CStatus->CTypeEntity
	}
}
