package ch.elexis.core.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Service to handle filesystem resources. These resources may be located on the
 * local computer, or on a remote network source. Currently we support the
 * following URL handles.
 * <ol>
 * <li>file:///directory/filename.txt
 * <li>/directory/filename.txt
 * <li>smb://[username:password]computername/share/directory/filename
 * <li>\\computername\share\directory\filename
 * </ol>
 * In order to handle the files itself, this service creates in
 * {@link IVirtualFilesystemHandle} via the respective <code>of</code> methods.
 */
public interface IVirtualFilesystemService {

	public IVirtualFilesystemHandle of(String urlString) throws IOException;

	public IVirtualFilesystemHandle of(File file) throws IOException;

	public interface IVirtualFilesystemHandle {

		public InputStream openInputStream() throws IOException;

		/**
		 * A stream to write the content. Will create or overwrite.
		 * 
		 * @return
		 * @throws IOException
		 */
		public OutputStream openOutputStream() throws IOException;

		/**
		 * 
		 * @param destination
		 * @throws IOException
		 */
		public void copyTo(IVirtualFilesystemHandle destination) throws IOException;

		/**
		 * 
		 * @return
		 */
		public IVirtualFilesystemHandle getParent() throws IOException;

		/**
		 * List the contents of a directory, returning absolute URL+ handles
		 * 
		 * @param source
		 * @return
		 * @throws IOException
		 */
		public List<String> list() throws IOException;

		/**
		 * 
		 * @param ff
		 * @return
		 * @see File#listFiles(java.io.FileFilter) equivalent behavior
		 */
		public IVirtualFilesystemHandle[] listHandles(IVirtualFilesystemhandleFilter ff) throws IOException;

		public IVirtualFilesystemHandle[] listHandles() throws IOException;

		/**
		 * Delete the corresponding file entry. If it is a directory, perform a
		 * recursive delete.
		 * 
		 * @param urlString
		 * @throws IOException
		 */
		public void delete() throws IOException;

		/**
		 * 
		 * @return is the underlying resource of type directory
		 */
		public boolean isDirectory();

		/**
		 * 
		 * @return
		 */
		public URL toURL();

		/**
		 * 
		 * @return a File representation of this object, or empty if it is not a file
		 */
		public Optional<File> toFile();

		/**
		 * 
		 * @return the file ending (after the dot) or an empty string
		 */
		public String getExtension();

		/**
		 * @return does the underlying resource really exist
		 * @throws IOException
		 */
		public boolean exists() throws IOException;

		public String getName();

		public boolean canRead();

		public String getAbsolutePath();

		/**
		 * Rename the underlying resource
		 * 
		 * @param newFileName
		 * @return
		 */
		public boolean renameTo(String newFileName);
		
		public boolean moveTo(IVirtualFilesystemHandle handle);

		/**
		 * Only executable for a directory; Create a possibly not yet existing
		 * sub-directory handle.
		 * 
		 * @param string
		 * @return
		 */
		public IVirtualFilesystemHandle subDir(String string) throws IOException;

		/**
		 * Only executable for a directory; create a sub file handle
		 * 
		 * @param name
		 * @return
		 */
		public IVirtualFilesystemHandle subFile(String name) throws IOException;

		/**
		 * 
		 * @return its own handle
		 * @throws IOException
		 */
		public IVirtualFilesystemHandle mkdir() throws IOException;

	}

	@FunctionalInterface
	public interface IVirtualFilesystemhandleFilter {
		boolean accept(IVirtualFilesystemHandle handle);
	}

}
