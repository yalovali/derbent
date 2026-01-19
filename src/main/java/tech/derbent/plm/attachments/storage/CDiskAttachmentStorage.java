package tech.derbent.plm.attachments.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Disk-based implementation of IAttachmentStorage. Stores attachment files in a configurable directory on the local filesystem. Files are organized
 * by year/month/day to prevent directory overcrowding. Uses UUIDs to prevent filename conflicts. */
@Component
public class CDiskAttachmentStorage implements IAttachmentStorage {

	private static final int BUFFER_SIZE = 8192;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDiskAttachmentStorage.class);

	/** Sanitize a filename by removing or replacing dangerous characters.
	 * @param fileName the original filename
	 * @return sanitized filename */
	private static String sanitizeFileName(final String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return "unnamed";
		}
		// Remove or replace dangerous characters
		return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

	@Value ("${derbent.attachments.storage.path:./data/attachments}")
	private String storagePath;

	@Override
	public boolean delete(final String contentPath) {
		try {
			final Path fullPath = Paths.get(storagePath, contentPath);
			if (Files.exists(fullPath)) {
				Files.delete(fullPath);
				LOGGER.info("Deleted file: {}", contentPath);
				return true;
			}
			LOGGER.warn("File not found for deletion: {}", contentPath);
			return false;
		} catch (final Exception e) {
			LOGGER.error("Error deleting file: {}", contentPath, e);
			return false;
		}
	}

	@Override
	public InputStream download(final String contentPath) throws Exception {
		final Path fullPath = Paths.get(storagePath, contentPath);
		if (!Files.exists(fullPath)) {
			throw new IllegalArgumentException("File not found: " + contentPath);
		}
		return new FileInputStream(fullPath.toFile());
	}

	@Override
	public boolean exists(final String contentPath) {
		final Path fullPath = Paths.get(storagePath, contentPath);
		return Files.exists(fullPath);
	}

	@Override
	public String getAbsolutePath(final String contentPath) {
		return Paths.get(storagePath, contentPath).toAbsolutePath().toString();
	}

	@Override
	public String upload(final String fileName, final InputStream contentStream, final long fileSize) throws Exception {
		// Generate unique path: year/month/day/uuid-filename
		final LocalDate now = LocalDate.now();
		final String yearMonth = String.format("%04d/%02d/%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
		final String uniqueFileName = UUID.randomUUID().toString() + "-" + sanitizeFileName(fileName);
		final String relativePath = yearMonth + "/" + uniqueFileName;
		// Create directory structure
		final Path fullPath = Paths.get(storagePath, relativePath);
		Files.createDirectories(fullPath.getParent());
		// Write file
		try (final FileOutputStream fos = new FileOutputStream(fullPath.toFile())) {
			final byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = contentStream.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		}
		LOGGER.info("Uploaded file to: {}", relativePath);
		return relativePath;
	}
}
