package tech.derbent.abstracts.views;

import java.util.function.Consumer;

import com.vaadin.flow.component.icon.Icon;

public class CDBRelationDialog<EntityService, RelatedEntityService, EntityClass>
	extends CDBEditDialog<EntityClass> {

	private static final long serialVersionUID = 1L;

	private static <T> T createNewRelation() {
		// Logic to create a new instance of RelationClass
		return null; // Placeholder
	}

	protected final RelatedEntityService relatedEntityService;

	protected final EntityService entityService;

	protected final EntityClass currentEntity;

	public CDBRelationDialog(final EntityService entityService,
		final RelatedEntityService relatedEntityService, final EntityClass currentEntity,
		final Consumer<EntityClass> onSave, final boolean isNew) {
		super(currentEntity, onSave, isNew);
		this.currentEntity = currentEntity;
		this.entityService = entityService;
		this.relatedEntityService = relatedEntityService;
		setupDialog();
		populateForm();
	}

	@Override
	protected Icon getFormIcon() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getFormTitle() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeaderTitle() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void populateForm() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void validateForm() {
		// TODO Auto-generated method stub
	}
}
