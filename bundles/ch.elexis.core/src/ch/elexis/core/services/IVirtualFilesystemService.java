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

	/**
	 * Generate a handle from an URL or UNC string. UNC paths (e.g.
	 * \\server\share\folder) are directly passed to the OS in windows, and
	 * rewritten to URL format (e.g. smb://server/share/folder) on other operating
	 * systems.
	 *
	 * @param urlString or uncString
	 * @return
	 * @throws IOException
	 */
	default IVirtualFilesystemHandle of(String urlString) throws IOException {
		return of(urlString, true);
	}

	/**
	 * Generate a handle from an URL or UNC string. UNC paths (e.g.
	 * \\server\share\folder) are directly passed to the OS in windows, and
	 * rewritten to URL format (e.g. smb://server/share/folder) on other operating
	 * systems.
	 * 
	 * @param urlString
	 * @param performVariableReplacement
	 * @return
	 * @throws IOException
	 * @since 3.12
	 */
	IVirtualFilesystemHandle of(String urlString, boolean performVariableReplacement) throws IOException;

	IVirtualFilesystemHandle of(File file) throws IOException;

	/**
	 * Hide the password that may be part of the URL, accepts UNCs or unix paths
	 * simply returning them
	 *
	 * @param urlString
	 * @return the same string, with the password replaced with <code>***</code>. If
	 *         the URL is incorrect the exception message is returned.
	 */
	static String hidePasswordInUrlString(String urlString) {
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
		String replacement = userInfo;
		if (userInfo.indexOf(':') > 0) {
			replacement = userInfo.substring(0, userInfo.indexOf(':')) + ":***";
		}
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
		InputStream openInputStream() throws IOException;

		/**
		 * A stream to write the content. Will create or overwrite.
		 *
		 * @return
		 * @throws IOException
		 */
		OutputStream openOutputStream() throws IOException;

		/**
		 * Read the content and return it as byte array
		 *
		 * @return
		 * @throws IOException
		 */
		byte[] readAllBytes() throws IOException;

		/**
		 * Write the given content into the file, fails if directory
		 * 
		 * @throws IOException
		 * @since 3.10
		 */
		void writeAllBytes(byte[] content) throws IOException;

		/**
		 * Return the length of the of the content
		 *
		 * @return
		 * @throws IOException
		 */
		long getContentLenght() throws IOException;

		/**
		 * Copy the contents of this handle to a new handle, where the underlying
		 * resource might not actually exist yet.
		 *
		 * @param destination
		 * @throws IOException
		 * @return handle reflecting the location of the copied file
		 */
		IVirtualFilesystemHandle copyTo(IVirtualFilesystemHandle destination) throws IOException;

		/**
		 *
		 * @return the parent url of the given parent
		 */
		IVirtualFilesystemHandle getParent() throws IOException;

		/**
		 * Only if {@link #isDirectory()}:
		 *
		 * @param ff
		 * @return
		 * @see File#listFiles(java.io.FileFilter) equivalent behavior
		 */
		IVirtualFilesystemHandle[] listHandles(IVirtualFilesystemhandleFilter ff) throws IOException;

		/**
		 * Only if {@link #isDirectory()}:
		 *
		 * @return
		 * @throws IOException
		 */
		IVirtualFilesystemHandle[] listHandles() throws IOException;

		/**
		 * Delete the corresponding file entry. If it is a directory, perform a
		 * recursive delete.
		 *
		 * @param urlString
		 * @throws IOException
		 */
		void delete() throws IOException;

		/**
		 * if the URL ends with "/" - does not check the underlying resource for its
		 * actual type
		 */
		boolean isDirectoryUrl() throws IOException;

		/**
		 * Checks a possibly existing file entry if it is a directory
		 * 
		 * @return <code>false</code> if not found or not a directory
		 * @throws IOException
		 * @since 3.10
		 */
		boolean isDirectory() throws IOException;

		/**
		 *
		 * @return
		 */
		URL toURL();

		/**
		 *
		 * @return
		 */
		URI getURI();

		/**
		 *
		 * @return a File representation of this object, or empty if it is not a file
		 */
		Optional<File> toFile();

		/**
		 *
		 * @return the file ending (after the dot) or an empty string
		 */
		String getExtension();

		/**
		 * @return does the underlying resource really exist
		 * @throws IOException
		 */
		boolean exists() throws IOException;

		/**
		 * @return the name including the extension
		 */
		String getName();

		/**
		 *
		 * @return
		 */
		boolean canRead();

		/**
		 *
		 * @return
		 */
		boolean canWrite();

		/**
		 *
		 * @return
		 */
		String getAbsolutePath();

		/**
		 * Move this to the handle. If this is a file and handle is a directory, the
		 * filename is kept and return references a file handle in the provided
		 * directory.<br>
		 * If this is a file, and the handle is a file, then it will be moved and
		 * possibly renamed to the target file. Required parent directories are not
		 * validated. <br>
		 * If a file with the same name exists in the target directory, it will be
		 * overwritten.
		 *
		 * @param handle the target handle of this
		 * @throws IOException
		 * @return the updated handle reflecting the new location
		 */
		IVirtualFilesystemHandle moveTo(IVirtualFilesystemHandle target) throws IOException;

		/**
		 * Create a possibly not yet existing sub-directory handle. The actual directory
		 * addressed, must then be created using {@link #mkdir()}
		 *
		 * @param string
		 * @return
		 */
		IVirtualFilesystemHandle subDir(String string) throws IOException;

		/**
		 * Only if {@link #isDirectory()}: create a sub file handle
		 *
		 * @param name
		 * @return
		 */
		IVirtualFilesystemHandle subFile(String name) throws IOException;

		/**
		 * Create a directory. Does not fail if directory already exists. This operation
		 * is valid both for directory-, and file-representing urls.
		 *
		 * @return its own handle
		 * @throws IOException
		 */
		IVirtualFilesystemHandle mkdir() throws IOException;

		/**
		 * Creates the directory named by this abstract pathname, including any
		 * necessary but nonexistent parent directories. Note that if this operation
		 * fails it may have succeeded in creating some of the necessary parent
		 * directories.
		 * 
		 * @return its own handle
		 * @throws IOException
		 * @since 3.12
		 */
		IVirtualFilesystemHandle mkdirs() throws IOException;

	}

	@FunctionalInterface
	public interface IVirtualFilesystemhandleFilter {
		boolean accept(IVirtualFilesystemHandle handle);
	}

	/**
	 * Convert a string to an URI. Tries to support as many cross platform paths as
	 * required and translate them to a usable format.
	 *
	 * @param value
	 * @return
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public static URI stringToURI(String value) throws URISyntaxException, MalformedURLException {

		value = value.replaceAll("%20", StringUtils.SPACE).replace("%7B", "{").replace("%7D", "}");

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
		// url may contain '#' characters which in a URL is referred to as a fragment
		// (leading to a getRef())
		// We don't use it as those, that is we don't have fragments, so we need to pass
		// this to the path
		String path = url.getPath();
		if (url.getRef() != null) {
			path += "#" + url.getRef();
		}

		if (url.getAuthority() != null && url.getAuthority().length() > 1 && url.getAuthority().charAt(1) == ':') {
			URI uri = new URI("file", url.getAuthority(), path, url.getQuery(), null);
			return uri;
		}

		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), path, url.getQuery(),
				null);
		return uri;
	}

}
