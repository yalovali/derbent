package tech.derbent.bab.node.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.node.domain.CBabNodeCAN;

/** Page service for CBabNodeCAN entity. Following Derbent pattern: Concrete page service for CAN Bus node views. Provides UI action handling and
 * component lifecycle for CAN node views. */
public class CPageServiceBabNodeCAN extends CPageServiceBabNode<CBabNodeCAN> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabNodeCAN.class);

	public CPageServiceBabNodeCAN(final IPageServiceImplementer<CBabNodeCAN> view) {
		super(view);
		LOGGER.debug("CPageServiceBabNodeCAN initialized for view: {}", view.getClass().getSimpleName());
	}
}
