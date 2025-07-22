package tech.derbent.abstracts.domains;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.projects.domain.CProject;

@MappedSuperclass
public class CEntityOfProject extends CEntityDB {

    // Many risks belong to one project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private CProject project;
    @Column(name = "name", nullable = false, length = MAX_LENGTH_NAME, unique = true)
    @Size(max = MAX_LENGTH_NAME)
    @MetaData(displayName = "Name", required = true, readOnly = false, defaultValue = "", description = "Name of the entity", hidden = false, order = 1, maxLength = MAX_LENGTH_NAME)
    private String name;

    // Default constructor for JPA
    public CEntityOfProject() {
        super();
        this.project = null; // This should be set later
    }

    public CEntityOfProject(final CProject project) {
        this.project = project;
    }

    public CEntityOfProject(final String name, final CProject project) {
        super();
        this.name = name;
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public CProject getProject() {
        return project;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setProject(final CProject project) {
        this.project = project;
    }
}
