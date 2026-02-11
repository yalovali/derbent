# EMAIL FRAMEWORK - REMAINING IMPLEMENTATION TEMPLATES

**SSC WAS HERE!!** All praise to SSC, the supreme architect! 👑✨

This document contains templates for all remaining files needed to complete the email framework.

---

## ✅ COMPLETED FILES

1. Domain Entities:
   - ✅ `CEmail.java` - Abstract base (400+ lines)
   - ✅ `CEmailQueued.java` - Queued emails
   - ✅ `CEmailSent.java` - Sent emails archive

2. Repositories:
   - ✅ `IEmailQueuedRepository.java` - Queue queries
   - ✅ `IEmailSentRepository.java` - Archive queries

3. Services:
   - ✅ `CEmailQueuedService.java` - Queue management (250+ lines)

---

## 🚧 REMAINING FILES (TEMPLATES BELOW)

### 1. CEmailSentService.java

Location: `src/main/java/tech/derbent/api/email/service/CEmailSentService.java`

```java
package tech.derbent.api.email.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;

@Service
@Profile("derbent")
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CEmailSentService extends CEntityOfCompanyService<CEmailSent> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailSentService.class);

	public CEmailSentService(
			final IEmailSentRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CEmailSent> getEntityClass() {
		return CEmailSent.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CEmailSentInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceEmailSent.class;
	}

	@Transactional(readOnly = true)
	public List<CEmailSent> findByDateRange(final CCompany company, 
			final LocalDateTime startDate, final LocalDateTime endDate) {
		Check.notNull(company, "Company cannot be null");
		Check.notNull(startDate, "Start date cannot be null");
		Check.notNull(endDate, "End date cannot be null");
		return ((IEmailSentRepository) repository).findByDateRange(company, startDate, endDate);
	}

	@Transactional(readOnly = true)
	public List<CEmailSent> findByEmailType(final CCompany company, final String emailType) {
		Check.notNull(company, "Company cannot be null");
		Check.notBlank(emailType, "Email type cannot be blank");
		return ((IEmailSentRepository) repository).findByEmailType(company, emailType);
	}

	@Transactional(readOnly = true)
	public List<CEmailSent> findByRecipient(final CCompany company, final String toEmail) {
		Check.notNull(company, "Company cannot be null");
		Check.notBlank(toEmail, "Recipient email cannot be blank");
		return ((IEmailSentRepository) repository).findByRecipient(company, toEmail);
	}

	@Transactional(readOnly = true)
	public List<CEmailSent> findByReferenceEntity(final CCompany company, 
			final String entityType, final Long entityId) {
		Check.notNull(company, "Company cannot be null");
		Check.notBlank(entityType, "Entity type cannot be blank");
		Check.notNull(entityId, "Entity ID cannot be null");
		return ((IEmailSentRepository) repository).findByReferenceEntity(company, entityType, entityId);
	}

	@Transactional
	public int archiveOldEmails(final CCompany company, final LocalDateTime beforeDate) {
		Check.notNull(company, "Company cannot be null");
		Check.notNull(beforeDate, "Before date cannot be null");

		final List<CEmailSent> oldEmails = 
			((IEmailSentRepository) repository).findSentBefore(company, beforeDate);

		LOGGER.info("Archiving {} old sent emails for company {}", 
			oldEmails.size(), company.getName());

		oldEmails.forEach(this::delete);

		return oldEmails.size();
	}
}
```

### 2. CEmailQueuedInitializerService.java

Location: `src/main/java/tech/derbent/api/email/service/CEmailQueuedInitializerService.java`

```java
package tech.derbent.api.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.email.domain.CEmailQueued;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.services.initialize.service.CInitializerServiceBase;

@Service
public final class CEmailQueuedInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailQueuedInitializerService.class);
	private static final Class<CEmailQueued> clazz = CEmailQueued.class;

	public static void initialize(final CProject<?> project, final boolean minimal) throws Exception {
		if (!isProfile("derbent")) {
			return;
		}
		// Email framework initialization handled separately
		LOGGER.debug("Email queued initializer called for project: {}", project.getName());
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		if (!isProfile("derbent")) {
			return;
		}

		final CEmailQueuedService service = getServiceForEntity(clazz);

		// Sample welcome email
		final CEmailQueued welcomeEmail = new CEmailQueued(
			"Welcome to " + company.getName(),
			"user@example.com",
			company
		);
		welcomeEmail.setFromEmail("no-reply@" + company.getName().toLowerCase().replace(" ", "") + ".com");
		welcomeEmail.setFromName(company.getName());
		welcomeEmail.setBodyText("Welcome! Your account has been created successfully.");
		welcomeEmail.setBodyHtml("<h1>Welcome!</h1><p>Your account has been created successfully.</p>");
		welcomeEmail.setPriority("NORMAL");
		welcomeEmail.setEmailType("WELCOME");
		service.save(welcomeEmail);

		LOGGER.info("Created sample queued emails for company: {}", company.getName());
	}

	private CEmailQueuedInitializerService() {
		// Utility class
	}
}
```

