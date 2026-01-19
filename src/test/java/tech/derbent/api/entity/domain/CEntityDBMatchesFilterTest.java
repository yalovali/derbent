package tech.derbent.api.entity.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.domain.CProject_Derbent;
import tech.derbent.base.users.domain.CUser;

/** Unit tests for the hierarchical matchesFilter() functionality across the entity hierarchy. */
class CEntityDBMatchesFilterTest {

	// Test entity classes
	private static class TestEntity extends CEntityDB<TestEntity> {

		public TestEntity() {
			super(TestEntity.class);
		}
	}

	private static class TestNamedEntity extends CEntityNamed<TestNamedEntity> {

		public TestNamedEntity(final String name) {
			super(TestNamedEntity.class, name);
		}
	}

	private static class TestProjectEntity extends CEntityOfProject<TestProjectEntity> {

		public TestProjectEntity(final String name, final CProject<?> project) {
			super(TestProjectEntity.class, name, project);
		}
	}

	private static class TestProjectItemEntity extends CProjectItem<TestProjectItemEntity> {

		public TestProjectItemEntity(final String name, final CProject<?> project) {
			super(TestProjectItemEntity.class, name, project);
		}
	}

	private TestEntity testEntity;
	private TestNamedEntity testNamedEntity;
	private TestProjectEntity testProjectEntity;
	private TestProjectItemEntity testProjectItemEntity;

	@BeforeEach
	void setUp() {
		testEntity = new TestEntity();
		testNamedEntity = new TestNamedEntity("Test Name");
		testProjectEntity = new TestProjectEntity("Project Entity", new CProject_Derbent());
		testProjectItemEntity = new TestProjectItemEntity("Item Entity", new CProject_Derbent());
	}

	@Test
	void testCaseInsensitiveMatching() {
		testNamedEntity.setName("Test Name");
		assertTrue(testNamedEntity.matchesFilter("TEST", List.of("name")), "Should match uppercase");
		assertTrue(testNamedEntity.matchesFilter("test", List.of("name")), "Should match lowercase");
		assertTrue(testNamedEntity.matchesFilter("TeSt", List.of("name")), "Should match mixed case");
	}

	@Test
	void testCEntityDB_matchesActive() {
		testEntity.setActive(true);
		assertTrue(testEntity.matchesFilter("true", List.of("active")), "Should match active=true");
		assertFalse(testEntity.matchesFilter("false", List.of("active")), "Should not match when active=true");
	}

	@Test
	void testCEntityDB_matchesId() {
		testEntity.id = 123L;
		assertTrue(testEntity.matchesFilter("123", List.of("id")), "Should match ID");
		assertFalse(testEntity.matchesFilter("456", List.of("id")), "Should not match different ID");
	}

	@Test
	void testCEntityNamed_defaultToNameWhenNoFieldsSpecified() {
		assertTrue(testNamedEntity.matchesFilter("Test", null), "Should match name when no fields specified");
		assertTrue(testNamedEntity.matchesFilter("Test", List.of()), "Should match name when empty field list");
	}

	@Test
	void testCEntityNamed_matchesDescription() {
		testNamedEntity.setDescription("This is a test description");
		assertTrue(testNamedEntity.matchesFilter("test description", List.of("description")), "Should match description");
		assertFalse(testNamedEntity.matchesFilter("other text", List.of("description")), "Should not match when text not in description");
	}

	@Test
	void testCEntityNamed_matchesName() {
		assertTrue(testNamedEntity.matchesFilter("Test", List.of("name")), "Should match name");
		assertTrue(testNamedEntity.matchesFilter("test", List.of("name")), "Should match name case-insensitive");
		assertFalse(testNamedEntity.matchesFilter("Other", List.of("name")), "Should not match different name");
	}

	@Test
	void testCEntityNamed_multipleFields() {
		testNamedEntity.setDescription("Description text");
		assertTrue(testNamedEntity.matchesFilter("Test", List.of("name", "description")), "Should match when search text in name");
		assertTrue(testNamedEntity.matchesFilter("Description", List.of("name", "description")), "Should match when search text in description");
		assertFalse(testNamedEntity.matchesFilter("NotFound", List.of("name", "description")), "Should not match when text not in any field");
	}

	@Test
	void testCEntityOfProject_matchesAssignedToName() {
		final CUser user = new CUser();
		user.setName("John Doe");
		testProjectEntity.setAssignedTo(user);
		assertTrue(testProjectEntity.matchesFilter("John", List.of("assignedTo")), "Should match assignedTo name");
		assertFalse(testProjectEntity.matchesFilter("Jane", List.of("assignedTo")), "Should not match different user name");
	}

	@Test
	void testCEntityOfProject_matchesProjectName() {
		final CProject_Derbent project = new CProject_Derbent();
		project.setName("Test Project");
		testProjectEntity.setProject(project);
		assertTrue(testProjectEntity.matchesFilter("Test Project", List.of("project")), "Should match project name");
		assertFalse(testProjectEntity.matchesFilter("Other Project", List.of("project")), "Should not match different project name");
	}

	@Test
	void testCProjectItem_matchesStatusName() {
		final CProjectItemStatus status = new CProjectItemStatus();
		status.setName("In Progress");
		testProjectItemEntity.setStatus(status);
		assertTrue(testProjectItemEntity.matchesFilter("In Progress", List.of("status")), "Should match status name");
		assertFalse(testProjectItemEntity.matchesFilter("Done", List.of("status")), "Should not match different status name");
	}

	@Test
	void testEmptyOrNullSearchValue() {
		assertTrue(testNamedEntity.matchesFilter(null, List.of("name")), "Null search should match all");
		assertTrue(testNamedEntity.matchesFilter("", List.of("name")), "Empty search should match all");
		assertTrue(testNamedEntity.matchesFilter("   ", List.of("name")), "Blank search should match all");
	}
}
