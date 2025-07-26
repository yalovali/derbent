package tech.derbent.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Manual verification test to check if the fixes work:
 * 1. Company ComboBox should have data available
 * 2. Basic sorting should work with proper pagination
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class ManualVerificationTest {

    @Autowired
    private CUserService userService;
    
    @Autowired
    private CUserTypeService userTypeService;
    
    @Autowired
    private CCompanyService companyService;

    @Test
    public void testCompanyServiceAvailableForComboBox() {
        // Test that company service can provide data for ComboBox
        assertNotNull(companyService, "Company service should be available");
        
        // Create a test company
        CCompany testCompany = new CCompany("Test Company", "Test Description");
        testCompany.setEnabled(true);
        companyService.save(testCompany);
        
        // Verify that findEnabledCompanies returns the company
        var enabledCompanies = companyService.findEnabledCompanies();
        assertFalse(enabledCompanies.isEmpty(), "Should have at least one enabled company");
        assertTrue(enabledCompanies.stream().anyMatch(c -> "Test Company".equals(c.getName())), 
                  "Should contain the test company");
        
        System.out.println("✓ Company ComboBox fix verified: Companies are available for selection");
    }

    @Test
    public void testUserServicePaginationWorksWithSorting() {
        // Test that pagination works properly for grid sorting
        assertNotNull(userService, "User service should be available");
        
        // Create test users
        CUser user1 = new CUser("user1", "password", "Alice", "alice@test.com");
        CUser user2 = new CUser("user2", "password", "Bob", "bob@test.com");
        
        userService.save(user1);
        userService.save(user2);
        
        // Test pagination with sorting
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, 10, 
                org.springframework.data.domain.Sort.by("name"));
        
        var users = userService.list(pageable);
        assertFalse(users.isEmpty(), "Should have users");
        
        // Verify that sorting by name works (Alice should come before Bob)
        if (users.size() >= 2) {
            assertTrue(users.get(0).getName().compareTo(users.get(1).getName()) <= 0,
                      "Users should be sorted by name");
        }
        
        System.out.println("✓ Grid sorting fix verified: Pagination with sorting works correctly");
    }

    @Test 
    public void testUserWithCompanyRelationship() {
        // Test that user-company relationship works properly
        
        // Create a company
        CCompany company = new CCompany("Tech Corp");
        company.setEnabled(true);
        company = companyService.save(company);
        
        // Create a user with company
        CUser user = new CUser("testuser", "password", "Test", "test@test.com");
        user.setCompany(company);
        user = userService.save(user);
        
        // Verify the relationship
        assertNotNull(user.getCompany(), "User should have a company");
        assertEquals("Tech Corp", user.getCompany().getName(), "Company name should match");
        
        System.out.println("✓ User-Company relationship verified: Users can be assigned to companies");
    }
}