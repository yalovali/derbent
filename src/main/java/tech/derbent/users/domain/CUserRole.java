package tech.derbent.users.domain;

/**
 * CUserRole - Enumeration representing user roles within the system.
 * Layer: Domain (MVC)
 * Defines the different roles that users can have in the application.
 */
public enum CUserRole {
    
    /**
     * Administrator role - full system access
     */
    ADMIN("Admin", "System administrator with full access to all features"),
    
    /**
     * Project Manager role - can manage projects and teams
     */
    PROJECT_MANAGER("Project Manager", "Can manage projects, assign tasks, and oversee team members"),
    
    /**
     * Team Member role - standard user with task management capabilities
     */
    TEAM_MEMBER("Team Member", "Can work on assigned tasks, log time, and update progress"),
    
    /**
     * Guest role - limited read-only access
     */
    GUEST("Guest", "Limited read-only access to designated project information");

    private final String displayName;
    private final String description;

    /**
     * Constructor for CUserRole enum.
     * @param displayName the human-readable name for the role
     * @param description detailed description of the role's capabilities
     */
    CUserRole(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the display name of the role.
     * @return human-readable role name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the description of the role.
     * @return detailed description of role capabilities
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the Spring Security authority name for this role.
     * @return authority name prefixed with "ROLE_"
     */
    public String getAuthority() {
        return "ROLE_" + name();
    }

    /**
     * Creates CUserRole from string representation.
     * Supports both enum name and display name matching.
     * @param roleString string representation of the role
     * @return corresponding CUserRole enum value
     * @throws IllegalArgumentException if no matching role is found
     */
    public static CUserRole fromString(final String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return TEAM_MEMBER; // Default role
        }

        final String trimmedRole = roleString.trim().toUpperCase();
        
        // Try direct enum name match first
        try {
            return valueOf(trimmedRole);
        } catch (IllegalArgumentException e) {
            // Try matching by display name
            for (CUserRole role : values()) {
                if (role.getDisplayName().equalsIgnoreCase(roleString.trim())) {
                    return role;
                }
            }
            // Handle legacy role strings
            if ("USER".equals(trimmedRole) || "MEMBER".equals(trimmedRole)) {
                return TEAM_MEMBER;
            }
            if ("MANAGER".equals(trimmedRole) || "PM".equals(trimmedRole)) {
                return PROJECT_MANAGER;
            }
            
            // Default to TEAM_MEMBER if no match found
            return TEAM_MEMBER;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}