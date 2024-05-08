package ch.elexis.core.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;

/**
 * @see https://docs.nextcloud.com/server/25/developer_manual/client_apis/WebDAV/basic.html
 */
public class WebdavFile extends URLConnection {

	/**
	 * If contained within "resourcetype" marks a directory
	 */
	private static QName COLLECTION_TYPE = new QName("DAV:", "collection");

	/**
	 * The properties to fetch with PROPFIND
	 */
	private static final Set<QName> PROPERTIES = Set.of(
			new QName("DAV:", "getetag", "d"),
			// contains child <d:collection/> if its a directory
			new QName("DAV:", "resourcetype", "d"),
			new QName("DAV:", "getlastmodified", "d"),
			new QName("DAV:", "getcontentlength", "d"),
			// https://github.com/owncloud/client/blob/master/src/common/remotepermissions.h
			new QName("http://owncloud.org/ns", "permissions", "oc"));

	private Sardine webdav;

	private Object isWriting;

	public WebdavFile(URL url) throws MalformedURLException {
		super(url);
		webdav = WebdavPool.INSTANCE.getSardine(url);
	}

	@Override
	public void connect() throws IOException {
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		PipedOutputStream out = new PipedOutputStream();
		final PipedInputStream in = new PipedInputStream(out);
		CompletableFuture.runAsync(() -> {
			try {
				isWriting = new Object();
				webdav.put(url.toString(), in, null, false);
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).warn("Error writing file [{}]", url.toString(), e);
			} finally {
				synchronized (isWriting) {
					isWriting.notifyAll();
					isWriting = null;
				}
			}
		});

		return out;
	}

	/**
	 * Wait until the current write operation finished and the {@link DavResource}
	 * is present. See {@link WebdavFile#getOutputStream()}.
	 */
	public void waitWriteComplete() {
		if (isWriting != null) {
			synchronized (isWriting) {
				try {
					isWriting.wait();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return webdav.get(url.toString());
	}

	@Override
	public long getContentLengthLong() {
		try {
			DavResource davResource = getDavResource();
			if (davResource != null) {
				return davResource.getContentLength();
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).warn("Cannot determine length", e);
		}
		return -1l;
	}

	public boolean isDirectory() throws IOException {
		DavResource davResource = getDavResource();
		if (davResource != null) {
			List<QName> resourceTypes = davResource.getResourceTypes();
			return resourceTypes.contains(COLLECTION_TYPE);
		}
		return false;
	}

	private DavResource getDavResource() throws IOException {
		try {
			List<DavResource> propfind = webdav.propfind(url.toString(), 0, PROPERTIES);
			if (propfind.isEmpty()) {
				return null;
			}
			return propfind.get(0);
		} catch (SardineException se) {
			if (se.getStatusCode() == 404) {
				return null;
			}
			throw se;
		}
	}

	public void delete() throws IOException {
		webdav.delete(getURL().toString());
	}

	public boolean exists() throws IOException {
		return webdav.exists(getURL().toString());
	}

	public void mkdir() throws IOException {
		webdav.createDirectory(url.toString());
	}

	public String getParent() throws URISyntaxException {
		URI uri = getURL().toURI();
		URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
		return parent.toString();
	}

	public boolean canRead() throws IOException {
		return getDavResource() != null;
	}

	public boolean canWrite() throws IOException {
		DavResource davResource = getDavResource();
		if (davResource != null) {
			String permissions = davResource.getCustomProps().get("permissions");
			if (davResource.getResourceTypes().contains(COLLECTION_TYPE)) {
				// is a directory
				return permissions.contains("C") && permissions.contains("K");
			} else {
				// is a file
				return permissions.contains("W");
			}
		}
		return false;
	}

	public void move(URL destination) throws IOException {
		webdav.move(getURL().toString(), destination.toString());
	}

	public WebdavFile[] listFiles(WebdavFileNameFilter filenameFilter) throws IOException {
		List<DavResource> list = webdav.list(getURL().toString(), 1);
		list.remove(0); // remove parent itself
		if (filenameFilter != null) {
			List<DavResource> collect = list.stream().filter(wdv -> filenameFilter.accept(wdv.getName()))
					.collect(Collectors.toList());
			return toWebdavFileArray(collect);
		}
		return toWebdavFileArray(list);
	}

	private WebdavFile[] toWebdavFileArray(List<DavResource> collect) {
		WebdavFile[] result = new WebdavFile[collect.size()];
		int i = 0;
		for (Iterator<DavResource> iterator = collect.iterator(); iterator.hasNext();) {
			DavResource davResource = iterator.next();
			URL url = null;
			try {
				URI href = davResource.getHref();
				if (href.isAbsolute()) {
					url = href.toURL();
				} else {
					url = getURL().toURI().resolve(href).toURL();
				}
				result[i++] = new WebdavFile(url);
			} catch (URISyntaxException | MalformedURLException e) {
				// should not happen
				LoggerFactory.getLogger(getClass()).error("malformed url [{}]", davResource, e);
			}
		}
		return result;
	}

}
