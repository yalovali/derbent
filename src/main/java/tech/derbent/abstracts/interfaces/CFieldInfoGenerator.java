package tech.derbent.abstracts.interfaces;

import java.util.Map;

import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public interface CFieldInfoGenerator {

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
				final var meta = field
					.getAnnotation(tech.derbent.abstracts.annotations.MetaData.class);

				if (meta != null) {
					final EntityFieldInfo info =
						CEntityFieldService.createFieldInfo(meta);
					mapFieldInfos.put(field.getName(), info);
				}
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
