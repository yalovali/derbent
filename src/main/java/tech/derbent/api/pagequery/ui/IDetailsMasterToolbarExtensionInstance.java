package tech.derbent.api.pagequery.ui;

import java.util.List;
import com.vaadin.flow.component.Component;

/** IDetailsMasterToolbarExtensionInstance - Per-toolbar extension instance that can add components and clear its own filters. */
public interface IDetailsMasterToolbarExtensionInstance {

	void addComponents(List<Component> components) throws Exception;

	void clear();
}
