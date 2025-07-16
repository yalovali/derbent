package tech.derbent.security.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.users.domain.CUser;

@Entity
@Table(name = "clogin")
@AttributeOverride(name = "id", column = @Column(name = "login_id"))
public class CLogin extends CEntityDB {

    @Column(name = "username", nullable = false, length = MAX_LENGTH_NAME, unique = true)
    @Size(max = MAX_LENGTH_NAME)
    @NotBlank
    @MetaData(displayName = "Username", required = true, readOnly = false, defaultValue = "", description = "Login username", hidden = false)
    private String username;

    @Column(name = "password", nullable = false, length = MAX_LENGTH_DESCRIPTION)
    @Size(max = MAX_LENGTH_DESCRIPTION)
    @NotBlank
    @MetaData(displayName = "Password", required = true, readOnly = false, defaultValue = "", description = "Login password", hidden = false)
    private String password;

    @Column(name = "roles", nullable = true, length = MAX_LENGTH_DESCRIPTION)
    @Size(max = MAX_LENGTH_DESCRIPTION)
    @MetaData(displayName = "Roles", required = false, readOnly = false, defaultValue = "USER", description = "User roles (comma separated)", hidden = false)
    private String roles;

    @Column(name = "enabled", nullable = false)
    @MetaData(displayName = "Enabled", required = true, readOnly = false, defaultValue = "true", description = "Whether the login is enabled", hidden = false)
    private Boolean enabled = true;

    @OneToOne
    @JoinColumn(name = "user_id")
    @MetaData(displayName = "User", required = false, readOnly = false, defaultValue = "", description = "Associated user", hidden = false)
    private CUser user;

    public CLogin() {
        super();
    }

    public CLogin(String username, String password) {
        super();
        this.username = username;
        this.password = password;
        this.enabled = true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public CUser getUser() {
        return user;
    }

    public void setUser(CUser user) {
        this.user = user;
    }
}