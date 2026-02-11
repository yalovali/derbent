package tech.derbent.test;

import tech.derbent.api.authentication.service.CLdapAuthenticator;
import tech.derbent.api.setup.domain.CSystemSettings;

/**
 * Simple test to verify LDAP connection with the provided settings
 * Tests the CLdapAuthenticator directly without Spring context
 */
public class TestLdapConnection {
    
    // Create a simple settings implementation for testing
    private static class TestSettings extends CSystemSettings<TestSettings> {
        
        protected TestSettings() {
            super();
            // JPA constructor - don't call initializeDefaults
        }
        
        @Override
        public Class<TestSettings> getEntityClass() {
            return TestSettings.class;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing LDAP Connection with provided settings...");
        
        // Create system settings with the provided LDAP values
        TestSettings settings = new TestSettings();
        
        // Set the LDAP configuration based on provided values
        settings.setEnableLdapAuthentication(true);
        settings.setLdapServerUrl("ldap://dc:389");
        settings.setLdapBindDn("cn=Users,dc=ECEMTAG,dc=LOCAL"); 
        settings.setLdapSearchBase("CN=ldap,CN=Users,DC=ECEMTAG,DC=LOCAL");
        settings.setLdapBindPassword("ysn605ysn");
        settings.setLdapUserFilter("sAMAccountName={0}");
        settings.setLdapVersion(3);
        settings.setLdapUseSslTls(false);
        
        // Create authenticator and test
        CLdapAuthenticator authenticator = new CLdapAuthenticator();
        
        System.out.println("\n=== LDAP Configuration ===");
        System.out.println("Server: " + settings.getLdapServerUrl());
        System.out.println("Base DN: " + settings.getLdapBindDn());
        System.out.println("Search Base: " + settings.getLdapSearchBase());
        System.out.println("User Filter: " + settings.getLdapUserFilter());
        System.out.println("Version: " + settings.getLdapVersion());
        System.out.println("Use SSL/TLS: " + settings.getLdapUseSslTls());
        
        // Test 1: Connection test
        System.out.println("\n=== Test 1: Connection Test ===");
        try {
            CLdapAuthenticator.CLdapTestResult connectionResult = authenticator.testConnection(settings);
            System.out.println(connectionResult.toString());
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 2: Fetch users
        System.out.println("\n=== Test 2: Fetch Users ===");
        try {
            CLdapAuthenticator.CLdapTestResult usersResult = authenticator.fetchAllUsers(settings);
            System.out.println(usersResult.toString());
            
            if (usersResult.isSuccess() && !usersResult.getUserData().isEmpty()) {
                System.out.println("\nFound Users:");
                usersResult.getUserData().forEach(user -> 
                    System.out.println("  - " + user));
            }
        } catch (Exception e) {
            System.err.println("User fetch test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 3: Authentication test (if specific credentials provided)
        if (args.length >= 2) {
            String username = args[0];
            String password = args[1];
            
            System.out.println("\n=== Test 3: Authentication Test ===");
            System.out.println("Testing credentials for user: " + username);
            
            try {
                CLdapAuthenticator.CLdapTestResult authResult = 
                    authenticator.testUserAuthentication(username, password, settings);
                System.out.println(authResult.toString());
            } catch (Exception e) {
                System.err.println("Authentication test failed: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("\n=== Test 3: Authentication Test ===");
            System.out.println("Skipped - provide username and password as arguments to test authentication");
            System.out.println("Usage: java TestLdapConnection <username> <password>");
        }
        
        System.out.println("\n✅ LDAP testing completed!");
    }
}