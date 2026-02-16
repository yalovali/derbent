package tech.derbent.bab.policybase.filter.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCSV;

/** Service for CSV policy filters. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyFilterCSVService extends CBabPolicyFilterBaseService<CBabPolicyFilterCSV> implements IEntityRegistrable, IEntityWithView {

	public CBabPolicyFilterCSVService(final IBabPolicyFilterCSVRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void copyTypeSpecificFieldsTo(final CBabPolicyFilterCSV source, final CBabPolicyFilterCSV target, final CCloneOptions options) {
		target.setCaptureColumnRange(source.getCaptureColumnRange());
		target.setColumnSeparator(source.getColumnSeparator());
		target.setLineRegularExpression(source.getLineRegularExpression());
	}

	@Override
	public Class<CBabPolicyFilterCSV> getEntityClass() { return CBabPolicyFilterCSV.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabPolicyFilterCSVInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabPolicyFilterCSV.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateTypeSpecificFields(final CBabPolicyFilterCSV entity) {
		validateStringLength(entity.getCaptureColumnRange(), "Capture Column Range", 20);
		validateStringLength(entity.getLineRegularExpression(), "Line Regular Expression", 255);
		validateCaptureColumnRange(entity);
		validateColumnSeparator(entity);
		validateRegularExpression(entity.getLineRegularExpression(), "Line regular expression");
	}

	private void validateCaptureColumnRange(final CBabPolicyFilterCSV entity) {
		final String range = entity.getCaptureColumnRange();
		if (range == null || range.isBlank()) {
			throw new CValidationException("Capture column range is required");
		}
		if (!range.matches("^\\d+(?:-\\d+)?$")) {
			throw new CValidationException("Capture column range must be N or N-M (for example: 3 or 3-6)");
		}
		try {
			if (!range.contains("-")) {
				if (Integer.parseInt(range) < 1) {
					throw new CValidationException("Capture column range must start at column 1 or higher");
				}
				return;
			}
			final String[] parts = range.split("-", 2);
			final int startColumn = Integer.parseInt(parts[0]);
			final int endColumn = Integer.parseInt(parts[1]);
			if (startColumn < 1 || endColumn < 1) {
				throw new CValidationException("Capture column range values must be 1 or higher");
			}
			if (endColumn < startColumn) {
				throw new CValidationException("Capture column range end must be greater than or equal to start");
			}
		} catch (final NumberFormatException ex) {
			throw new CValidationException("Capture column range must contain valid column numbers");
		}
	}

	private void validateColumnSeparator(final CBabPolicyFilterCSV entity) {
		final String separator = entity.getColumnSeparator();
		if (!CBabPolicyFilterCSV.COLUMN_SEPARATOR_COMMA.equals(separator) && !CBabPolicyFilterCSV.COLUMN_SEPARATOR_SEMICOLON.equals(separator)) {
			throw new CValidationException("Column separator must be ',' or ';'");
		}
	}
}
