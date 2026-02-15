package tech.derbent.bab.node.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.node.domain.CBabNodeROS;

/** Page service for CBabNodeROS entity. Following Derbent pattern: Concrete page service for ROS node views. Provides UI action handling and
 * component lifecycle for ROS node views. */
public class CPageServiceBabNodeROS extends CPageServiceBabNode<CBabNodeROS> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabNodeROS.class);

	public CPageServiceBabNodeROS(final IPageServiceImplementer<CBabNodeROS> view) {
		super(view);
		LOGGER.debug("CPageServiceBabNodeROS initialized for view: {}", view.getClass().getSimpleName());
	}

	public List<String> getComboValuesOfRosVersion() {
		return List.of("ROS1", "ROS2");
	}
}
