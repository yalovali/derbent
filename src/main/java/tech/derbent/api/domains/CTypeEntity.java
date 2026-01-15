package tech.derbent.api.domains;

import java.util.Arrays;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.IHasColor;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.workflow.domain.CWorkflowEntity;

/** CTypeEntity - Abstract base class for all type entities in the system. Provides common fields for type management including color, sort order, and
 * active status. Layer: Domain (MVC)
 * @author Derbent Team
 * @since 1.0 */
@MappedSuperclass
public abstract class CTypeEntity<EntityClass> extends CEntityOfCompany<EntityClass> implements IHasColor {

	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this type entity cannot be deleted by users (system configuration)", hidden = false
	)
	private boolean attributeNonDeletable = true;
	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = "#4A90E2", colorField = true,
			description = "Hex color code for type visualization (e.g., #4A90E2)", hidden = false, maxLength = 7
	)
	private String color = "#4A90E2";
	@Column (name = "sort_order", nullable = false)
	@NotNull
	@AMetaData (
			displayName = "Sort Order", required = true, readOnly = false, defaultValue = "100", description = "Display order for type sorting",
			hidden = false
	)
	private Integer sortOrder = 100;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "workflow_id", nullable = true)
	@AMetaData (
			displayName = "Workflow", required = false, readOnly = false, description = "Workflow for this type", hidden = false,
			dataProviderBean = "CWorkflowEntityService"
	)
	private CWorkflowEntity workflow;
	
	// Parent-Child Hierarchy Configuration
	@Column (name = "can_have_children", nullable = false)
	@AMetaData (
			displayName = "Can Have Children", required = false, readOnly = false, defaultValue = "true",
			description = "Whether items of this type can have child items", hidden = false
	)
	private boolean canHaveChildren = true;
	
	@Column (name = "parent_level1_entity_class", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Level 1 Entity Class", required = false, readOnly = false,
			description = "Entity class name for level 1 parent (e.g., Epic level)", hidden = false, maxLength = 100
	)
	private String parentLevel1EntityClass;
	
	@Column (name = "parent_level2_entity_class", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Level 2 Entity Class", required = false, readOnly = false,
			description = "Entity class name for level 2 parent (e.g., Feature level)", hidden = false, maxLength = 100
	)
	private String parentLevel2EntityClass;
	
	@Column (name = "parent_level3_entity_class", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Level 3 Entity Class", required = false, readOnly = false,
			description = "Entity class name for level 3 parent (e.g., User Story level)", hidden = false, maxLength = 100
	)
	private String parentLevel3EntityClass;
	
	@Column (name = "parent_level4_entity_class", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Level 4 Entity Class", required = false, readOnly = false,
			description = "Entity class name for level 4 parent (e.g., Task level)", hidden = false, maxLength = 100
	)
	private String parentLevel4EntityClass;

	/** Default constructor for JPA. */
	protected CTypeEntity() {
		super();
		// Initialize with default values for JPA
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
		canHaveChildren = true;
	}

	/** Constructor with required fields.
	 * @param name    the name of the type entity
	 * @param company the company this type belongs to */
	public CTypeEntity(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name, company);
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
		canHaveChildren = true;
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	/** Gets whether this type entity is non-deletable.
	 * @return true if this entity cannot be deleted */
	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	/** Gets the color code for this type.
	 * @return the hex color code */
	@Override
	public String getColor() { return color; }

	/** Gets the sort order for this type.
	 * @return the sort order */
	public Integer getSortOrder() { return sortOrder; }

	/** Gets the workflow for this type.
	 * @return the workflow entity */
	public CWorkflowEntity getWorkflow() { return workflow; }
	
	/** Gets whether this type can have children.
	 * @return true if items of this type can have child items */
	public boolean getCanHaveChildren() { return canHaveChildren; }
	
	/** Gets the entity class name for level 1 parent.
	 * @return the entity class name or null */
	public String getParentLevel1EntityClass() { return parentLevel1EntityClass; }
	
	/** Gets the entity class name for level 2 parent.
	 * @return the entity class name or null */
	public String getParentLevel2EntityClass() { return parentLevel2EntityClass; }
	
	/** Gets the entity class name for level 3 parent.
	 * @return the entity class name or null */
	public String getParentLevel3EntityClass() { return parentLevel3EntityClass; }
	
	/** Gets the entity class name for level 4 parent.
	 * @return the entity class name or null */
	public String getParentLevel4EntityClass() { return parentLevel4EntityClass; }

	@Override
	public int hashCode() {
		// Use the superclass hashCode method for consistency with equals method
		return super.hashCode();
	}

	/** Checks if this entity matches the given search value in the specified fields. This implementation extends CEntityOfCompany to also search in
	 * type-specific fields.
	 * @param searchValue the value to search for (case-insensitive)
	 * @param fieldNames  the list of field names to search in. If null or empty, searches only in "name" field. Supported field names: all parent
	 *                    fields plus "workflow", "attributeNonDeletable", "color"
	 * @return true if the entity matches the search criteria in any of the specified fields */
	@Override
	public boolean matchesFilter(final String searchValue, final java.util.Collection<String> fieldNames) {
		if ((searchValue == null) || searchValue.isBlank()) {
			return true; // No filter means match all
		}
		if (super.matchesFilter(searchValue, fieldNames)) {
			return true;
		}
		final String lowerSearchValue = searchValue.toLowerCase().trim();
		// Check entity field
		if (fieldNames.remove("workflow") && (getWorkflow() != null) && getWorkflow().matchesFilter(lowerSearchValue, Arrays.asList("name"))) {
			return true;
		}
		// Check boolean field
		if (fieldNames.remove("attributeNonDeletable") && String.valueOf(getAttributeNonDeletable()).toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		// Check string field
		if (fieldNames.remove("color") && (getColor() != null) && getColor().toLowerCase().contains(lowerSearchValue)) {
			return true;
		}
		return false;
	}

	/** Sets whether this type entity is non-deletable.
	 * @param attributeNonDeletable true if this entity cannot be deleted */
	public void setAttributeNonDeletable(final boolean attributeNonDeletable) {
		this.attributeNonDeletable = attributeNonDeletable;
	}

	/** Sets the color code for this type.
	 * @param color the hex color code to set */
	@Override
	public void setColor(final String color) { this.color = color; }

	/** Sets the sort order for this type.
	 * @param sortOrder the sort order to set */
	public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }

	/** Sets the workflow for this type.
	 * @param workflow the workflow entity to set */
	public void setWorkflow(final CWorkflowEntity workflow) { this.workflow = workflow; }
	
	/** Sets whether this type can have children.
	 * @param canHaveChildren true if items of this type can have child items */
	public void setCanHaveChildren(final boolean canHaveChildren) { this.canHaveChildren = canHaveChildren; }
	
	/** Sets the entity class name for level 1 parent.
	 * @param parentLevel1EntityClass the entity class name */
	public void setParentLevel1EntityClass(final String parentLevel1EntityClass) { 
		this.parentLevel1EntityClass = parentLevel1EntityClass; 
	}
	
	/** Sets the entity class name for level 2 parent.
	 * @param parentLevel2EntityClass the entity class name */
	public void setParentLevel2EntityClass(final String parentLevel2EntityClass) { 
		this.parentLevel2EntityClass = parentLevel2EntityClass; 
	}
	
	/** Sets the entity class name for level 3 parent.
	 * @param parentLevel3EntityClass the entity class name */
	public void setParentLevel3EntityClass(final String parentLevel3EntityClass) { 
		this.parentLevel3EntityClass = parentLevel3EntityClass; 
	}
	
	/** Sets the entity class name for level 4 parent.
	 * @param parentLevel4EntityClass the entity class name */
	public void setParentLevel4EntityClass(final String parentLevel4EntityClass) { 
		this.parentLevel4EntityClass = parentLevel4EntityClass; 
	}

	@Override
	public String toString() {
		return String.format("%s{id=%d, name='%s', color='%s', sortOrder=%d, nonDeletable=%s, company=%s}", getClass().getSimpleName(), getId(),
				getName(), color, sortOrder, attributeNonDeletable, getCompany() != null ? getCompany().getName() : "null");
	}
}
