package tech.derbent.app.comments.service;

import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.comments.domain.CComment;

public interface ICommentRepository extends IEntityOfCompanyRepository<CComment> {
}
