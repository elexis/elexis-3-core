package ch.elexis.core.services.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemhandleFilter;

public class TransientVirtualFilesystemHandle implements IVirtualFilesystemHandle {

	private String filename;

	public TransientVirtualFilesystemHandle(String webdavFileName) {
		this.filename = webdavFileName;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeAllBytes(byte[] content) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public long getContentLenght() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IVirtualFilesystemHandle copyTo(IVirtualFilesystemHandle destination) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualFilesystemHandle getParent() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualFilesystemHandle[] listHandles(IVirtualFilesystemhandleFilter ff) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualFilesystemHandle[] listHandles() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDirectoryUrl() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDirectory() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URL toURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<File> toFile() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public String getExtension() {
		int lastIndexOf = filename.lastIndexOf('.');
		if (lastIndexOf > -1) {
			return filename.substring(lastIndexOf + 1);
		}
		return StringUtils.EMPTY;
	}

	@Override
	public boolean exists() throws IOException {
		return false;
	}

	@Override
	public String getName() {
		return filename;
	}

	@Override
	public boolean canRead() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canWrite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAbsolutePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualFilesystemHandle moveTo(IVirtualFilesystemHandle target) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualFilesystemHandle subDir(String string) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualFilesystemHandle subFile(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualFilesystemHandle mkdir() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualFilesystemHandle mkdirs() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
