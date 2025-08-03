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
import tech.derbent.abstracts.domains.CEntityConstants;
import tech.derbent.abstracts.domains.CEntityNamed;

/**
 * CCompany - Domain entity representing companies within the organization. Layer: Domain (MVC) Inherits from CEntityDB
 * to provide database functionality.
 */
@Entity
@Table(name = "ccompany") // table name for the entity as the default is the class name
// in lowercase
@AttributeOverride(name = "id", column = @Column(name = "company_id"))
public class CCompany extends CEntityNamed<CCompany> {
    // name and description fields are now inherited from CEntityNamed

    @Column(name = "address", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    @Size(max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Address", required = false, readOnly = false, defaultValue = "", description = "Company address", hidden = false, order = 3, maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION)
    private String address;

    @Column(name = "phone", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
    @Size(max = CEntityConstants.MAX_LENGTH_NAME)
    @MetaData(displayName = "Phone", required = false, readOnly = false, defaultValue = "", description = "Company phone number", hidden = false, order = 4, maxLength = CEntityConstants.MAX_LENGTH_NAME)
    private String phone;

    @Column(name = "email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
    @Size(max = CEntityConstants.MAX_LENGTH_NAME)
    @MetaData(displayName = "Email", required = false, readOnly = false, defaultValue = "", description = "Company email address", hidden = false, order = 5, maxLength = CEntityConstants.MAX_LENGTH_NAME)
    private String email;

    @Column(name = "website", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
    @Size(max = CEntityConstants.MAX_LENGTH_NAME)
    @MetaData(displayName = "Website", required = false, readOnly = false, defaultValue = "", description = "Company website URL", hidden = false, order = 6, maxLength = CEntityConstants.MAX_LENGTH_NAME)
    private String website;

    @Column(name = "tax_number", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
    @Size(max = CEntityConstants.MAX_LENGTH_NAME)
    @MetaData(displayName = "Tax Number", required = false, readOnly = false, defaultValue = "", description = "Company tax identification number", hidden = false, order = 7, maxLength = CEntityConstants.MAX_LENGTH_NAME)
    private String taxNumber;

    @Column(name = "enabled", nullable = false)
    @MetaData(displayName = "Active", required = true, readOnly = false, defaultValue = "true", description = "Is company active?", hidden = false, order = 8)
    private Boolean enabled = Boolean.TRUE;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MetaData(displayName = "Users", required = false, readOnly = true, description = "Users belonging to this company", hidden = false, order = 9)
    private List<tech.derbent.users.domain.CUser> users;

    /**
     * Default constructor for JPA.
     */
    public CCompany() {
        super();
        // Initialize with default values for JPA
        this.enabled = true;
    }

    public CCompany(final String name) {
        super(CCompany.class, name); // Use the CEntityNamed constructor
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public List<tech.derbent.users.domain.CUser> getUsers() {
        return users;
    }

    public String getWebsite() {
        return website;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public void setTaxNumber(final String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public void setUsers(final List<tech.derbent.users.domain.CUser> users) {
        this.users = users;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "CCompany{" + "name='" + getName() + '\'' + ", description='" + getDescription() + '\'' + ", address='"
                + address + '\'' + ", phone='" + phone + '\'' + ", email='" + email + '\'' + ", website='" + website
                + '\'' + ", taxNumber='" + taxNumber + '\'' + ", enabled=" + enabled + '}';
    }
}