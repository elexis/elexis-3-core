package ch.elexis.core.services.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.URIUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemhandleFilter;
import ch.elexis.core.webdav.WebdavFile;
import ch.elexis.core.webdav.WebdavFileNameFilter;
import jcifs.SmbTreeHandle;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

public class VirtualFilesystemHandle implements IVirtualFilesystemHandle {

	private static final String ERROR_MESSAGE_CAN_NOT_HANDLE = "Can not handle type";

	private final URI uri;

	public VirtualFilesystemHandle(URI uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	public VirtualFilesystemHandle(File file) throws IOException {
		this(file.toURI());
	}

	@Override
	public InputStream openInputStream() throws IOException {
		if (!isDirectoryUrl()) {
			File file = URIUtil.toFile(uri);
			if (file != null) {
				return new FileInputStream(file);
			}
			URL url = URIUtil.toURL(uri);
			return url.openStream();
		}
		throw new IOException("Inputstream on directory not supported");
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		try (InputStream in = openInputStream();
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			IOUtils.copy(in, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
	}

	@Override
	public void writeAllBytes(byte[] content) throws IOException {
		try (OutputStream outputStream = openOutputStream()) {
			IOUtils.write(content, outputStream);
		}
	}

	@Override
	public long getContentLenght() throws IOException {
		File file = URIUtil.toFile(uri);
		if (file != null) {
			return file.length();
		}
		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				return smbFile.getContentLengthLong();
			}
		} else if (connection instanceof WebdavFile) {
			return ((WebdavFile) connection).getContentLengthLong();
		}
		throw new IOException("Can not determine content length on [" + connection.getClass() + "]");
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		if (!isDirectoryUrl()) {
			File file = URIUtil.toFile(uri);
			if (file != null) {
				return new FileOutputStream(file);
			}

			URLConnection openConnection = uri.toURL().openConnection();
			if (openConnection != null) {
				return openConnection.getOutputStream();
			}
		}

		throw new IOException("Outputstream on directory not supported");
	}

	@Override
	public IVirtualFilesystemHandle copyTo(IVirtualFilesystemHandle target) throws IOException {
		if (target.isDirectory()) {
			String targetString = this.getName().replace(StringUtils.SPACE, "%20");
			URI targetURI = target.getURI().resolve(targetString);
			target = new VirtualFilesystemHandle(targetURI);
			return copyTo(target);
		}

		try (InputStream in = openInputStream()) {
			try (OutputStream out = target.openOutputStream()) {
				IOUtils.copy(in, out);
			}
		}
		return target;
	}

	@Override
	public IVirtualFilesystemHandle getParent() throws IOException {
		File file = URIUtil.toFile(uri);
		if (file != null) {
			URI parent = file.getParentFile().toURI();
			return new VirtualFilesystemHandle(parent);
		}

		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				try {
					URL parent = new URL(smbFile.getParent());
					return new VirtualFilesystemHandle(parent.toURI());
				} catch (MalformedURLException | URISyntaxException mfe) {
					LoggerFactory.getLogger(getClass()).warn("getParent()", mfe);
				}
			}
		} else if (connection instanceof WebdavFile) {
			try {
				String parent = ((WebdavFile) connection).getParent();
				URI webdavUri = convertToWebdavHandlingUrl(new URL(parent));
				return new VirtualFilesystemHandle(webdavUri);
			} catch (URISyntaxException e) {
				LoggerFactory.getLogger(getClass()).warn("getParent()", e);
			}
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public IVirtualFilesystemHandle[] listHandles() throws IOException {
		return listHandles(null);
	}

	@Override
	public IVirtualFilesystemHandle[] listHandles(IVirtualFilesystemhandleFilter ff) throws IOException {

		if (!isDirectory()) {
			throw new IOException("not a directory [" + uri + "]");
		}

		Optional<File> file = toFile();
		if (file.isPresent()) {
			File[] listFiles = file.get().listFiles(new IVFSFileFilterAdapter(ff));
			IVirtualFilesystemHandle[] retVal = new IVirtualFilesystemHandle[listFiles.length];
			for (int i = 0; i < listFiles.length; i++) {
				File _file = listFiles[i];
				retVal[i] = new VirtualFilesystemHandle(_file);
			}
			return retVal;
		}

		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				SmbFile[] listFiles = smbFile.listFiles(new IVFSFileFilterAdapter(ff));
				IVirtualFilesystemHandle[] retVal = new IVirtualFilesystemHandle[listFiles.length];
				for (int i = 0; i < listFiles.length; i++) {
					try (SmbFile _file = listFiles[i]) {
						String fileURL = convertToURLEscapingIllegalCharacters(_file.getURL());
						URI _fileUri = new URI(fileURL);
						retVal[i] = new VirtualFilesystemHandle(_fileUri);
					} catch (URISyntaxException e) {
						LoggerFactory.getLogger(getClass()).warn("listHandles() [{}]", listFiles[i], e);
					}

				}
				return retVal;
			}
		} else if (connection instanceof WebdavFile) {
			WebdavFile[] listFiles = ((WebdavFile) connection).listFiles(new IVFSFileFilterAdapter(ff));
			IVirtualFilesystemHandle[] retVal = new IVirtualFilesystemHandle[listFiles.length];
			for (int i = 0; i < listFiles.length; i++) {
				try {
					URI webdavUri = convertToWebdavHandlingUrl(listFiles[i].getURL());
					retVal[i] = new VirtualFilesystemHandle(webdavUri);
				} catch (URISyntaxException e) {
					LoggerFactory.getLogger(getClass()).warn("listHandles() [{}]", listFiles[i], e);
				}
			}
			return retVal;
		}

		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public void delete() throws IOException {
		if (toFile().isPresent()) {
			File file = toFile().get();
			if (file.isDirectory()) {
				// delete all sub-entries first
				List<IVirtualFilesystemHandle> subHandles = Arrays.asList(listHandles());
				for (IVirtualFilesystemHandle subHandle : subHandles) {
					subHandle.delete();
				}

			}
			Files.delete(toFile().get().toPath());
			return;
		}

		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				smbFile.delete();
				return;
			}
		} else if (connection instanceof WebdavFile) {
			((WebdavFile) connection).delete();
			return;
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public URL toURL() {
		try {
			return URIUtil.toURL(this.uri);
		} catch (MalformedURLException e) {
			LoggerFactory.getLogger(getClass()).warn("toURL()", e);
		}
		return null;
	}

	@Override
	public boolean isDirectoryUrl() throws IOException {
		try {
			return getURI().toURL().toString().endsWith("/");
		} catch (MalformedURLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean isDirectory() throws IOException {
		if (toFile().isPresent()) {
			File file = toFile().get();
			return file.isDirectory();
		}
		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				return smbFile.isDirectory();
			} catch (SmbException e) {
				throw new IOException(e);
			}
		} else if (connection instanceof WebdavFile) {
			return ((WebdavFile) connection).isDirectory();
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public Optional<File> toFile() {
		return Optional.ofNullable(URIUtil.toFile(uri));
	}

	private Optional<SmbFile> toSmbFile() {
		try {
			URLConnection connection = uri.toURL().openConnection();
			if (connection instanceof SmbFile) {
				try (SmbFile smbFile = (SmbFile) connection) {
					return Optional.of(smbFile);
				}
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).warn("toSmbFile()", e);
		}

		return Optional.empty();
	}

	private Optional<WebdavFile> toWebdavFile() {
		try {
			URLConnection connection = uri.toURL().openConnection();
			if (connection instanceof WebdavFile) {
				return Optional.of((WebdavFile) connection);
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).warn("toWebdavFile()", e);
		}

		return Optional.empty();
	}

	@Override
	public String getExtension() {
		String _url = uri.toString();
		int lastIndexOf = _url.lastIndexOf('.');
		if (lastIndexOf > -1) {
			return _url.substring(lastIndexOf + 1);
		}
		return StringUtils.EMPTY;
	}

	@Override
	public boolean exists() throws IOException {
		File file = URIUtil.toFile(uri);
		if (file != null) {
			return file.exists();
		}
		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				return smbFile.exists();
			}
		} else if (connection instanceof WebdavFile) {
			return ((WebdavFile) connection).exists();
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public String getName() {
		String path = uri.getPath();
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return FilenameUtils.getName(path);
	}

	@Override
	public boolean canRead() {
		Optional<File> file = toFile();
		if (file.isPresent()) {
			return file.get().canRead();
		}

		Optional<SmbFile> smbFile = toSmbFile();
		if (smbFile.isPresent()) {
			try {
				return smbFile.get().canRead();
			} catch (SmbException e) {
			}
		}

		Optional<WebdavFile> webdavFile = toWebdavFile();
		if (webdavFile.isPresent()) {
			try {
				return webdavFile.get().canRead();
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("canRead()", e);
			}
		}

		return false;
	}

	@Override
	public boolean canWrite() {
		Optional<File> file = toFile();
		if (file.isPresent()) {
			return file.get().canWrite();
		}

		Optional<SmbFile> smbFile = toSmbFile();
		if (smbFile.isPresent()) {
			try {
				return smbFile.get().canWrite();
			} catch (SmbException e) {
			}
		}

		Optional<WebdavFile> webdavFile = toWebdavFile();
		if (webdavFile.isPresent()) {
			try {
				return webdavFile.get().canWrite();
			} catch (IOException e) {
			}
		}

		return false;
	}

	@Override
	public String getAbsolutePath() {
		return uri.toString();
	}

	@Override
	public IVirtualFilesystemHandle moveTo(IVirtualFilesystemHandle target) throws IOException {
		if (target.isDirectory()) {
			String targetString = this.getName().replace(StringUtils.SPACE, "%20");
			URI targetURI = target.getURI().resolve(targetString);
			target = new VirtualFilesystemHandle(targetURI);
			return moveTo(target);
		}

		Optional<File> file = toFile();
		if (file.isPresent()) {
			Optional<File> _target = target.toFile();
			if (_target.isPresent()) {
				// from file to file
				Path path = Files.move(file.get().toPath(), _target.get().toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				return new VirtualFilesystemHandle(path.toFile());
			}
		}

		Optional<SmbFile> smbFile = toSmbFile();
		if (smbFile.isPresent()) {
			Optional<SmbFile> smbFileTarget = ((VirtualFilesystemHandle) target).toSmbFile();
			if (smbFileTarget.isPresent()) {

				try (SmbTreeHandle smbFileTh = smbFile.get().getTreeHandle();
						SmbTreeHandle smbFileThTarget = smbFileTarget.get().getTreeHandle()) {

					if (smbFileTh.isSameTree(smbFileThTarget)) {

						// both resources on same share
						smbFile.get().renameTo(smbFileTarget.get(), true);
					} else {
						smbFile.get().copyTo(smbFileTarget.get());
						smbFile.get().delete();
					}
				}
			}
			return target;
		}
		
		Optional<WebdavFile> webdavFile = toWebdavFile();
		if(webdavFile.isPresent()) {
			Optional<WebdavFile> webdavTarget = ((VirtualFilesystemHandle) target).toWebdavFile();
			if(webdavTarget.isPresent()) {
				webdavFile.get().move(webdavTarget.get().getURL());
				return target;
			}
		}

		throw new IOException("Invalid type");
	}

	@Override
	public IVirtualFilesystemHandle subDir(String subDir) throws IOException {
		if (!subDir.endsWith("/")) {
			subDir += "/";
		}

		URI _uri = null;
		if (uri.getAuthority() != null && uri.getAuthority().length() > 0 && uri.getAuthority().charAt(1) == ':') {
			// workaround - URIUtil "swallows" C: authority
			String _cur = uri.toString();
			if (!_cur.endsWith("/")) {
				_cur += "/";
			}
			try {
				_uri = IVirtualFilesystemService.stringToURI(_cur + subDir);
			} catch (MalformedURLException | URISyntaxException e) {
				throw new IOException(e);
			}
		} else {
			_uri = URIUtil.append(uri, subDir);
		}

		return new VirtualFilesystemHandle(_uri);
	}

	@Override
	public IVirtualFilesystemHandle subFile(String subFile) throws IOException {
		if (!isDirectoryUrl()) {
			throw new IOException("[" + uri + "] is not a directory");
		}
		if (subFile.startsWith("/")) {
			throw new IllegalArgumentException("must not start with /");
		}
		URI _uri = null;
		if (uri.getAuthority() != null && uri.getAuthority().length() > 0 && uri.getAuthority().charAt(1) == ':') {
			// workaround - URIUtil "swallows" C: authority
			try {
				_uri = IVirtualFilesystemService.stringToURI(uri.toString() + subFile);
			} catch (MalformedURLException | URISyntaxException e) {
				throw new IOException(e);
			}
		} else {
			_uri = URIUtil.append(uri, subFile);
		}
		return new VirtualFilesystemHandle(_uri);
	}

	@Override
	public IVirtualFilesystemHandle mkdir() throws IOException {
		File file = URIUtil.toFile(uri);
		if (file != null) {
			file.mkdir();
			return this;
		}
		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				if (!smbFile.exists()) {
					smbFile.mkdir();
				}
				return this;
			}
		} else if (connection instanceof WebdavFile) {
			((WebdavFile) connection).mkdir();
			return this;
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public IVirtualFilesystemHandle mkdirs() throws IOException {
		File file = URIUtil.toFile(uri);
		if (file != null) {
			file.mkdirs();
			return this;
		}
		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				if (!smbFile.exists()) {
					smbFile.mkdirs();
				}
				return this;
			}
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public URI getURI() {
		return uri;
	}

	private class IVFSFileFilterAdapter implements FileFilter, SmbFileFilter, WebdavFileNameFilter {

		private final IVirtualFilesystemhandleFilter ff;

		public IVFSFileFilterAdapter(IVirtualFilesystemhandleFilter ff) {
			this.ff = ff;
		}

		@Override
		public boolean accept(File pathname) {
			if (ff == null) {
				return true;
			}

			VirtualFilesystemHandle pathnameVfsHandle;
			try {
				pathnameVfsHandle = new VirtualFilesystemHandle(pathname);
				return ff.accept(pathnameVfsHandle);
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).warn("accept()", e);
			}

			return false;
		}

		@Override
		public boolean accept(SmbFile file) throws SmbException {
			if (ff == null) {
				return true;
			}

			VirtualFilesystemHandle pathnameVfsHandle;
			try {
				String fileURL = convertToURLEscapingIllegalCharacters(file.getURL());
				URI fileUri = new URI(fileURL);
				pathnameVfsHandle = new VirtualFilesystemHandle(fileUri);
				return ff.accept(pathnameVfsHandle);
			} catch (URISyntaxException | MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		public boolean accept(String webdavFileName) {
			if (ff == null) {
				return true;
			}
			
			return ff.accept(new TransientVirtualFilesystemHandle(webdavFileName));
		}
		
	}

	/**
	 * Webdav URLStreamHandlerService are registered for protocol "davs" (and "dav"
	 * in testing scenario), in order not to override the original "http" and
	 * "https" protocl handling. To keep using these handlers, we need to rewrite
	 * the "internally" used http protocol to our davs/dav scheme.
	 * 
	 * @param url
	 * @return
	 * @throws URISyntaxException
	 */
	private URI convertToWebdavHandlingUrl(URL url) throws URISyntaxException {
		String targetProtocol = "https".equals(url.getProtocol()) ? "davs" : "dav";
		return new URI(targetProtocol, url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(),
				url.getRef());
	}

	/**
	 * https://en.wikipedia.org/wiki/Percent-encoding of reserved characters
	 *
	 * @param toEscape
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @see adapted from https://stackoverflow.com/a/30640843/905817
	 */
	private String convertToURLEscapingIllegalCharacters(URL url) throws MalformedURLException, URISyntaxException {
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
				url.getQuery(), url.getRef());
		// if a % is included in the toEscape string, it will be re-encoded to %25 and
		// we don't want re-encoding, just encoding
		return uri.toString().replace("%25", "%");
	}
}
