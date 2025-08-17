package unit_tests.tech.derbent.orders.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import tech.derbent.orders.domain.COrder;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Unit tests for COrder domain entity. Tests basic functionality of the order management system including entity
 * creation, field validation, and approval management.
 */
class COrderTest extends CTestBase {

    private COrder order;

    @Override
    protected void setupForTest() {
        order = new COrder("Test Order", project);
    }

    @Test
    void testDeliveryAddress() {
        final String address = "123 Main Street, Anytown, USA 12345";
        order.setDeliveryAddress(address);
        assertEquals(address, order.getDeliveryAddress());
    }

    @Test
    void testFinancialFields() {
        final BigDecimal estimatedCost = new BigDecimal("2500.50");
        final BigDecimal actualCost = new BigDecimal("2750.75");
        order.setEstimatedCost(estimatedCost);
        order.setActualCost(actualCost);
        assertEquals(estimatedCost, order.getEstimatedCost());
        assertEquals(actualCost, order.getActualCost());
    }

    @Test
    void testOrderCreation() {
        assertNotNull(order);
        assertEquals("Test Order", order.getName());
        assertEquals(project, order.getProject());
        assertEquals(LocalDate.now(), order.getOrderDate());
        assertNotNull(order.getApprovals());
        assertTrue(order.getApprovals().isEmpty());
    }

    @Test
    void testOrderDates() {
        final LocalDate requireDate = LocalDate.now().plusDays(30);
        final LocalDate deliveryDate = LocalDate.now().plusDays(25);
        order.setRequiredDate(requireDate);
        order.setDeliveryDate(deliveryDate);
        assertEquals(requireDate, order.getRequiredDate());
        assertEquals(deliveryDate, order.getDeliveryDate());
    }

    @Test
    void testOrderNumber() {
        order.setOrderNumber("ORD-2025-001");
        assertEquals("ORD-2025-001", order.getOrderNumber());
    }

    @Test
    void testOrderToString() {
        final String orderString = order.toString();
        assertEquals("Test Order", orderString);
    }

    @Test
    void testProviderInformation() {
        order.setProviderCompanyName("TechCorp Solutions");
        order.setProviderContactName("John Smith");
        order.setProviderEmail("john.smith@techcorp.com");
        assertEquals("TechCorp Solutions", order.getProviderCompanyName());
        assertEquals("John Smith", order.getProviderContactName());
        assertEquals("john.smith@techcorp.com", order.getProviderEmail());
    }
}