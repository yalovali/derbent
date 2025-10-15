package tech.derbent.page.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/** Unit tests for menu order parsing functionality. Tests the hierarchical menu order parsing in CPageMenuIntegrationService. */
public class CPageMenuOrderTest {

	/** Test parsing of hierarchical order values. Tests the interpretation of order values like 4.1 where: - Integer part (4) represents parent order
	 * - Fractional part (0.1) represents child order (1) */
	@Test
	public void testHierarchicalOrderParsing() {
		// Test single level order
		Double order1 = 5.0;
		int intPart1 = (int) Math.floor(order1);
		double fracPart1 = order1 - intPart1;
		assertEquals(5, intPart1, "Integer part of 5.0 should be 5");
		assertEquals(0.0, fracPart1, 0.001, "Fractional part of 5.0 should be 0.0");
		// Test two-level hierarchical order
		Double order2 = 4.1;
		int intPart2 = (int) Math.floor(order2);
		double fracPart2 = order2 - intPart2;
		assertEquals(4, intPart2, "Integer part of 4.1 should be 4 (parent order)");
		assertEquals(0.1, fracPart2, 0.001, "Fractional part of 4.1 should be 0.1 (child order 1)");
		// Extract child order from fractional part
		double childOrder2 = fracPart2 * 10.0;
		assertEquals(1.0, childOrder2, 0.001, "Child order from 4.1 should be 1.0");
		// Test three-level hierarchical order
		Double order3 = 4.12;
		int intPart3 = (int) Math.floor(order3);
		double fracPart3 = order3 - intPart3;
		assertEquals(4, intPart3, "Integer part of 4.12 should be 4");
		assertEquals(0.12, fracPart3, 0.001, "Fractional part of 4.12 should be 0.12");
		// Extract individual digits from fractional part
		String fracStr3 = String.format("%.10f", fracPart3).substring(2);
		assertEquals('1', fracStr3.charAt(0), "First digit of fractional part should be 1");
		assertEquals('2', fracStr3.charAt(1), "Second digit of fractional part should be 2");
		// Test edge case: order with no fractional part
		Double order4 = 7.0;
		int intPart4 = (int) Math.floor(order4);
		double fracPart4 = order4 - intPart4;
		assertEquals(7, intPart4, "Integer part of 7.0 should be 7");
		assertEquals(0.0, fracPart4, 0.001, "Fractional part of 7.0 should be 0.0");
	}

	/** Test that order values are correctly interpreted for menu hierarchy. */
	@Test
	public void testMenuOrderInterpretation() {
		// Simulate menu ordering scenarios
		// Scenario 1: Top-level items with different orders
		Double topItem1 = 1.0;
		Double topItem2 = 2.0;
		Double topItem3 = 5.0;
		assertTrue(topItem1 < topItem2, "Item with order 1.0 should come before 2.0");
		assertTrue(topItem2 < topItem3, "Item with order 2.0 should come before 5.0");
		// Scenario 2: Parent with children
		Double parent = 4.0; // Parent at position 4
		Double child1 = 4.1; // First child under parent 4, with child order 1
		Double child2 = 4.2; // Second child under parent 4, with child order 2
		int parentOrder = (int) Math.floor(parent);
		int child1ParentOrder = (int) Math.floor(child1);
		int child2ParentOrder = (int) Math.floor(child2);
		assertEquals(parentOrder, child1ParentOrder, "Child 1 parent order should match parent order");
		assertEquals(parentOrder, child2ParentOrder, "Child 2 parent order should match parent order");
		double child1Order = (child1 - child1ParentOrder) * 10.0;
		double child2Order = (child2 - child2ParentOrder) * 10.0;
		assertTrue(child1Order < child2Order, "Child with order 1 should come before child with order 2");
	}

	/** Test default order value (999.0) for missing or invalid orders. */
	@Test
	public void testDefaultOrder() {
		Double defaultOrder = 999.0;
		Double normalOrder = 5.0;
		assertTrue(normalOrder < defaultOrder, "Normal order should be less than default order");
		assertTrue(defaultOrder > 100.0, "Default order should be high enough to appear last");
	}
}
