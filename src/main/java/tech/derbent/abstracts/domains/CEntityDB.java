package tech.derbent.abstracts.domains;

import org.jspecify.annotations.Nullable;
import org.springframework.data.util.ProxyUtils;

import jakarta.persistence.MappedSuperclass;
import tech.derbent.base.domain.AbstractEntity;

@MappedSuperclass
public abstract class CEntityDB<ID> extends CEntity {

	public static final int MAX_LENGTH_DESCRIPTION = 255;
	public static final int MAX_LENGTH_NAME = 100;

	@Override
	public boolean equals(final Object obj) {
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
		return (id != null) && id.equals(((AbstractEntity<?>) obj).getId());
	}

	public abstract @Nullable ID getId();

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