package tech.derbent.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.security.service.CLoginService;

@SpringBootTest
@TestPropertySource(properties = "application.security.use-clogin=true")
class CLoginAuthenticationTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CLoginService loginService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testLoadUserByUsername() {
        // Test loading admin user
        UserDetails admin = userDetailsService.loadUserByUsername("admin");
        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
        assertTrue(admin.isEnabled());
        assertFalse(admin.getAuthorities().isEmpty());
        
        // Test that password matches
        assertTrue(passwordEncoder.matches("admin123", admin.getPassword()));
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void testLoginServiceFindByUsername() {
        var loginOpt = loginService.findByUsername("admin");
        assertTrue(loginOpt.isPresent());
        
        var login = loginOpt.get();
        assertEquals("admin", login.getUsername());
        assertTrue(passwordEncoder.matches("admin123", login.getPassword()));
    }
}