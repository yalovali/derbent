package tech.derbent.api.services;

import java.util.Optional;

public interface IEagerLoadingCapable<T> {

	Optional<T> findByIdWithEagerLoading(Long id);
}
