package tech.derbent.app.tickets.ticket.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.tickets.ticket.domain.CTicket;
import tech.derbent.app.tickets.tickettype.domain.CTicketType;

public interface ITicketRepository extends IEntityOfProjectRepository<CTicket> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CTicketType type);
	@Override
	@Query (
		"SELECT r FROM CTicket r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType et LEFT JOIN FETCH et.workflow " + "WHERE r.id = :id"
	)
	Optional<CTicket> findById(@Param ("id") Long id);
}
