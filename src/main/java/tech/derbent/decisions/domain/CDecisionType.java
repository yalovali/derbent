package tech.derbent.decisions.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.projects.domain.CProject;

/**
 * CDecisionType - Domain entity representing decision categorization types. Provides
 * classification for project decisions to support decision tracking and analysis. Layer:
 * Domain (MVC) Standard decision types: STRATEGIC, TACTICAL, OPERATIONAL, TECHNICAL,
 * BUDGET
 * @author Derbent Team
 * @since 1.0
 */
@Entity
@Table (name = "cdecisiontype")
@AttributeOverride (name = "id", column = @Column (name = "cdecisiontype_id"))
public class CDecisionType extends CTypeEntity {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionType.class);

	/**
	 * Default constructor for JPA.
	 */
	public CDecisionType() {
		super();
	}

	/**
	 * Constructor with required fields only.
	 * @param name    the name of the decision type (e.g., "STRATEGIC", "TECHNICAL")
	 * @param project the project this decision type belongs to
	 */
	public CDecisionType(final String name, final CProject project) {
		super(name, project);
	}

	/**
	 * Constructor with all common fields.
	 * @param name      the name of the decision type
	 * @param project   the project this decision type belongs to
	 * @param color     the hex color code for UI display
	 * @param sortOrder the display order
	 */
	public CDecisionType(final String name, final CProject project, final String color,
		final Integer sortOrder) {
		super(name, project);
		setColor(color);
		setSortOrder(sortOrder);
	}

	@Override
	public String toString() {
		return String.format(
			"CDecisionType{id=%d, name='%s', color='%s', sortOrder=%d, isActive=%s, project=%s}",
			getId(), getName(), getColor(), getSortOrder(), getIsActive(),
			getProject() != null ? getProject().getName() : "null");
	}
}