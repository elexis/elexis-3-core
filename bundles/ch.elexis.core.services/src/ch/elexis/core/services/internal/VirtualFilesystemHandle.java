package ch.elexis.core.services.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.smb.SmbFile;

public class VirtualFilesystemHandle implements IVirtualFilesystemHandle {
	
	private final URL url;
	
	public VirtualFilesystemHandle(URL url){
		this.url = url;
	}
	
	@Override
	public InputStream openInputStream() throws IOException{
		return url.openStream();
	}
	
	@Override
	public OutputStream openOutputStream() throws IOException{
		File file = FileUtils.toFile(url);
		if (file != null) {
			return new FileOutputStream(file);
		}
		
		URLConnection openConnection = url.openConnection();
		if (openConnection != null) {
			return openConnection.getOutputStream();
		}
		
		throw new IOException("Can not get outputStream for url [" + url.toString() + "]");
	}
	
	@Override
	public void copyTo(IVirtualFilesystemHandle destination) throws IOException{
		try (InputStream in = openInputStream()) {
			try (OutputStream out = destination.openOutputStream()) {
				IOUtils.copy(in, out);
			}
		}
	}
	
	@Override
	public IVirtualFilesystemHandle getParent() throws IOException{
		File file = FileUtils.toFile(url);
		if (file != null) {
			try {
				URL parent = file.getParentFile().toURI().toURL();
				return new VirtualFilesystemHandle(parent);
			} catch (MalformedURLException mfe) {}
		}
		
		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				try {
					URL parent = new URL(smbFile.getParent());
					return new VirtualFilesystemHandle(parent);
				} catch (MalformedURLException mfe) {}
			}
		}
		throw new IOException("Can not handle");
	}
	
	@Override
	public List<String> list() throws IOException{
		
		List<String> result = new ArrayList<>();
		
		File file = FileUtils.toFile(url);
		if (file != null && file.isDirectory()) {
			for (File child : file.listFiles()) {
				if (child.isFile() && child.canRead()) {
					result.add(child.getAbsolutePath());
				}
			}
			return result;
		}
		
		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				if (smbFile.isDirectory()) {
					CloseableIterator<SmbResource> children = smbFile.children();
					while (children.hasNext()) {
						SmbResource smbResource = children.next();
						if (smbResource.isFile() && smbResource.canRead()) {
							result.add(smbResource.getName());
						}
					}
				}
			}
			return result;
		}
		
		return result;
	}
	
	@Override
	public List<IVirtualFilesystemHandle> listHandles(){
		
		
		
		return null;
	}
	
	@Override
	public void delete() throws IOException{
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
		throw new IOException("Can not handle");
	}
	
	@Override
	public URL toURL(){
		return url;
	}
	
	@Override
	public boolean isDirectory() throws IOException{
		File file = FileUtils.toFile(url);
		if (file != null) {
			return file.isDirectory();
		}
		
		URLConnection connection = url.openConnection();
		if (connection instanceof SmbFile) {
			try (SmbFile smbFile = (SmbFile) connection) {
				return smbFile.isDirectory();
			}
		}
		throw new IOException("Can not handle");
	}
	
	@Override
	public Optional<File> toFile(){
		return Optional.ofNullable(FileUtils.toFile(url));
	}
	
	@Override
	public String getExtension(){
		try {
			String uri = url.toURI().toString();
			return uri.substring(uri.lastIndexOf('.'));
		} catch (URISyntaxException e) {}
		return "";
	}
	
	@Override
	public boolean exists() throws IOException{
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
		throw new IOException("Can not handle");
	}
	
	@Override
	public String getName(){
		return FilenameUtils.getBaseName(url.getPath());
	}
	
	@Override
	public boolean canRead(){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String getAbsolutePath(){
		// TODO Auto-generated method stub
		try {
			return url.toURI().toURL().toString();
		} catch (MalformedURLException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean renameTo(String newFileName){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public IVirtualFilesystemHandle subdir(String subdir) throws IOException{
		if(isDirectory()) {
			try {
				return new VirtualFilesystemHandle(url.toURI().resolve(subdir).toURL());
			} catch (URISyntaxException | MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} else {
			return getParent().subdir(subdir);
		}
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
				smbFile.mkdir();
				return this;
			}
		}
		throw new IOException("Can not handle");
	}
	
}
