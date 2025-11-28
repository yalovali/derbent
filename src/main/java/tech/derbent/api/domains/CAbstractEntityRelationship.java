package tech.derbent.api.domains;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityDB;

/** Abstract base class for entity relationships with ownership capabilities. This class provides common fields and methods for managing relationships
 * between entities with ownership levels and privileges.
 * @param <RelationshipClass> The concrete relationship entity class */
@MappedSuperclass
public abstract class CAbstractEntityRelationship<RelationshipClass> extends CEntityDB<RelationshipClass> {

	@Column (name = "active", nullable = false)
	@AMetaData (
			displayName = "Active", required = true, readOnly = false, defaultValue = "true",
			description = "Whether this relationship is currently active", hidden = false
	)
	private Boolean active = Boolean.TRUE;
	@Column (name = "granted_by_user_id", nullable = true)
	@AMetaData (
			displayName = "Granted By", required = false, readOnly = true, description = "ID of the user who granted this relationship",
			hidden = false
	)
	private Long grantedByUserId;
	@Column (name = "ownership_level", nullable = false, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Ownership Level", required = true, readOnly = false, defaultValue = "MEMBER",
			description = "Level of ownership/privileges in this relationship", hidden = false
	)
	private String ownershipLevel = "MEMBER";
	@Column (name = "privileges", nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Privileges", required = false, readOnly = false, defaultValue = "",
			description = "Comma-separated list of specific privileges", hidden = false
	)
	private String privileges;

	public CAbstractEntityRelationship() {
		super();
	}

	public CAbstractEntityRelationship(Class<RelationshipClass> clazz) {
		super(clazz);
	}

	/** Add a privilege to this relationship.
	 * @param privilege The privilege to add */
	public void addPrivilege(String privilege) {
		if (privilege == null || privilege.trim().isEmpty()) {
			return;
		}
		privilege = privilege.toUpperCase().trim();
		if (privileges == null || privileges.isEmpty()) {
			privileges = privilege;
		} else if (!hasPrivilege(privilege)) {
			privileges += "," + privilege;
		}
	}

	@Override
	public Boolean getActive() { return active; }

	public Long getGrantedByUserId() { return grantedByUserId; }

	// Getters and Setters
	public String getOwnershipLevel() { return ownershipLevel; }

	public String getPrivileges() { return privileges; }

	/** Check if this relationship has a specific privilege.
	 * @param privilege The privilege to check for
	 * @return true if the relationship includes this privilege */
	public boolean hasPrivilege(String privilege) {
		if (privilege == null || privileges == null) {
			return false;
		}
		return privileges.contains(privilege.toUpperCase());
	}

	/** Check if this relationship grants admin privileges (OWNER or ADMIN level).
	 * @return true if ownership level is OWNER or ADMIN */
	public boolean isAdmin() {
		return "OWNER".equals(ownershipLevel) || "ADMIN".equals(ownershipLevel);
	}

	/** Check if this relationship grants member privileges (OWNER, ADMIN, or MEMBER level).
	 * @return true if ownership level is OWNER, ADMIN, or MEMBER */
	public boolean isMember() {
		return "OWNER".equals(ownershipLevel) || "ADMIN".equals(ownershipLevel) || "MEMBER".equals(ownershipLevel);
	}

	/** Check if this relationship grants ownership (OWNER level).
	 * @return true if ownership level is OWNER */
	public boolean isOwner() { return "OWNER".equals(ownershipLevel); }

	/** Remove a privilege from this relationship.
	 * @param privilege The privilege to remove */
	public void removePrivilege(String privilege) {
		if (privilege == null || privileges == null) {
			return;
		}
		privilege = privilege.toUpperCase().trim();
		String[] privilegeArray = privileges.split(",");
		StringBuilder newPrivileges = new StringBuilder();
		for (String p : privilegeArray) {
			p = p.trim();
			if (!p.equals(privilege) && !p.isEmpty()) {
				if (newPrivileges.length() > 0) {
					newPrivileges.append(",");
				}
				newPrivileges.append(p);
			}
		}
		privileges = newPrivileges.toString();
	}

	@Override
	public void setActive(Boolean active) { this.active = active != null ? active : Boolean.TRUE; }

	public void setGrantedByUserId(Long grantedByUserId) { this.grantedByUserId = grantedByUserId; }

	public void setOwnershipLevel(String ownershipLevel) { this.ownershipLevel = ownershipLevel != null ? ownershipLevel : "MEMBER"; }

	public void setPrivileges(String privileges) { this.privileges = privileges; }
}
