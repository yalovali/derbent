package tech.derbent.plm.storage.storageitem.service;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table(name = "cstorageitemtype", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "company_id" }))
@AttributeOverride(name = "id", column = @Column(name = "cstorageitemtype_id"))
public class CStorageItemType extends CTypeEntity<CStorageItemType> {

    public static final String DEFAULT_COLOR = "#20B2AA"; // X11 LightSeaGreen - item types
    public static final String DEFAULT_ICON = "vaadin:package";
    public static final String ENTITY_TITLE_PLURAL = "Storage Item Types";
    public static final String ENTITY_TITLE_SINGULAR = "Storage Item Type";
    public static final String VIEW_NAME = "Storage Item Type Management";

    /** Default constructor for JPA. */
    public CStorageItemType() {
        super();
    }

    public CStorageItemType(final String name, final CCompany company) {
        super(CStorageItemType.class, name, company);
    }
}
