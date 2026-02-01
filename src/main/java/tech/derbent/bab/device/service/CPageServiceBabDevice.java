package tech.derbent.bab.device.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.device.domain.CBabDevice;

/** Page service for CBabDevice entity. Following Derbent pattern: Page service extends CPageServiceDynamicPage. Provides UI action handling and
 * component lifecycle for device views. */
public class CPageServiceBabDevice extends CPageServiceDynamicPage<CBabDevice> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabDevice.class);

	public CPageServiceBabDevice(final IPageServiceImplementer<CBabDevice> view) {
		super(view);
		LOGGER.debug("CPageServiceBabDevice initialized for view: {}", view.getClass().getSimpleName());
	}
}
