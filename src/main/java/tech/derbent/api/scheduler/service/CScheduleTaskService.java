package tech.derbent.api.scheduler.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.scheduler.domain.CScheduleTask;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;

@Service
@PreAuthorize ("isAuthenticated()")
public class CScheduleTaskService extends CEntityOfCompanyService<CScheduleTask> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CScheduleTaskService.class);

	public CScheduleTaskService(final IScheduleTaskRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Transactional
	public void calculateNextRun(final CScheduleTask task) {
		try {
			final CronExpression cron = CronExpression.parse(task.getCronExpression());
			task.setNextRun(cron.next(LocalDateTime.now()));
		} catch (final Exception e) {
			LOGGER.error("Error calculating next run: {}", e.getMessage());
		}
	}

	@Transactional (readOnly = true)
	public List<CScheduleTask> findTasksDueForExecution() {
		return ((IScheduleTaskRepository) repository).findTasksDueForExecution(LocalDateTime.now());
	}

	@Override
	public Class<CScheduleTask> getEntityClass() { return CScheduleTask.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CScheduleTaskInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceScheduleTask.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateEntity(final CScheduleTask task) {
		super.validateEntity(task);
		Check.notBlank(task.getCronExpression(), "Cron expression is required");
		Check.notBlank(task.getAction(), "Action is required");
		try {
			CronExpression.parse(task.getCronExpression());
		} catch (final IllegalArgumentException e) {
			throw new CValidationException("Invalid cron expression: " + e.getMessage());
		}
	}
}
