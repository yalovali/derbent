package tech.derbent.meetings.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.abstracts.interfaces.CKanbanType;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.meetings.view.CMeetingTypeView;
import tech.derbent.projects.domain.CProject;

/** CMeetingType - Domain entity representing meeting types. Layer: Domain (MVC) Inherits from CEntityOfProject to provide project-aware type
 * functionality for meetings. */
@Entity
@Table (name = "cmeetingtype")
@AttributeOverride (name = "id", column = @Column (name = "cmeetingtype_id"))
public class CMeetingType extends CTypeEntity<CMeetingType> implements CKanbanType {

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return "#28a745"; // Green color for meeting type entities
	}

	public static String getStaticIconFilename() { return "vaadin:tags"; }

	public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { return CMeetingTypeView.class; }

	/** Default constructor for JPA. */
	public CMeetingType() {
		super();
	}

	public CMeetingType(final String name, final CProject project) {
		super(CMeetingType.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends CAbstractEntityDBPage<?>> getViewClass() { // TODO Auto-generated method stub
		return CMeetingType.getViewClassStatic();
	}
}
