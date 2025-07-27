package tech.derbent.abstracts.domains;

public abstract class CTypeEntity extends CEntityNamed {

	public CTypeEntity() {
		super();
	}

	public CTypeEntity(final String name) {
		super(name);
	}

	public CTypeEntity(final String name, final String description) {
		super(name, description);
	}
}
