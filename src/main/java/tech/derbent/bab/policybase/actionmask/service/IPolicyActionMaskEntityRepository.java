package tech.derbent.bab.policybase.actionmask.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;

/** Generic repository contract for action mask entities. */
@NoRepositoryBean
@Profile ("bab")
public interface IPolicyActionMaskEntityRepository<MaskType extends CBabPolicyActionMaskBase<MaskType>> extends IAbstractNamedRepository<MaskType> {

	@Override
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.policyAction a
			LEFT JOIN FETCH a.policyRule
			LEFT JOIN FETCH a.destinationNode
			WHERE e.id = :id
			""")
	Optional<MaskType> findById(@Param ("id") Long id);
}
