package tech.derbent.abstracts.views;

import java.lang.reflect.Field;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.abstracts.annotations.MetaData;

public class CEntityFormBuilder {

	public static Div buildForm(final Class<?> entityClass) {
		final Div panel = new Div();
		panel.setClassName("editor-layout");
		final FormLayout formLayout = new FormLayout();
		for (final Field field : entityClass.getDeclaredFields()) {
			final MetaData meta = field.getAnnotation(MetaData.class);
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if ((meta != null) && meta.hidden()) {
				continue; // Skip fields without MetaData or hidden fields
			}
			final TextField txtField = new TextField(field.getName());
			if (meta != null) {
				txtField.setLabel(meta.displayName());
				txtField.setRequiredIndicatorVisible(meta.required());
				txtField.setReadOnly(meta.readOnly());
				if (!meta.description().isEmpty()) {
					txtField.setHelperText(meta.description());
				}
			}
			formLayout.add(txtField);
		}
		panel.add(formLayout);
		return panel;
	}
}
