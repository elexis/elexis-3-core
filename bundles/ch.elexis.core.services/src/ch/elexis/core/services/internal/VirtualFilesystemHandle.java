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
import org.eclipse.core.runtime.URIUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemhandleFilter;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

public class VirtualFilesystemHandle implements IVirtualFilesystemHandle {
	
	private static final String ERROR_MESSAGE_CAN_NOT_HANDLE = "Can not handle type";
	
	private final URI uri;
	
	public VirtualFilesystemHandle(URI uri){
		this.uri = uri;
	}
	
	@Override
	public String toString(){
		return uri.toString();
	}
	
	public VirtualFilesystemHandle(File file) throws IOException{
		this.uri = file.toURI();
	}
	
	@Override
	public InputStream openInputStream() throws IOException{
		File file = URIUtil.toFile(uri);
		if (file != null) {
			return new FileInputStream(file);
		}
		return uri.toURL().openStream();
	}
	
	@Override
	public byte[] readAllBytes() throws IOException{
		try (InputStream in = openInputStream();
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			IOUtils.copy(in, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
	}
	
	@Override
	public OutputStream openOutputStream() throws IOException{
		if (!isDirectory()) {
			File file = URIUtil.toFile(uri);
			if (file != null) {
				return new FileOutputStream(file);
			}
			
			URLConnection openConnection = uri.toURL().openConnection();
			if (openConnection != null) {
				return openConnection.getOutputStream();
			}
		}
		
		throw new IOException("Does not support outputstream on directory");
	}
	
	@Override
	public IVirtualFilesystemHandle copyTo(IVirtualFilesystemHandle target) throws IOException{
		if (target.isDirectory()) {
			URI targetURI = target.getURI().resolve(this.getName());
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
	public IVirtualFilesystemHandle getParent() throws IOException{
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
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}
	
	@Override
	public IVirtualFilesystemHandle[] listHandles() throws IOException{
		return listHandles(null);
	}
	
	@Override
	public IVirtualFilesystemHandle[] listHandles(IVirtualFilesystemhandleFilter ff)
		throws IOException{
		
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
					SmbFile _file = listFiles[i];
					
					try {
						String fileURL = _file.getURL().toString().replaceAll(" ", "%20");
						URI _fileUri = new URI(fileURL);
						retVal[i] = new VirtualFilesystemHandle(_fileUri);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					
				}
				return retVal;
			}
		}
		
		// TODO http?
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}
	
	@Override
	public void delete() throws IOException{
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
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}
	
	@Override
	public URL toURL(){
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			LoggerFactory.getLogger(getClass()).warn("toURL()", e);
		}
		return null;
	}
	
	@Override
	public boolean isDirectory() throws IOException{
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
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}
	
	@Override
	public Optional<File> toFile(){
		return Optional.ofNullable(URIUtil.toFile(uri));
	}
	
	private Optional<SmbFile> toSmbFile(){
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
	
	@Override
	public String getExtension(){
		String _url = uri.toString();
		int lastIndexOf = _url.lastIndexOf('.');
		if (lastIndexOf > -1) {
			return _url.substring(lastIndexOf + 1);
		}
		return "";
	}
	
	@Override
	public boolean exists() throws IOException{
		File file = URIUtil.toFile(uri);
		if (file != null) {
			return file.exists();
		}
		URLConnection connection = uri.toURL().openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				return smbFile.exists();
			}
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}
	
	@Override
	public String getName(){
		String path = uri.getPath();
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return FilenameUtils.getName(path);
	}
	
	@Override
	public boolean canRead(){
		Optional<File> file = toFile();
		if (file.isPresent()) {
			return file.get().canRead();
		}
		
		Optional<SmbFile> smbFile = toSmbFile();
		if (smbFile.isPresent()) {
			try {
				return smbFile.get().canRead();
			} catch (SmbException e) {}
		}
		
		return false;
	}
	
	@Override
	public String getAbsolutePath(){
		return uri.toString();
	}
	
	@Override
	public IVirtualFilesystemHandle moveTo(IVirtualFilesystemHandle target) throws IOException{
		if (target.isDirectory()) {
			URI targetURI = target.getURI().resolve(this.getName());
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
		// TODO SMB if on same resource - use rename method
		copyTo(target);
		delete();
		return target;
	}
	
	@Override
	public IVirtualFilesystemHandle subDir(String subDir) throws IOException{
		subDir = subDir.replaceAll(" ", "%20");
		if (!subDir.endsWith("/")) {
			subDir += "/";
		}
		URI _uri = URIUtil.append(uri, subDir);
		return new VirtualFilesystemHandle(_uri);
	}
	
	@Override
	public IVirtualFilesystemHandle subFile(String subFile) throws IOException{
		if (!isDirectory()) {
			throw new IOException("[" + uri + "] is not a directory");
		}
		if (subFile.startsWith("/")) {
			throw new IllegalArgumentException("must not start with /");
		}
		
		subFile = subFile.replaceAll(" ", "%20");
		URI _uri = URIUtil.append(uri, subFile);
		return new VirtualFilesystemHandle(_uri);
	}
	
	@Override
	public IVirtualFilesystemHandle mkdir() throws IOException{
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
		}
		throw new IOException(ERROR_MESSAGE_CAN_NOT_HANDLE);
	}
	
	public URI getURI(){
		return uri;
	}
	
	private class IVFSFileFilterAdapter implements FileFilter, SmbFileFilter {
		
		private final IVirtualFilesystemhandleFilter ff;
		
		public IVFSFileFilterAdapter(IVirtualFilesystemhandleFilter ff){
			this.ff = ff;
		}
		
		@Override
		public boolean accept(File pathname){
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
		public boolean accept(SmbFile file) throws SmbException{
			if (ff == null) {
				return true;
			}
			
			VirtualFilesystemHandle pathnameVfsHandle;
			try {
				String fileURL = file.getURL().toString().replaceAll(" ", "%20");
				URI fileUri = new URI(fileURL);
				pathnameVfsHandle = new VirtualFilesystemHandle(fileUri);
				return ff.accept(pathnameVfsHandle);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		
	}
}
