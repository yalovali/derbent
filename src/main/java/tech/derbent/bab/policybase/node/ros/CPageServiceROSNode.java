package tech.derbent.bab.policybase.node.ros;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;

/**
 * CPageServiceROSNode - Page service for ROS nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Page service for dynamic page management.
 * 
 * Handles ROS node page operations and UI integration.
 * Note: View is nullable as polymorphic node entities may not have dedicated views.
 */
@Service
@Profile("bab")
public class CPageServiceROSNode extends CPageServiceDynamicPage<CBabROSNode> {
	
	public CPageServiceROSNode(@Nullable final IPageServiceImplementer<CBabROSNode> view) {
		super(view);
	}

	public List<String> getAvailableRosVersions() {
		return List.of("ROS1", "ROS2");
	}
}
