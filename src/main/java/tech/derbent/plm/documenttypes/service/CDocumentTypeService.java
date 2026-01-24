package tech.derbent.plm.documenttypes.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.documenttypes.domain.CDocumentType;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-text-o", title = "Settings.Document Types")
@PermitAll
public class CDocumentTypeService extends CEntityOfCompanyService<CDocumentType> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CDocumentTypeService.class);

	private static String getFileExtension(final String fileName) {
		final int lastDot = fileName.lastIndexOf('.');
		if (lastDot > 0 && lastDot < fileName.length() - 1) {
			return fileName.substring(lastDot + 1);
		}
		return "";
	}

	public CDocumentTypeService(final IDocumentTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** @param mimeType */
	public Optional<CDocumentType> detectDocumentType(final String fileName, final String mimeType) {
		if (fileName == null || fileName.isBlank()) {
			return Optional.empty();
		}
		final String extension = getFileExtension(fileName).toLowerCase();
		final List<CDocumentType> allTypes = findAll();
		for (final CDocumentType type : allTypes) {
			final String typeName = type.getName().toLowerCase();
			if (typeName.contains("pdf") && extension.equals("pdf")) {
				return Optional.of(type);
			}
			if (typeName.contains("word") || typeName.contains("document")) {
				if (extension.equals("doc") || extension.equals("docx")) {
					return Optional.of(type);
				}
			}
			if (typeName.contains("excel") || typeName.contains("spreadsheet")) {
				if (extension.equals("xls") || extension.equals("xlsx")) {
					return Optional.of(type);
				}
			}
			if (typeName.contains("powerpoint") || typeName.contains("presentation")) {
				if (extension.equals("ppt") || extension.equals("pptx")) {
					return Optional.of(type);
				}
			}
			if (typeName.contains("image") || typeName.contains("picture")) {
				if (extension.matches("jpg|jpeg|png|gif|bmp|svg")) {
					return Optional.of(type);
				}
			}
			if (typeName.contains("video")) {
				if (extension.matches("mp4|avi|mov|mkv|wmv")) {
					return Optional.of(type);
				}
			}
			if (typeName.contains("code") || typeName.contains("source")) {
				if (extension.matches("java|py|js|ts|cpp|c|h|cs|go|rb|php")) {
					return Optional.of(type);
				}
			}
			if (typeName.contains("text")) {
				if (extension.equals("txt")) {
					return Optional.of(type);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public Class<CDocumentType> getEntityClass() { return CDocumentType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CDocumentTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() {
		// Page service not yet implemented - optional for now
		return null;
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CDocumentType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CDocumentType> existing = ((IDocumentTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
