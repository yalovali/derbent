package tech.derbent.users.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.domains.CEntityDB;

@Entity
@Table(name = "cuser") // table name for the entity as the default is the class name in lowercase
@AttributeOverride(name = "id", column = @Column(name = "user_id")) // Override the default column name for the ID field
public class CUser extends CEntityDB {

	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME)
	@Size(max = MAX_LENGTH_NAME)
	private String name;

	public String getName() { return name; }

	public void setName(final String name) { this.name = name; }
}
