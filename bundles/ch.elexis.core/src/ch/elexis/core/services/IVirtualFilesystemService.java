package ch.elexis.core.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.utils.CoreUtil;

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
	
	/**
	 * Generate a handle from an URL or UNC string. UNC paths (e.g. \\server\share\folder) are
	 * directly passed to the OS in windows, and rewritten to URL format (e.g.
	 * smb://server/share/folder) on other operating systems.
	 * 
	 * @param urlString
	 *            or uncString
	 * @return
	 * @throws IOException
	 */
	public IVirtualFilesystemHandle of(String urlString) throws IOException;
	
	public IVirtualFilesystemHandle of(File file) throws IOException;
	
	/**
	 * Hide the password that may be part of the URL, accepts UNCs or unix paths simply returning
	 * them
	 * 
	 * @param urlString
	 * @return the same string, with the password replaced with <code>***</code>. If the URL is
	 *         incorrect the exception message is returned.
	 */
	static String hidePasswordInUrlString(String urlString){
		if (urlString.startsWith("\\\\") || urlString.startsWith("/")) {
			return urlString;
		}
		
		URI url;
		try {
			url = new URI(urlString);
		} catch (URISyntaxException e) {
			return e.getMessage();
		}
		
		String userInfo = url.getUserInfo();
		if (userInfo == null) {
			return url.toString();
		}
		String replacement = userInfo.substring(0, userInfo.indexOf(':')) + ":***";
		return urlString.replace(userInfo, replacement);
	}
	
	/**
	 * A handle for a file which may or may not exist.
	 */
	public interface IVirtualFilesystemHandle {
		
		/**
		 * A stream to read the content from.
		 * 
		 * @return
		 * @throws IOException
		 */
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
		 * Return the length of the of the content
		 * 
		 * @return
		 * @throws IOException
		 */
		public long getContentLenght() throws IOException;
		
		/**
		 * Copy the contents of this handle to a new handle, where the underlying resource might not
		 * actually exist yet.
		 * 
		 * @param destination
		 * @throws IOException
		 * @return handle reflecting the location of the copied file
		 */
		public IVirtualFilesystemHandle copyTo(IVirtualFilesystemHandle destination)
			throws IOException;
		
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
		 * @return
		 */
		public URI getURI();
		
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
		public boolean canWrite();
		
		/**
		 * 
		 * @return
		 */
		public String getAbsolutePath();
		
		/**
		 * Move this to the handle. If this is a file and handle is a directory, the filename is
		 * kept and return references a file handle in the provided directory.
		 * 
		 * @param handle
		 *            the target handle of this
		 * @throws IOException
		 * @return the updated handle reflecting the new location
		 */
		public IVirtualFilesystemHandle moveTo(IVirtualFilesystemHandle target) throws IOException;
		
		/**
		 * Create a possibly not yet existing sub-directory handle. The actual directory addressed,
		 * must then be created using {@link #mkdir()}
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
	
	/**
	 * Convert a string to an URI. Tries to support as many cross platform paths as required and
	 * translate them to a usable format.
	 * 
	 * @param value
	 * @return
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public static URI stringToURI(String value) throws URISyntaxException, MalformedURLException{
		
		value = value.replaceAll("%20", " ");
		
		// C:\main.c++ -> file:/C:/main.c++
		if (value.length() > 2 && value.charAt(1) == ':') {
			String replaced = value.replace("\\", "/");
			value = "file://" + replaced;
		}
		
		// UNC Path
		if (StringUtils.startsWith(value, "\\\\")) {
			String replaced = value.replace("\\", "/");
			if (CoreUtil.isWindows()) {
				// https://wiki.eclipse.org/Eclipse/UNC_Paths
				value = "file://" + replaced;
			} else {
				value = "smb:" + replaced;
			}
		}
		
		// absolute unixoid path
		if (value.startsWith("/")) {
			value = "file:" + value;
		}
		
		URL url = new URL(value);
		// url may contain '#' characters which in a URL is referred to as a fragment (leading to a getRef())
		// We don't use it as those, that is we don't have fragments, so we need to pass
		// this to the path
		String path = url.getPath();
		if (url.getRef() != null) {
			path += "#" + url.getRef();
		}
		
		if (url.getAuthority() != null && url.getAuthority().length() > 0
			&& url.getAuthority().charAt(1) == ':') {
			URI uri = new URI("file", url.getAuthority(), url.getPath(), url.getQuery(), null);
			return uri;
		}
		
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), path,
			url.getQuery(), null);
		return uri;
	}
	
}
