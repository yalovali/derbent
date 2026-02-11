package tech.derbent.api.setup;

import org.junit.jupiter.api.Test;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * Test class to verify LDAP connection using the same configuration as your working ldapsearch command.
 * 
 * Your working command:
 * ldapsearch -x -H ldap://dc:389 -D "CN=ldap,CN=Users,DC=ECEMTAG,DC=LOCAL" -w "ysn605ysn" -b "cn=Users,dc=ECEMTAG,dc=LOCAL" "(sAMAccountName=*)"
 */
public class LdapConnectionTest {

	@Test
	public void testLdapConnection() {
		// Configuration based on your working ldapsearch command
		final String ldapUrl = "ldap://dc:389";
		final String ldapUser = "CN=ldap,CN=Users,DC=ECEMTAG,DC=LOCAL";  // -D parameter
		final String ldapPassword = "ysn605ysn";                         // -w parameter
		final String ldapSearchBase = "cn=Users,dc=ECEMTAG,dc=LOCAL";     // -b parameter
		final String ldapUserFilter = "(sAMAccountName=*)";               // Filter parameter
		
		System.out.println("=== LDAP Connection Test ===");
		System.out.println("LDAP URL: " + ldapUrl);
		System.out.println("LDAP User: " + ldapUser);
		System.out.println("LDAP Search Base: " + ldapSearchBase);
		System.out.println("LDAP User Filter: " + ldapUserFilter);
		
		// Create LDAP environment
		final Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ldapUser);
		env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
		env.put("com.sun.jndi.ldap.connect.timeout", "5000");
		env.put("com.sun.jndi.ldap.read.timeout", "10000");
		
		DirContext ldapContext = null;
		try {
			// Connect to LDAP
			System.out.println("\n=== Attempting LDAP Connection ===");
			ldapContext = new InitialDirContext(env);
			System.out.println("✅ LDAP connection successful!");
			
			// Search for users
			System.out.println("\n=== Searching for Users ===");
			final SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchControls.setReturningAttributes(new String[]{"sAMAccountName", "cn", "mail", "displayName"});
			searchControls.setCountLimit(10); // Limit results for test
			
			final NamingEnumeration<SearchResult> results = ldapContext.search(ldapSearchBase, ldapUserFilter, searchControls);
			
			int userCount = 0;
			while (results.hasMore()) {
				final SearchResult searchResult = results.next();
				final Attributes attributes = searchResult.getAttributes();
				
				final String dn = searchResult.getNameInNamespace();
				final String sAMAccountName = getAttributeValue(attributes, "sAMAccountName");
				final String cn = getAttributeValue(attributes, "cn");
				final String mail = getAttributeValue(attributes, "mail");
				final String displayName = getAttributeValue(attributes, "displayName");
				
				System.out.println(String.format("User %d:", ++userCount));
				System.out.println("  DN: " + dn);
				System.out.println("  sAMAccountName: " + sAMAccountName);
				System.out.println("  CN: " + cn);
				System.out.println("  Mail: " + mail);
				System.out.println("  Display Name: " + displayName);
				System.out.println();
				
				if (userCount >= 5) break; // Limit output for readability
			}
			
			System.out.println("✅ Found " + userCount + " users (showing first 5)");
			
			// Test specific user lookup
			System.out.println("\n=== Testing User Authentication Pattern ===");
			final String testUsername = "Administrator";
			final String userSpecificFilter = "(sAMAccountName=" + testUsername + ")";
			
			final NamingEnumeration<SearchResult> userResults = ldapContext.search(ldapSearchBase, userSpecificFilter, searchControls);
			if (userResults.hasMore()) {
				final SearchResult userResult = userResults.next();
				final String userDN = userResult.getNameInNamespace();
				System.out.println("✅ Found user '" + testUsername + "' with DN: " + userDN);
			} else {
				System.out.println("❌ User '" + testUsername + "' not found");
			}
			
		} catch (NamingException e) {
			System.out.println("❌ LDAP Error: " + e.getMessage());
			e.printStackTrace();
			
			// Provide debugging info
			System.out.println("\n=== Debugging Info ===");
			System.out.println("Check if:");
			System.out.println("1. LDAP server 'dc:389' is reachable");
			System.out.println("2. User credentials are correct: " + ldapUser);
			System.out.println("3. Search base exists: " + ldapSearchBase);
			System.out.println("4. No firewall blocking port 389");
			
		} catch (Exception e) {
			System.out.println("❌ Unexpected error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (ldapContext != null) {
				try {
					ldapContext.close();
					System.out.println("✅ LDAP connection closed");
				} catch (NamingException e) {
					System.out.println("⚠️ Error closing LDAP connection: " + e.getMessage());
				}
			}
		}
	}
	
	private String getAttributeValue(final Attributes attributes, final String attributeName) {
		try {
			if (attributes.get(attributeName) != null) {
				return attributes.get(attributeName).get().toString();
			}
		} catch (NamingException e) {
			// Ignore
		}
		return "";
	}
}