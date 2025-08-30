package tech.derbent.abstracts.views.dialogs;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public class CDialogClone<EntityClass> extends CDBEditDialog<EntityClass> {
	private static final long serialVersionUID = 1L;

	public static List<String> getAvailableTypes() {
		// TODO Auto-generated method stub
		return List.of("Type1", "Type2", "Type3");
	}

	public CDialogClone(final EntityClass entity, final Consumer<EntityClass> onSave) {
		super(entity, onSave, true);
		setupDialog();
	}

	@Override
	protected Icon getFormIcon() { // TODO Auto-generated method stub
		return VaadinIcon.COPY.create();
	}

	@Override
	protected String getFormTitle() { // TODO Auto-generated method stub
		return "Clone";
	}

	@Override
	public String getHeaderTitle() { // TODO Auto-generated method stub
		return "Clone Item To Selected Type";
	}

	@Override
	protected void populateForm() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		final CEntityFormBuilder<EntityClass> formBuilder = new CEntityFormBuilder<>(entity.getClass(), null);
		// create a comboxbox to select the type to clone to
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldTypeClass(String.class);
		fieldInfo.setDisplayName("Select Type");
		fieldInfo.setRequired(true);
		fieldInfo.setDataProviderBean("tech.derbent.abstracts.views.dialogs.CDialogClone");
		fieldInfo.setDataProviderMethod("getAvailableTypes");
		formBuilder.addFieldLine(fieldInfo);
		//
		fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldTypeClass(String.class);
		fieldInfo.setDisplayName("Name");
		fieldInfo.setRequired(true);
		formBuilder.addFieldLine(fieldInfo);
		getDialogLayout().add(formBuilder.getFormLayout());
	}

	@Override
	protected void validateForm() {
		// TODO Auto-generated method stub
	}
}
