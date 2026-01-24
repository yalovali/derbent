package tech.derbent.plm.documenttypes.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

/** CDocumentType - Domain entity representing document type categories. Used to categorize attachments (e.g., Specification, Design Document, Meeting
 * Minutes, Test Report, etc.). Company-scoped to allow different companies to define their own document type taxonomies. Layer: Domain (MVC) */
@Entity
@Table (name = "cdocument_type")
@AttributeOverride (name = "id", column = @Column (name = "document_type_id"))
public class CDocumentType extends CTypeEntity<CDocumentType> {

	public static final String DEFAULT_COLOR = "#708090"; // Slate Gray - documentation
	public static final String DEFAULT_ICON = "vaadin:file-text-o";
	public static final String ENTITY_TITLE_PLURAL = "Document Types";
	public static final String ENTITY_TITLE_SINGULAR = "Document Type";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDocumentType.class);
	public static final String VIEW_NAME = "Document Types View";

	/** Default constructor for JPA. */
	public CDocumentType() {
		super();
		initializeDefaults();
	}

	/** Constructor with name and company.
	 * @param name    the document type name - must not be null or empty
	 * @param company the company this type belongs to */
	public CDocumentType(final String name, final CCompany company) {
		super(CDocumentType.class, name, company);
		initializeDefaults();
	}

	/** Constructor with name, company and description.
	 * @param name        the document type name - must not be null or empty
	 * @param company     the company this type belongs to
	 * @param description the description of this document type */
	public CDocumentType(final String name, final CCompany company, final String description) {
		super(CDocumentType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