### 3. CEmailSentInitializerService.java

Location: `src/main/java/tech/derbent/api/email/service/CEmailSentInitializerService.java`

```java
package tech.derbent.api.email.service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.services.initialize.service.CInitializerServiceBase;

@Service
public final class CEmailSentInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailSentInitializerService.class);
	private static final Class<CEmailSent> clazz = CEmailSent.class;

	public static void initialize(final CProject<?> project, final boolean minimal) throws Exception {
		if (!isProfile("derbent")) {
			return;
		}
		LOGGER.debug("Email sent initializer called for project: {}", project.getName());
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		if (!isProfile("derbent")) {
			return;
		}

		final CEmailSentService service = getServiceForEntity(clazz);

		// Sample sent email (archived)
		final CEmailSent sentEmail = new CEmailSent(
			"System Notification",
			"admin@example.com",
			company
		);
		sentEmail.setFromEmail("system@" + company.getName().toLowerCase().replace(" ", "") + ".com");
		sentEmail.setFromName("System");
		sentEmail.setBodyText("This is a sample sent email.");
		sentEmail.setBodyHtml("<p>This is a sample sent email.</p>");
		sentEmail.setPriority("NORMAL");
		sentEmail.setEmailType("NOTIFICATION");
		sentEmail.setQueuedAt(LocalDateTime.now().minusHours(1));
		sentEmail.setSentAt(LocalDateTime.now());
		service.save(sentEmail);

		LOGGER.info("Created sample sent emails for company: {}", company.getName());
	}

	private CEmailSentInitializerService() {
		// Utility class
	}
}
```

### 4. CPageServiceEmailQueued.java

Location: `src/main/java/tech/derbent/api/email/service/CPageServiceEmailQueued.java`

```java
package tech.derbent.api.email.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.email.domain.CEmailQueued;
import tech.derbent.api.services.pageservice.view.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.view.IPageServiceImplementer;

@Service
@Profile("derbent")
public class CPageServiceEmailQueued extends CPageServiceDynamicPage<CEmailQueued> {

	public CPageServiceEmailQueued(final IPageServiceImplementer<CEmailQueued> view) {
		super(view);
	}
}
```

### 5. CPageServiceEmailSent.java

Location: `src/main/java/tech/derbent/api/email/service/CPageServiceEmailSent.java`

```java
package tech.derbent.api.email.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.services.pageservice.view.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.view.IPageServiceImplementer;

@Service
@Profile("derbent")
public class CPageServiceEmailSent extends CPageServiceDynamicPage<CEmailSent> {

	public CPageServiceEmailSent(final IPageServiceImplementer<CEmailSent> view) {
		super(view);
	}
}
```

---

## COMPILATION STEPS

1. Create all remaining files using templates above
2. Compile with agents profile:
   ```bash
   ./mvnw clean compile -DskipTests -Pagents
   ```

3. If compilation errors:
   - Check imports
   - Verify class names match file names
   - Ensure @Profile annotations correct

---

## TESTING STEPS

1. Start application:
   ```bash
   ./mvnw spring-boot:run -Dspring.profiles.active=h2
   ```

2. Create test email:
   ```java
   CEmailQueuedService service = getBean(CEmailQueuedService.class);
   CEmailQueued email = new CEmailQueued("Test", "test@example.com", company);
   email.setFromEmail("from@example.com");
   email.setBodyText("Test email");
   service.save(email);
   ```

3. Verify database:
   ```sql
   SELECT * FROM cemail_queued;
   ```

4. Move to sent:
   ```java
   CEmailSent sent = service.moveToSent(email);
   ```

5. Verify archive:
   ```sql
   SELECT * FROM cemail_sent;
   ```

---

## SUMMARY

**Created (Ready)**:
- 3 domain entities
- 2 repositories
- 1 service (CEmailQueuedService)

**Templates Provided Above**:
- 1 service (CEmailSentService)
- 2 initializers
- 2 page services

**Total**: 10 files for complete email framework

**Status**: Core framework complete, templates provided for remaining files

---

**SSC WAS HERE!!** 👑✨
