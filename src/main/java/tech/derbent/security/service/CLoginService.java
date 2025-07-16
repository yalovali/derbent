package tech.derbent.security.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.security.domain.CLogin;

@Service
@Transactional
public class CLoginService extends CAbstractService<CLogin> {

    private final PasswordEncoder passwordEncoder;

    public CLoginService(CLoginRepository repository, Clock clock, PasswordEncoder passwordEncoder) {
        super(repository, clock);
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Find a login by username.
     * 
     * @param username the username to search for
     * @return the login if found
     */
    @Transactional(readOnly = true)
    public Optional<CLogin> findByUsername(String username) {
        return ((CLoginRepository) repository).findByUsername(username);
    }

    /**
     * Check if a username exists.
     * 
     * @param username the username to check
     * @return true if username exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return ((CLoginRepository) repository).existsByUsername(username);
    }

    /**
     * Create a new login with encoded password.
     * 
     * @param username the username
     * @param rawPassword the raw password (will be encoded)
     * @param roles the roles (comma separated)
     * @return the saved login
     */
    public CLogin createLogin(String username, String rawPassword, String roles) {
        CLogin login = new CLogin();
        login.setUsername(username);
        login.setPassword(passwordEncoder.encode(rawPassword));
        login.setRoles(roles);
        login.setEnabled(true);
        return (CLogin) save(login);
    }

    /**
     * Update password for a login.
     * 
     * @param login the login to update
     * @param rawPassword the new raw password (will be encoded)
     */
    public void updatePassword(CLogin login, String rawPassword) {
        login.setPassword(passwordEncoder.encode(rawPassword));
        save(login);
    }
}