package tech.derbent.orders.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/**
 * Unit tests for COrder domain entity.
 * 
 * Tests basic functionality of the order management system including
 * entity creation, field validation, and approval management.
 */
class COrderTest {

    private COrder order;
    private CProject project;
    private CUser user;

    @BeforeEach
    void setUp() {
        project = new CProject();
        project.setName("Test Project");
        
        user = new CUser("test.user", "test123", "Test User", "test.user@example.com");
        
        order = new COrder("Test Order", project);
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
    void testOrderWithBasicFields() {
        // Set up order fields
        COrderType orderType = new COrderType("Service Order");
        COrderStatus status = new COrderStatus("Draft");
        CCurrency currency = new CCurrency("US Dollar", "USD", "$");
        
        order.setOrderType(orderType);
        order.setStatus(status);
        order.setCurrency(currency);
        order.setProviderCompanyName("Test Provider Corp");
        order.setRequestor(user);
        order.setResponsible(user);
        order.setEstimatedCost(new BigDecimal("1500.00"));
        
        // Verify fields are set correctly
        assertEquals(orderType, order.getOrderType());
        assertEquals(status, order.getStatus());
        assertEquals(currency, order.getCurrency());
        assertEquals("Test Provider Corp", order.getProviderCompanyName());
        assertEquals(user, order.getRequestor());
        assertEquals(user, order.getResponsible());
        assertEquals(new BigDecimal("1500.00"), order.getEstimatedCost());
    }

    @Test
    void testOrderApprovalManagement() {
        // Create approval
        CApprovalStatus approvalStatus = new CApprovalStatus("Pending");
        COrderApproval approval = new COrderApproval("Technical Approval", order);
        approval.setApprover(user);
        approval.setApprovalStatus(approvalStatus);
        approval.setApprovalLevel(1);
        
        // Add approval to order
        order.addApproval(approval);
        
        assertEquals(1, order.getApprovals().size());
        assertEquals(approval, order.getApprovals().get(0));
        assertEquals(order, approval.getOrder());
        
        // Remove approval
        order.removeApproval(approval);
        assertTrue(order.getApprovals().isEmpty());
        assertNull(approval.getOrder());
    }

    @Test
    void testOrderToString() {
        String orderString = order.toString();
        assertEquals("Test Order", orderString);
    }

    @Test
    void testProjectName() {
        assertEquals("Test Project", order.getProjectName());
        
        // Test with null project
        COrder orderWithoutProject = new COrder();
        assertEquals("No Project", orderWithoutProject.getProjectName());
    }

    @Test
    void testOrderDates() {
        LocalDate requireDate = LocalDate.now().plusDays(30);
        LocalDate deliveryDate = LocalDate.now().plusDays(25);
        
        order.setRequiredDate(requireDate);
        order.setDeliveryDate(deliveryDate);
        
        assertEquals(requireDate, order.getRequiredDate());
        assertEquals(deliveryDate, order.getDeliveryDate());
    }

    @Test
    void testFinancialFields() {
        BigDecimal estimatedCost = new BigDecimal("2500.50");
        BigDecimal actualCost = new BigDecimal("2750.75");
        
        order.setEstimatedCost(estimatedCost);
        order.setActualCost(actualCost);
        
        assertEquals(estimatedCost, order.getEstimatedCost());
        assertEquals(actualCost, order.getActualCost());
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

    @Test
    void testOrderNumber() {
        order.setOrderNumber("ORD-2025-001");
        assertEquals("ORD-2025-001", order.getOrderNumber());
    }

    @Test
    void testDeliveryAddress() {
        String address = "123 Main Street, Anytown, USA 12345";
        order.setDeliveryAddress(address);
        assertEquals(address, order.getDeliveryAddress());
    }
}