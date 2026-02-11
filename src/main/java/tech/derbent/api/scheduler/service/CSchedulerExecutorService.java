package tech.derbent.api.scheduler.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.email.service.CEmailQueuedService;
import tech.derbent.api.scheduler.domain.CScheduleTask;

/**
 * CSchedulerExecutorService - Background task executor service.
 * 
 * <p>Executes scheduled tasks based on cron expressions.
 * Enabled via: derbent.scheduler.enabled=true
 * 
 * <p>Runs every minute to check for due tasks.
 */
@Service
@ConditionalOnProperty(value = "derbent.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class CSchedulerExecutorService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CSchedulerExecutorService.class);
	
	private final CScheduleTaskService scheduleTaskService;
	private final CEmailQueuedService emailQueuedService;
	
	public CSchedulerExecutorService(final CScheduleTaskService scheduleTaskService,
			final CEmailQueuedService emailQueuedService) {
		this.scheduleTaskService = scheduleTaskService;
		this.emailQueuedService = emailQueuedService;
		LOGGER.info("Scheduler executor service initialized");
	}
	
	@Scheduled(cron = "0 * * * * *")
	@Transactional
	public void executePendingTasks() {
		final List<CScheduleTask> dueTasks = scheduleTaskService.findTasksDueForExecution();
		
		if (dueTasks.isEmpty()) {
			return;
		}
		
		LOGGER.debug("Found {} tasks due for execution", dueTasks.size());
		
		for (final CScheduleTask task : dueTasks) {
			executeTask(task);
		}
	}
	
	private void executeTask(final CScheduleTask task) {
		LOGGER.info("Executing task: {} (action: {})", task.getName(), task.getAction());
		
		try {
			switch (task.getAction()) {
				case CScheduleTask.ACTION_PROCESS_EMAIL_QUEUE:
					emailQueuedService.processQueue();
					break;
				default:
					LOGGER.warn("Unknown action: {}", task.getAction());
					task.setLastError("Unknown action: " + task.getAction());
					task.recordExecution(false);
					scheduleTaskService.save(task);
					return;
			}
			
			task.recordExecution(true);
			scheduleTaskService.calculateNextRun(task);
			scheduleTaskService.save(task);
			
			LOGGER.info("Task executed successfully: {}", task.getName());
			
		} catch (final Exception e) {
			LOGGER.error("Task execution failed: {}", task.getName(), e);
			task.setLastError(e.getMessage());
			task.recordExecution(false);
			scheduleTaskService.calculateNextRun(task);
			scheduleTaskService.save(task);
		}
	}
}
