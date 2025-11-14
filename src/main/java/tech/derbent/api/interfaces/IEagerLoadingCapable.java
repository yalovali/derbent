package tech.derbent.api.interfaces;

import java.util.Optional;

public interface IEagerLoadingCapable<T> {

	Optional<T> findByIdWithEagerLoading(Long id);
}
