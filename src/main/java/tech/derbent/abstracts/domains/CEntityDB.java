package tech.derbent.abstracts.domains;

import org.jspecify.annotations.Nullable;
import org.springframework.data.util.ProxyUtils;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class CEntityDB extends CEntity {

	public static final int MAX_LENGTH_DESCRIPTION = 255;
	public static final int MAX_LENGTH_NAME = 100;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	public CEntityDB() {
		super();
	}

	public boolean equals(final CEntityDB obj) {
		if (obj == null) {
			return false;
		}
		else if (obj == this) {
			return true;
		}
		final var thisUserClass = ProxyUtils.getUserClass(getClass());
		final var otherUserClass = ProxyUtils.getUserClass(obj);
		if (thisUserClass != otherUserClass) {
			return false;
		}
		final var id = getId();
		return (id != null) && id.equals(obj.getId());
	}

	@Nullable
	public Long getId() { return id; }

	@Override
	public int hashCode() {
		// Hashcode should never change during the lifetime of an object. Because of
		// this we can't use getId() to calculate the hashcode. Unless you have sets
		// with lots of entities in them, returning the same hashcode should not be a
		// problem.
		return ProxyUtils.getUserClass(getClass()).hashCode();
	}

	@Override
	public String toString() {
		return "%s{id=%s}".formatted(getClass().getSimpleName(), getId());
	}
}