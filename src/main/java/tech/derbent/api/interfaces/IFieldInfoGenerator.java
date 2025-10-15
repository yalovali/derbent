package tech.derbent.api.interfaces;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public interface IFieldInfoGenerator {

	Logger LOGGER = LoggerFactory.getLogger(IFieldInfoGenerator.class);
	Map<String, EntityFieldInfo> mapFieldInfos = null;

	Class<?> getClassName();

	default Map<String, EntityFieldInfo> getMapFieldInfos() {
		if (mapFieldInfos == null) {
			initFieldInfos();
		}
		return mapFieldInfos;
	}

	default void initFieldInfos() {
		try {
			for (final var field : getClassName().getDeclaredFields()) {
				final var meta = field.getAnnotation(tech.derbent.api.annotations.AMetaData.class);
				if (meta != null) {
					final EntityFieldInfo info = CEntityFieldService.createFieldInfo(meta);
					mapFieldInfos.put(field.getName(), info);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error initializing field information for class '{}': {}", getClassName(), e.getMessage());
			throw e;
		}
	}
}
