package tech.derbent.bab.policybase.actionmask.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskFile;

@Profile ("bab")
public interface IBabPolicyActionMaskFileRepository extends IPolicyActionMaskEntityRepository<CBabPolicyActionMaskFile> {}
