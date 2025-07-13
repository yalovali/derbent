package tech.derbent.users.domain;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.domains.CEntityDB;

@Entity
@Table(name = "cuser")
public class CUser extends CEntityDB<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "user_id")
	private Long id;
	@Column(name = "name", nullable = false, length = MAX_LENGTH_NAME)
	@Size(max = MAX_LENGTH_NAME)
	private String name;

	@Override
	public @Nullable Long getId() { return id; }

	public String getName() { return name; }

	public void setName(final String name) { this.name = name; }
}
