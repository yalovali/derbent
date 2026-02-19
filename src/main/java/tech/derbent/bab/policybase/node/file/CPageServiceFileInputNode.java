package tech.derbent.bab.policybase.node.file;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

/** CPageServiceFileInputNode - Page service for File Input nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Page service for dynamic page management. Handles File Input node page operations and UI integration. Note: View is nullable as
 * polymorphic node entities may not have dedicated views. */
@Service
@Profile ("bab")
public class CPageServiceFileInputNode extends CPageServiceDynamicPage<CBabFileInputNode> {

	public CPageServiceFileInputNode(@Nullable final IPageServiceImplementer<CBabFileInputNode> view) {
		super(view);
	}

	public List<String> getComboValuesOfFileFormat() { return List.of("JSON", "XML", "CSV", "TXT", "BINARY"); }
}
