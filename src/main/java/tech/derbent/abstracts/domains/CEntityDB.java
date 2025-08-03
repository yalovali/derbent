package tech.derbent.abstracts.domains;

import org.jspecify.annotations.Nullable;
import org.springframework.data.util.ProxyUtils;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
//mapped superclass is a class that is not

@MappedSuperclass
public abstract class CEntityDB<EntityClass> extends CEntity<EntityClass> {

	@Id
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Default constructor for JPA.
	 */
	protected CEntityDB() {
		super();
	}

	public CEntityDB(final Class<EntityClass> clazz) {
		super(clazz);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof CEntityDB)) {
			return false;
		}
		final CEntityDB<EntityClass> other = (CEntityDB<EntityClass>) obj;
		final Class<?> thisClass = ProxyUtils.getUserClass(getClass());
		final Class<?> otherClass = ProxyUtils.getUserClass(other.getClass());

		if (!thisClass.equals(otherClass)) {
			return false;
		}
		final Long id = getId();
		return (id != null) && id.equals(other.getId());
	}

	public String getColor() { return null; }

	@Nullable
	public Long getId() { return id; }

	@Override
	public int hashCode() {
		final Long id = getId();

		if (id != null) {
			return id.hashCode();
		}
		return ProxyUtils.getUserClass(getClass()).hashCode();
	}

	protected void initializeDefaults() {}

	@Override
	public String toString() {
		return "%s{id=%s}".formatted(getClass().getSimpleName(), getId());
	}
}