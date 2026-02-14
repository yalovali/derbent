package tech.derbent.bab.policybase.service;

import tech.derbent.bab.utils.CReflectionJsonSerializer;

/** Service-style entrypoint for policy JSON serialization. */
public final class CJsonSerializationService {

	private CJsonSerializationService() {}

	public static String toJson(final Object object) {
		return CReflectionJsonSerializer.toJson(object);
	}
}
