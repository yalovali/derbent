package tech.derbent.companies.domain;

import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CCompany - Domain entity representing companies within the organization.
 * Layer: Domain (MVC)
 * Inherits from CEntityDB to provide database functionality.
 */
@Entity
@Table(name = "ccompany") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "company_id")) // Override the default column name for the ID field
public class CCompany extends CEntityDB {

    @Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Company Name", required = true, readOnly = false, defaultValue = "", description = "Name of the company", hidden = false, order = 1, maxLength = MAX_LENGTH_NAME)
    private String name;

    @Column(name = "description", nullable = true, length = MAX_LENGTH_DESCRIPTION)
    @Size(max = MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Description", required = false, readOnly = false, defaultValue = "", description = "Company description", hidden = false, order = 2, maxLength = MAX_LENGTH_DESCRIPTION)
    private String description;

    @Column(name = "address", nullable = true, length = MAX_LENGTH_DESCRIPTION)
    @Size(max = MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Address", required = false, readOnly = false, defaultValue = "", description = "Company address", hidden = false, order = 3, maxLength = MAX_LENGTH_DESCRIPTION)
    private String address;

    @Column(name = "phone", nullable = true, length = MAX_LENGTH_NAME)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Phone", required = false, readOnly = false, defaultValue = "", description = "Company phone number", hidden = false, order = 4, maxLength = MAX_LENGTH_NAME)
    private String phone;

    @Column(name = "email", nullable = true, length = MAX_LENGTH_NAME)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Email", required = false, readOnly = false, defaultValue = "", description = "Company email address", hidden = false, order = 5, maxLength = MAX_LENGTH_NAME)
    private String email;

    @Column(name = "website", nullable = true, length = MAX_LENGTH_NAME)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Website", required = false, readOnly = false, defaultValue = "", description = "Company website URL", hidden = false, order = 6, maxLength = MAX_LENGTH_NAME)
    private String website;

    @Column(name = "tax_number", nullable = true, length = MAX_LENGTH_NAME)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Tax Number", required = false, readOnly = false, defaultValue = "", description = "Company tax identification number", hidden = false, order = 7, maxLength = MAX_LENGTH_NAME)
    private String taxNumber;

    @Column(name = "enabled", nullable = false)
    @MetaData(displayName = "Active", required = true, readOnly = false, defaultValue = "true", description = "Is company active?", hidden = false, order = 8)
    private boolean enabled = true;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MetaData(displayName = "Users", required = false, readOnly = true, description = "Users belonging to this company", hidden = false, order = 9)
    private List<tech.derbent.users.domain.CUser> users;

    public CCompany() {
        super();
    }

    public CCompany(final String name) {
        super();
        this.name = name;
    }

    public CCompany(final String name, final String description) {
        super();
        this.name = name;
        this.description = description;
    }

    // Getters and setters following the existing pattern

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(final String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public List<tech.derbent.users.domain.CUser> getUsers() {
        return users;
    }

    public void setUsers(final List<tech.derbent.users.domain.CUser> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "CCompany{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +
                ", taxNumber='" + taxNumber + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}