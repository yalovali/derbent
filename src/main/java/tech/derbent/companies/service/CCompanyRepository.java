package tech.derbent.companies.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.companies.domain.CCompany;

/**
 * CCompanyRepository - Data access layer for CCompany entities
 * Layer: Service (MVC) - Repository interface
 * Extends CAbstractRepository to provide standard CRUD operations
 */
public interface CCompanyRepository extends CAbstractRepository<CCompany> {

    /**
     * Finds all enabled companies. Useful for filtering active companies only.
     * 
     * @param enabled true to find enabled companies, false for disabled companies
     * @return List of CCompany entities matching the enabled status
     */
    @Query("SELECT c FROM CCompany c WHERE c.enabled = :enabled ORDER BY c.name")
    List<CCompany> findByEnabled(@Param("enabled") boolean enabled);

    /**
     * Finds companies by name containing the search term (case-insensitive).
     * 
     * @param name the name search term
     * @return List of CCompany entities containing the search term in their name
     */
    @Query("SELECT c FROM CCompany c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.name")
    List<CCompany> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Finds a company by exact name match.
     * 
     * @param name the exact company name
     * @return Optional containing the CCompany if found, empty otherwise
     */
    @Query("SELECT c FROM CCompany c WHERE c.name = :name")
    Optional<CCompany> findByName(@Param("name") String name);

    /**
     * Finds companies by tax number.
     * 
     * @param taxNumber the tax identification number
     * @return Optional containing the CCompany if found, empty otherwise
     */
    @Query("SELECT c FROM CCompany c WHERE c.taxNumber = :taxNumber")
    Optional<CCompany> findByTaxNumber(@Param("taxNumber") String taxNumber);

    /**
     * Finds all companies ordered by name for listing purposes.
     * 
     * @return List of all CCompany entities ordered by name
     */
    @Query("SELECT c FROM CCompany c ORDER BY c.name")
    List<CCompany> findAllOrderByName();
}