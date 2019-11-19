package ch.elexis.core.services.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemhandleFilter;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

import ch.rgw.tools.StringTool;
public class VirtualFilesystemHandle implements IVirtualFilesystemHandle {

	private static final String ERROR_MESSAGE_CAN_NOT_HANDLE = "Can not handle type";

	private final URL url;

	public VirtualFilesystemHandle(URL url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return url.toString();
	}

	public VirtualFilesystemHandle(File file) throws IOException {
		try {
			this.url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return url.openStream();
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		try (InputStream in = url.openStream();
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			IOUtils.copy(in, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		if (!isDirectory()) {
			File file = FileUtils.toFile(url);
			if (file != null) {
				return new FileOutputStream(file);
			}

			URLConnection openConnection = url.openConnection();
			if (openConnection != null) {
				return openConnection.getOutputStream();
			}
		}

		throw new IOException("Does not support outputstream on directory");
	}

	@Override
	public void copyTo(IVirtualFilesystemHandle destination) throws IOException {
		try (InputStream in = openInputStream()) {
			try (OutputStream out = destination.openOutputStream()) {
				IOUtils.copy(in, out);
			}
		}
	}

	@Override
	public IVirtualFilesystemHandle getParent() throws IOException {
		File file = FileUtils.toFile(url);
		if (file != null) {
			try {
				URL parent = file.getParentFile().toURI().toURL();
				return new VirtualFilesystemHandle(parent);
			} catch (MalformedURLException mfe) {
			}
		}

		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				try {
					URL parent = new URL(smbFile.getParent());
					return new VirtualFilesystemHandle(parent);
				} catch (MalformedURLException mfe) {
				}
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
			throw new IOException("not a directory ["+getAbsolutePath()+"]");
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

		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				SmbFile[] listFiles = smbFile.listFiles(new IVFSFileFilterAdapter(ff));
				IVirtualFilesystemHandle[] retVal = new IVirtualFilesystemHandle[listFiles.length];
				for (int i = 0; i < listFiles.length; i++) {
					SmbFile _file = listFiles[i];
					retVal[i] = new VirtualFilesystemHandle(_file.getURL());
				}
				return retVal;
			}
		}

		// TODO http?
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public void delete() throws IOException {
		File file = FileUtils.toFile(url);
		if (file != null) {
			Files.delete(file.toPath());
			return;
		}

		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				smbFile.delete();
				return;
			}
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public URL toURL() {
		return url;
	}

	@Override
	public boolean isDirectory() throws IOException {
		if (toFile().isPresent()) {
			File file = toFile().get();
			return file.isDirectory();
		}
		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				return smbFile.isDirectory();
			} catch (SmbException e) {
				throw new IOException(e);
			}
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public Optional<File> toFile() {
		return Optional.ofNullable(FileUtils.toFile(url));
	}

	private Optional<SmbFile> toSmbFile() {
		try {
			URLConnection connection = url.openConnection();
			if (connection instanceof SmbFile) {
				try (SmbFile smbFile = (SmbFile) connection) {
					return Optional.of(smbFile);
				}
			}
		} catch (IOException e) {
			// TODO log?
		}

		return Optional.empty();
	}

	@Override
	public String getExtension() {
		try {
			String uri = url.toURI().toString();
			int lastIndexOf = uri.lastIndexOf('.');
			if (lastIndexOf > -1) {
				return uri.substring(lastIndexOf + 1);
			}
		} catch (URISyntaxException e) {
		}
		return StringTool.leer;
	}

	@Override
	public boolean exists() throws IOException {
		File file = FileUtils.toFile(url);
		if (file != null) {
			return file.exists();
		}
		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				return smbFile.exists();
			}
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public String getName() {
		return FilenameUtils.getName(url.getPath());
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

		return false;
	}

	@Override
	public String getAbsolutePath() {
		try {
			return url.toURI().toURL().toString();
		} catch (MalformedURLException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void moveTo(IVirtualFilesystemHandle target) throws IOException{
		Optional<File> file = toFile();
		if (file.isPresent()) {
			Optional<File> _target = target.toFile();
			if (_target.isPresent()) {
				// from file to file
				Files.move(file.get().toPath(), _target.get().toPath(), StandardCopyOption.REPLACE_EXISTING);
				return;
			}
		}
		
		copyTo(target);
		delete();
	}

	@Override
	public IVirtualFilesystemHandle subDir(String subdir) throws IOException {
		return subFile(subdir + StringTool.slash);
	}

	@Override
	public IVirtualFilesystemHandle subFile(String subFile) throws IOException {

		if(StringUtils.isBlank(subFile) || subFile.startsWith(StringTool.slash)) {
			throw new IllegalArgumentException();
		}
		
		if (!isDirectory()) {
			throw new IOException("not a directory");
		}

		try {
			return new VirtualFilesystemHandle(url.toURI().resolve(subFile).toURL());
		} catch (URISyntaxException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	@Override
	public IVirtualFilesystemHandle mkdir() throws IOException {
		File file = FileUtils.toFile(url);
		if (file != null) {
			file.mkdir();
			return this;
		}
		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				if(!smbFile.exists()) {
					smbFile.mkdir();
				}
				return this;
			}
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}

	private class IVFSFileFilterAdapter implements FileFilter, SmbFileFilter {

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return false;
		}

		@Override
		public boolean accept(SmbFile file) throws SmbException {
			if (ff == null) {
				return true;
			}

			VirtualFilesystemHandle pathnameVfsHandle;
			pathnameVfsHandle = new VirtualFilesystemHandle(file.getURL());
			return ff.accept(pathnameVfsHandle);
		}

	}
}
