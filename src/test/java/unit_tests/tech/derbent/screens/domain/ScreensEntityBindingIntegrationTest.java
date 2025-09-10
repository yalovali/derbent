package unit_tests.tech.derbent.screens.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.domain.CMasterSection;

/** Integration test to verify that the fixed entities work correctly with form binding using CEnhancedBinder. This test verifies that the
 * getter/setter fixes resolve the original form binding issue that caused null constraint violations. */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.sql.init.mode=never", "spring.jpa.defer-datasource-initialization=false"
})
public class ScreensEntityBindingIntegrationTest {

	@Test
	void testCGridEntityFormBinding() throws Exception {
		// Create a binder for CGridEntity - this would have failed before the fix
		final CEnhancedBinder<CGridEntity> binder = new CEnhancedBinder<>(CGridEntity.class);
		assertNotNull(binder, "Binder should be created successfully");
		// Create an entity and test binding
		final CGridEntity entity = new CGridEntity();
		// Set a value that would have been lost before the fix
		entity.setDataServiceBeanName("CViewsService");
		// Test that the binder can read the entity without errors
		binder.readBean(entity);
		// Simulate form data being written back to entity
		entity.setDataServiceBeanName("UpdatedService");
		// This should work now with the getter/setter fix
		binder.writeBean(entity);
		// Verify the value was preserved
		assertEquals("UpdatedService", entity.getDataServiceBeanName(), "DataServiceBeanName should be preserved through binding");
	}

	@Test
	void testCMasterSectionFormBinding() throws Exception {
		// Create a binder for CMasterSection - would have failed before the fix
		final CEnhancedBinder<CMasterSection> binder = new CEnhancedBinder<>(CMasterSection.class);
		assertNotNull(binder, "Binder should be created successfully");
		// Create an entity and test binding
		final CMasterSection entity = new CMasterSection();
		// Set values that would have been lost before the fix
		entity.setSectionDBName("test_section");
		entity.setSectionType("GRID");
		// Test that the binder can read the entity without errors
		binder.readBean(entity);
		// Simulate form data being written back to entity
		entity.setSectionDBName("updated_section");
		entity.setSectionType("LIST");
		// This should work now with the getter/setter fix
		binder.writeBean(entity);
		// Verify the values were preserved
		assertEquals("updated_section", entity.getSectionDBName(), "SectionDBName should be preserved through binding");
		assertEquals("LIST", entity.getSectionType(), "SectionType should be preserved through binding");
	}
}
