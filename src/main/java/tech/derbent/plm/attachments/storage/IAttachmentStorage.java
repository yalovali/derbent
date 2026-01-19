package tech.derbent.plm.attachments.storage;

import java.io.InputStream;

/**
 * Abstraction interface for attachment storage.
 * 
 * Provides methods to upload, download, and delete attachment files.
 * Implementation can store files in different locations (filesystem, S3, etc.).
 */
public interface IAttachmentStorage {

	/** Upload a file and return its storage path.
	 * @param fileName the original file name
	 * @param contentStream the file content as an input stream
	 * @param fileSize the size of the file in bytes
	 * @return the relative path where the file was stored
	 * @throws Exception if upload fails */
	String upload(String fileName, InputStream contentStream, long fileSize) throws Exception;

	/** Download a file by its storage path.
	 * @param contentPath the relative path to the file
	 * @return the file content as an input stream
	 * @throws Exception if download fails or file not found */
	InputStream download(String contentPath) throws Exception;

	/** Delete a file by its storage path.
	 * @param contentPath the relative path to the file
	 * @return true if deletion was successful, false otherwise */
	boolean delete(String contentPath);

	/** Check if a file exists at the given path.
	 * @param contentPath the relative path to the file
	 * @return true if file exists, false otherwise */
	boolean exists(String contentPath);

	/** Get the absolute file path for a given content path.
	 * @param contentPath the relative path to the file
	 * @return the absolute file path */
	String getAbsolutePath(String contentPath);
}
