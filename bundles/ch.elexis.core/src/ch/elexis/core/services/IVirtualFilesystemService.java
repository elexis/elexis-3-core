package ch.elexis.core.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Optional;

/**
 * Service to handle filesystem resources. These resources may be located on the local computer, or
 * on a remote network source. Currently we support the following URL handles.
 * <ol>
 * <li>file:///directory/filename.txt
 * <li>/directory/filename.txt
 * <li>smb://[username:password]computername/share/directory/filename
 * <li>\\computername\share\directory\filename
 * </ol>
 * In order to handle the files itself, this service creates in {@link IVirtualFilesystemHandle} via
 * the respective <code>of</code> methods.
 */
public interface IVirtualFilesystemService {
	
	public IVirtualFilesystemHandle of(String urlString) throws IOException;
	
	public IVirtualFilesystemHandle of(File file) throws IOException;
	
	/**
	 * A handle for a file which may or may not exist.
	 */
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
		 * Read the content and return it as byte array
		 * 
		 * @return
		 * @throws IOException
		 */
		public byte[] readAllBytes() throws IOException;
		
		/**
		 * Copy the contents of this handle to a new handle, where the underlying resource might not
		 * actually exist yet.
		 * 
		 * @param destination
		 * @throws IOException
		 */
		public void copyTo(IVirtualFilesystemHandle destination) throws IOException;
		
		/**
		 * 
		 * @return the parent url of the given parent
		 */
		public IVirtualFilesystemHandle getParent() throws IOException;
		
		/**
		 * Only if {@link #isDirectory()}:
		 * 
		 * @param ff
		 * @return
		 * @see File#listFiles(java.io.FileFilter) equivalent behavior
		 */
		public IVirtualFilesystemHandle[] listHandles(IVirtualFilesystemhandleFilter ff)
			throws IOException;
		
		/**
		 * Only if {@link #isDirectory()}:
		 * 
		 * @return
		 * @throws IOException
		 */
		public IVirtualFilesystemHandle[] listHandles() throws IOException;
		
		/**
		 * Delete the corresponding file entry. If it is a directory, perform a recursive delete.
		 * 
		 * @param urlString
		 * @throws IOException
		 */
		public void delete() throws IOException;
		
		/**
		 * 
		 * @return is the underlying resource of type directory
		 */
		public boolean isDirectory() throws IOException;
		
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
		
		/**
		 * 
		 * @return
		 */
		public String getName();
		
		/**
		 * 
		 * @return
		 */
		public boolean canRead();
		
		/**
		 * 
		 * @return
		 */
		public String getAbsolutePath();
		
		/**
		 * 
		 * @param handle
		 * @throws IOException
		 */
		public void moveTo(IVirtualFilesystemHandle handle) throws IOException;
		
		/**
		 * Only if {@link #isDirectory()}: Create a possibly not yet existing sub-directory handle.
		 * The actual directory addressed, must then be created using {@link #mkdir()}
		 * 
		 * @param string
		 * @return
		 */
		public IVirtualFilesystemHandle subDir(String string) throws IOException;
		
		/**
		 * Only if {@link #isDirectory()}: create a sub file handle
		 * 
		 * @param name
		 * @return
		 */
		public IVirtualFilesystemHandle subFile(String name) throws IOException;
		
		/**
		 * Create a directory. Does not fail if directory already exists.
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
