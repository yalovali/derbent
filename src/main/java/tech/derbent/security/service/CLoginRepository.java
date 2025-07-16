package tech.derbent.security.service;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.security.domain.CLogin;

@Repository
public interface CLoginRepository extends CAbstractRepository<CLogin> {
    
    /**
     * Find a login by username.
     * 
     * @param username the username to search for
     * @return the login if found
     */
    Optional<CLogin> findByUsername(String username);
    
    /**
     * Check if a username exists.
     * 
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);
}