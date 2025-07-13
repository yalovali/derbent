package tech.derbent.abstracts.services;

import java.time.Clock;

public class CAbstractService {

	protected final Clock clock;

	public CAbstractService(final Clock clock) {
		this.clock = clock;
	}
}