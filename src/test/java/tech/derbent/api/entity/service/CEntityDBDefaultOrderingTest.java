package tech.derbent.api.entity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;

/** Unit tests for the default ordering functionality in entities. Tests that getDefaultOrderBy() returns appropriate values and that services use
 * these for ordering. */
@SuppressWarnings ("static-method")
class CEntityDBDefaultOrderingTest {

	/** Test entity that overrides default ordering to use a custom field. */
	private static class TestCustomOrderEntity extends CEntityDB<TestCustomOrderEntity> {

		public TestCustomOrderEntity() {
			super();
		}

		@Override
		public String getDefaultOrderBy() { return "createDate"; }
	}

	/** Test entity extending CEntityDB for testing purposes. */
	private static class TestEntity extends CEntityDB<TestEntity> {

		public TestEntity() {
			super();
		}
	}

	/** Test named entity extending CEntityNamed for testing purposes. */
	private static class TestNamedEntity extends CEntityNamed<TestNamedEntity> {

		public TestNamedEntity(final String name) {
			super(TestNamedEntity.class, name);
		}
	}

	@Test
	void testCEntityDB_defaultOrderByReturnsId() {
		final TestEntity entity = new TestEntity();
		final String orderField = entity.getDefaultOrderBy();
		assertNotNull(orderField, "Default order field should not be null");
		assertEquals("id", orderField, "CEntityDB should order by 'id' by default");
	}

	@Test
	void testCEntityNamed_defaultOrderByReturnsName() {
		final TestNamedEntity entity = new TestNamedEntity("Test");
		final String orderField = entity.getDefaultOrderBy();
		assertNotNull(orderField, "Default order field should not be null");
		assertEquals("name", orderField, "CEntityNamed should order by 'name' by default");
	}

	@Test
	void testCustomEntity_canOverrideDefaultOrder() {
		final TestCustomOrderEntity entity = new TestCustomOrderEntity();
		final String orderField = entity.getDefaultOrderBy();
		assertNotNull(orderField, "Default order field should not be null");
		assertEquals("createDate", orderField, "Custom entity should order by 'createDate'");
	}

	@Test
	void testSort_canBeCreatedFromDefaultOrder() {
		final TestEntity entity = new TestEntity();
		final String orderField = entity.getDefaultOrderBy();
		// Verify we can create a Sort object from the default order field
		final Sort sort = Sort.by(Sort.Direction.DESC, orderField);
		assertNotNull(sort, "Sort should be created successfully");
		assertEquals(1, sort.stream().count(), "Sort should have exactly one order");
		sort.stream().forEach(order -> {
			assertEquals("id", order.getProperty(), "Sort should be for 'id' field");
			assertEquals(Sort.Direction.DESC, order.getDirection(), "Sort should be descending");
		});
	}
}
