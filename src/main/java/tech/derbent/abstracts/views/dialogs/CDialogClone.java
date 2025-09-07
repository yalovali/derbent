package tech.derbent.abstracts.views.dialogs;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public class CDialogClone<EntityClass extends CEntityDB<EntityClass>> extends CDBEditDialog<EntityClass> {

	private static final long serialVersionUID = 1L;

	public static List<String> getAvailableTypes() {
		// TODO Auto-generated method stub
		return List.of("Type1", "Type2", "Type3");
	}

	public CDialogClone(final EntityClass entity, final Consumer<EntityClass> onSave) throws Exception {
		super(entity, onSave, true);
		setupDialog();
	}

	@Override
	public String getDialogTitleString() { // TODO Auto-generated method stub
		final String title = "Clone " + getEntity().toString();
		return title;
	}

	@Override
	protected Icon getFormIcon() throws Exception { return CColorUtils.getIconForEntity(getEntity()); }

	@Override
	protected String getFormTitleString() { // TODO Auto-generated method stub
		return "Clone";
	}

	@Override
	protected void populateForm() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		final CEntityFormBuilder<EntityClass> formBuilder = new CEntityFormBuilder<>(getEntity().getClass(), null);
		// create a comboxbox to select the type to clone to
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldTypeClass(String.class);
		fieldInfo.setDisplayName("Select Type");
		fieldInfo.setDataProviderBean("tech.derbent.abstracts.views.dialogs.CDialogClone");
		fieldInfo.setDataProviderMethod("getAvailableTypes");
		formBuilder.addFieldLine(fieldInfo);
		//
		fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldTypeClass(String.class);
		fieldInfo.setDisplayName("New Name");
		formBuilder.addFieldLine(fieldInfo);
		getDialogLayout().add(formBuilder.getFormLayout());
	}

	@Override
	protected void validateForm() {
		// TODO Auto-generated method stub
	}
}
